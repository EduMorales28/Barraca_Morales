import { useEffect, useState } from 'react'
import {
  getPedidos,
  getUsuarios,
  createUsuario,
  getClientes,
  getArticulos,
  getProveedores,
  createCliente,
  createProveedor,
  createArticulo,
  createPedido,
  asignarPedido,
  aceptarPedido
} from '../api'
import { StatCard, PedidoCard, FormInput, Button, StateBadge } from '../components/index.jsx'

const emptyItem = {
  codigo: '',
  nombre: '',
  cantidad: 1,
  precio: ''
}

const emptyPedido = {
  cliente: '',
  direccion: '',
  lat: '',
  lng: '',
  levantado: 'con_mostrador',
  levantado_en_mostrador: '',
  sin_levantado_mostrador: false,
  items: [{ ...emptyItem }]
}

const roleLabels = {
  admin: 'Administrador',
  conductor: 'Conductor',
  creador_pedidos: 'Creador de pedidos'
}

const getAvailableViews = (rol) => {
  if (rol === 'admin') {
    return ['dashboard', 'pedidos', 'crear', 'items', 'conductores', 'usuarios']
  }

  if (rol === 'creador_pedidos') {
    return ['dashboard', 'pedidos', 'crear']
  }

  return ['dashboard', 'pedidos']
}

export default function DashboardPage({ user, view, onViewChange, onNotificationsChange }) {
  const [pedidos, setPedidos] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [conductores, setConductores] = useState([])
  const [clientes, setClientes] = useState([])
  const [articulos, setArticulos] = useState([])
  const [proveedores, setProveedores] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedPedido, setSelectedPedido] = useState(null)
  const [showDetalle, setShowDetalle] = useState(false)
  const [showAsignar, setShowAsignar] = useState(false)
  const [showClientePicker, setShowClientePicker] = useState(false)
  const [activeItemIndex, setActiveItemIndex] = useState(null)
  const [asignarForm, setAsignarForm] = useState({ conductor_id: '' })
  const [newUsuarioForm, setNewUsuarioForm] = useState({ nombre: '', email: '', password: '', rol: 'conductor' })
  const [newClienteForm, setNewClienteForm] = useState({ nombre: '', direccion: '', telefono: '' })
  const [itemsFilterProveedor, setItemsFilterProveedor] = useState('')
  const [newProveedorForm, setNewProveedorForm] = useState({ nombre: '', rut: '', direccion: '', telefono: '' })
  const [newArticuloForm, setNewArticuloForm] = useState({ codigo: '', nombre: '', proveedor_id: '', precio: '' })
  const [newPedido, setNewPedido] = useState(emptyPedido)

  const mapQuery = encodeURIComponent(newPedido.direccion.trim())
  const availableViews = getAvailableViews(user?.rol)
  const isAdmin = user?.rol === 'admin'
  const isConductor = user?.rol === 'conductor'
  const filteredClientes = clientes.filter((cliente) =>
    cliente.nombre.toLowerCase().includes(newPedido.cliente.toLowerCase())
  )
  const filteredArticulos = itemsFilterProveedor
    ? articulos.filter((articulo) => String(articulo.proveedor_id || '') === itemsFilterProveedor)
    : articulos

  useEffect(() => {
    loadData()
  }, [])

  useEffect(() => {
    if (!availableViews.includes(view)) {
      onViewChange(availableViews[0] || 'dashboard')
    }
  }, [availableViews, onViewChange, view])

  const loadData = async () => {
    try {
      setLoading(true)
      const usuariosRequest = isAdmin ? getUsuarios() : Promise.resolve({ data: [] })

      const [pedidosRes, usuariosRes, clientesRes, articulosRes, proveedoresRes] = await Promise.all([
        getPedidos(),
        usuariosRequest,
        getClientes(),
        getArticulos(),
        getProveedores()
      ])
      setPedidos(pedidosRes.data)
      setUsuarios(usuariosRes.data)
      setConductores(usuariosRes.data.filter((usuario) => usuario.rol === 'conductor'))
      setClientes(clientesRes.data)
      setArticulos(articulosRes.data)
      setProveedores(proveedoresRes.data)
      setError('')
    } catch (err) {
      setError('Error al cargar datos')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateUsuario = async (event) => {
    event.preventDefault()

    try {
      await createUsuario({
        nombre: newUsuarioForm.nombre.trim(),
        email: newUsuarioForm.email.trim(),
        password: newUsuarioForm.password,
        rol: newUsuarioForm.rol
      })
      setNewUsuarioForm({ nombre: '', email: '', password: '', rol: 'conductor' })
      await loadData()
    } catch (err) {
      setError(err.response?.data?.error || 'Error al crear usuario')
    }
  }

  const resetPedidoForm = () => {
    setNewPedido({
      ...emptyPedido,
      items: [{ ...emptyItem }]
    })
    setNewClienteForm({ nombre: '', direccion: '', telefono: '' })
    setShowClientePicker(false)
    setActiveItemIndex(null)
  }

  const updateItem = (index, changes) => {
    const items = [...newPedido.items]
    items[index] = { ...items[index], ...changes }
    setNewPedido({ ...newPedido, items })
  }

  const addItemRow = () => {
    setNewPedido({
      ...newPedido,
      items: [...newPedido.items, { ...emptyItem }]
    })
    setActiveItemIndex(newPedido.items.length)
  }

  const selectArticulo = (index, articulo) => {
    updateItem(index, {
      codigo: articulo.codigo || '',
      nombre: articulo.nombre,
      precio: articulo.precio ?? ''
    })
    setActiveItemIndex(null)
  }

  const handleSelectCliente = (cliente) => {
    setNewPedido({
      ...newPedido,
      cliente: cliente.nombre,
      direccion: newPedido.direccion || cliente.direccion || ''
    })
    setShowClientePicker(false)
  }

  const handleCreateCliente = async (event) => {
    event.preventDefault()

    try {
      const response = await createCliente({
        nombre: newClienteForm.nombre || newPedido.cliente,
        direccion: newClienteForm.direccion || newPedido.direccion,
        telefono: newClienteForm.telefono
      })

      const clienteCreado = response.data
      setClientes((prev) => [...prev, clienteCreado].sort((a, b) => a.nombre.localeCompare(b.nombre)))
      setNewPedido({
        ...newPedido,
        cliente: clienteCreado.nombre,
        direccion: newPedido.direccion || clienteCreado.direccion || ''
      })
      setNewClienteForm({ nombre: '', direccion: '', telefono: '' })
      setShowClientePicker(false)
    } catch (err) {
      setError(err.response?.data?.error || 'Error al crear cliente')
    }
  }

  const handleCreateProveedor = async (event) => {
    event.preventDefault()

    try {
      const response = await createProveedor(newProveedorForm)
      const proveedorCreado = response.data
      setProveedores((prev) => [...prev, proveedorCreado].sort((a, b) => a.nombre.localeCompare(b.nombre)))
      setNewProveedorForm({ nombre: '', rut: '', direccion: '', telefono: '' })
      setNewArticuloForm((prev) => ({ ...prev, proveedor_id: String(proveedorCreado.id) }))
    } catch (err) {
      setError(err.response?.data?.error || 'Error al crear proveedor')
    }
  }

  const handleCreateArticulo = async (event) => {
    event.preventDefault()

    try {
      const response = await createArticulo({
        codigo: newArticuloForm.codigo || null,
        nombre: newArticuloForm.nombre,
        proveedor_id: newArticuloForm.proveedor_id || null,
        precio: Number(newArticuloForm.precio) || 0
      })
      const articuloCreado = response.data
      setArticulos((prev) => [...prev, articuloCreado].sort((a, b) => a.nombre.localeCompare(b.nombre)))
      setNewArticuloForm({ codigo: '', nombre: '', proveedor_id: '', precio: '' })
    } catch (err) {
      setError(err.response?.data?.error || 'Error al crear item')
    }
  }

  const handleCreatePedido = async (event) => {
    event.preventDefault()

    try {
      await createPedido({
        ...newPedido,
        lat: newPedido.lat ? parseFloat(newPedido.lat) : null,
        lng: newPedido.lng ? parseFloat(newPedido.lng) : null,
        levantado: newPedido.sin_levantado_mostrador ? 'sin_mostrador' : 'con_mostrador',
        levantado_en_mostrador: newPedido.sin_levantado_mostrador ? '' : newPedido.levantado_en_mostrador,
        sin_levantado_mostrador: newPedido.sin_levantado_mostrador,
        items: newPedido.items
          .filter((item) => item.nombre.trim())
          .map((item) => ({
            codigo: item.codigo?.trim() || null,
            nombre: item.nombre.trim(),
            cantidad: Number(item.cantidad) || 1,
            precio: Number(item.precio) || 0
          }))
      })

      resetPedidoForm()
      onViewChange('pedidos')
      await loadData()
    } catch (err) {
      setError(err.response?.data?.error || 'Error al crear pedido')
    }
  }

  const handleAsignar = async (event) => {
    event.preventDefault()

    try {
      await asignarPedido(selectedPedido.id, asignarForm.conductor_id)
      setShowAsignar(false)
      setAsignarForm({ conductor_id: '' })
      await loadData()
    } catch (err) {
      setError(err.response?.data?.error || 'Error al asignar pedido')
    }
  }

  const handleAceptarPedido = async (pedido = selectedPedido) => {
    if (!pedido?.id) return

    try {
      await aceptarPedido(pedido.id)
      setShowDetalle(false)
      await loadData()
      await onNotificationsChange?.()
    } catch (err) {
      setError(err.response?.data?.error || 'Error al aceptar pedido')
    }
  }

  const openDetalle = (pedido) => {
    setSelectedPedido(pedido)
    setShowDetalle(true)
  }

  const openAsignar = (pedido) => {
    setSelectedPedido(pedido)
    setAsignarForm({ conductor_id: pedido.conductor_id || '' })
    setShowAsignar(true)
  }

  const stats = {
    total: pedidos.length,
    pendientes: pedidos.filter((pedido) => pedido.estado === 'pendiente').length,
    asignados: pedidos.filter((pedido) => pedido.estado === 'asignado').length,
    entregados: pedidos.filter((pedido) => pedido.estado === 'entregado').length
  }

  if (loading && pedidos.length === 0) {
    return (
      <div className="flex h-96 items-center justify-center">
        <div className="text-center">
          <div className="mb-4 text-4xl animate-spin">⏳</div>
          <p className="text-gray-600">Cargando pedidos...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      {error && (
        <div className="flex items-center gap-3 rounded-lg border-l-4 border-red-500 bg-red-100 p-4 text-red-700">
          <span className="text-2xl">⚠️</span>
          <span>{error}</span>
        </div>
      )}

      <div className="flex flex-wrap gap-4 border-b border-gray-200">
        {availableViews.map((viewId) => (
          <button
            key={viewId}
            onClick={() => onViewChange(viewId)}
            className={`border-b-2 px-6 py-3 font-semibold transition-colors ${
              view === viewId
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-600 hover:text-gray-800'
            }`}
          >
            {viewId === 'dashboard' && '📊 Dashboard'}
            {viewId === 'pedidos' && '📦 Pedidos'}
            {viewId === 'crear' && '➕ Crear Pedido'}
            {viewId === 'items' && '🧾 Items'}
            {viewId === 'conductores' && '👥 Conductores'}
            {viewId === 'usuarios' && '🔐 Usuarios'}
          </button>
        ))}
      </div>

      {view === 'dashboard' && (
        <div className="space-y-8">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
            <StatCard title="Total Pedidos" value={stats.total} icon="📦" color="border-blue-500" />
            <StatCard title="Pendientes" value={stats.pendientes} icon="⏳" color="border-yellow-500" />
            <StatCard title="En Reparto" value={stats.asignados} icon="🚚" color="border-purple-500" />
            <StatCard title="Entregados" value={stats.entregados} icon="✅" color="border-green-500" />
          </div>

          <div>
            <h2 className="mb-6 text-2xl font-bold text-gray-800">Últimos Pedidos</h2>
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
              {pedidos.slice(0, 4).map((pedido) => (
                <PedidoCard
                  key={pedido.id}
                  pedido={pedido}
                  onView={openDetalle}
                  onAsignar={openAsignar}
                  onAceptar={handleAceptarPedido}
                  canAssign={isAdmin}
                  canAccept={isConductor && pedido.conductor_id === user?.id}
                />
              ))}
            </div>
          </div>
        </div>
      )}

      {view === 'pedidos' && (
        <div>
          <h2 className="mb-6 text-2xl font-bold text-gray-800">Todos los Pedidos</h2>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            {pedidos.map((pedido) => (
              <PedidoCard
                key={pedido.id}
                pedido={pedido}
                onView={openDetalle}
                onAsignar={openAsignar}
                onAceptar={handleAceptarPedido}
                canAssign={isAdmin}
                canAccept={isConductor && pedido.conductor_id === user?.id}
              />
            ))}
          </div>
        </div>
      )}

      {view === 'items' && (
        <div className="space-y-8">
          <div className="flex items-center justify-between gap-4">
            <div>
              <h2 className="text-2xl font-bold text-gray-800">Items</h2>
              <p className="text-sm text-gray-500">Catálogo de artículos con filtro por proveedor.</p>
            </div>
            <div className="min-w-64">
              <label className="mb-2 block text-sm font-semibold text-gray-700">Filtrar por proveedor</label>
              <select
                value={itemsFilterProveedor}
                onChange={(event) => setItemsFilterProveedor(event.target.value)}
                className="w-full rounded-lg border-2 border-gray-200 px-4 py-2 focus:border-blue-500 focus:outline-none"
              >
                <option value="">Todos los proveedores</option>
                {proveedores.map((proveedor) => (
                  <option key={proveedor.id} value={proveedor.id}>
                    {proveedor.nombre}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
            <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
              <h3 className="mb-4 text-lg font-bold text-gray-800">Agregar Item</h3>
              <form onSubmit={handleCreateArticulo} className="space-y-4">
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <FormInput
                    label="Código"
                    value={newArticuloForm.codigo}
                    onChange={(event) => setNewArticuloForm({ ...newArticuloForm, codigo: event.target.value })}
                    placeholder="Ej: ART-006"
                  />
                  <FormInput
                    label="Nombre"
                    value={newArticuloForm.nombre}
                    onChange={(event) => setNewArticuloForm({ ...newArticuloForm, nombre: event.target.value })}
                    required
                  />
                </div>
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <label className="block text-sm font-semibold text-gray-700">Proveedor</label>
                    <select
                      value={newArticuloForm.proveedor_id}
                      onChange={(event) => setNewArticuloForm({ ...newArticuloForm, proveedor_id: event.target.value })}
                      className="w-full rounded-lg border-2 border-gray-200 px-4 py-2 focus:border-blue-500 focus:outline-none"
                    >
                      <option value="">Sin proveedor</option>
                      {proveedores.map((proveedor) => (
                        <option key={proveedor.id} value={proveedor.id}>
                          {proveedor.nombre}
                        </option>
                      ))}
                    </select>
                  </div>
                  <FormInput
                    label="Precio"
                    type="number"
                    value={newArticuloForm.precio}
                    onChange={(event) => setNewArticuloForm({ ...newArticuloForm, precio: event.target.value })}
                    placeholder="0.00"
                  />
                </div>
                <div className="flex justify-end">
                  <Button type="submit" variant="primary">Agregar item</Button>
                </div>
              </form>
            </div>

            <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
              <h3 className="mb-4 text-lg font-bold text-gray-800">Crear Proveedor</h3>
              <form onSubmit={handleCreateProveedor} className="space-y-4">
                <FormInput
                  label="Nombre del proveedor"
                  value={newProveedorForm.nombre}
                  onChange={(event) => setNewProveedorForm({ ...newProveedorForm, nombre: event.target.value })}
                  required
                />
                <FormInput
                  label="RUT"
                  value={newProveedorForm.rut}
                  onChange={(event) => setNewProveedorForm({ ...newProveedorForm, rut: event.target.value })}
                  placeholder="Ej: 214567890012"
                />
                <FormInput
                  label="Dirección"
                  value={newProveedorForm.direccion}
                  onChange={(event) => setNewProveedorForm({ ...newProveedorForm, direccion: event.target.value })}
                  placeholder="Dirección del proveedor"
                />
                <FormInput
                  label="Teléfono"
                  value={newProveedorForm.telefono}
                  onChange={(event) => setNewProveedorForm({ ...newProveedorForm, telefono: event.target.value })}
                  placeholder="Opcional"
                />
                <div className="flex justify-end">
                  <Button type="submit" variant="secondary">Agregar proveedor</Button>
                </div>
              </form>
            </div>
          </div>

          <div className="rounded-lg border border-gray-200 bg-white shadow-sm">
            <div className="border-b border-gray-200 px-6 py-4">
              <h3 className="text-lg font-bold text-gray-800">Listado de Items</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead className="bg-gray-50 text-left text-gray-600">
                  <tr>
                    <th className="px-6 py-3 font-semibold">Código</th>
                    <th className="px-6 py-3 font-semibold">Nombre</th>
                    <th className="px-6 py-3 font-semibold">Proveedor</th>
                    <th className="px-6 py-3 font-semibold">Precio</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredArticulos.map((articulo) => (
                    <tr key={articulo.id} className="border-t border-gray-100">
                      <td className="px-6 py-4 font-mono text-gray-700">{articulo.codigo || '-'}</td>
                      <td className="px-6 py-4 font-semibold text-gray-800">{articulo.nombre}</td>
                      <td className="px-6 py-4 text-gray-600">{articulo.proveedor_nombre || 'Sin proveedor'}</td>
                      <td className="px-6 py-4 text-gray-700">$ {Number(articulo.precio || 0).toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {filteredArticulos.length === 0 && (
                <div className="px-6 py-8 text-center text-sm text-gray-500">
                  No hay items para el filtro seleccionado.
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {view === 'crear' && (
        <div className="max-w-2xl">
          <h2 className="mb-6 text-2xl font-bold text-gray-800">Crear Nuevo Pedido</h2>
          <form onSubmit={handleCreatePedido} className="space-y-6 rounded-lg bg-white p-8 shadow-md">
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
              <FormInput
                label="Cliente"
                value={newPedido.cliente}
                onChange={(event) => {
                  setNewPedido({ ...newPedido, cliente: event.target.value })
                  setNewClienteForm((prev) => ({ ...prev, nombre: event.target.value }))
                }}
                placeholder="Escribe o selecciona un cliente"
                required
              />
              <div className="md:col-span-2 -mt-2 flex justify-end">
                <button
                  type="button"
                  onClick={() => {
                    setNewClienteForm({
                      nombre: newPedido.cliente,
                      direccion: newPedido.direccion,
                      telefono: ''
                    })
                    setShowClientePicker((prev) => !prev)
                  }}
                  className="rounded-lg border border-blue-200 bg-blue-50 px-4 py-2 text-sm font-semibold text-blue-700 hover:bg-blue-100"
                >
                  {showClientePicker ? 'Cerrar clientes' : 'Elegir o crear cliente'}
                </button>
              </div>

              {showClientePicker && (
                <div className="md:col-span-2 rounded-xl border border-gray-200 bg-gray-50 p-5 shadow-sm">
                  <div className="mb-4 flex items-center justify-between gap-4">
                    <div>
                      <h3 className="text-sm font-semibold text-gray-800">Clientes existentes</h3>
                      <p className="text-sm text-gray-500">Selecciona uno o crea uno nuevo sin salir del pedido.</p>
                    </div>
                    <span className="text-xs font-semibold uppercase tracking-wide text-gray-400">
                      {filteredClientes.length} resultados
                    </span>
                  </div>

                  <div className="mb-5 max-h-48 space-y-2 overflow-y-auto rounded-lg bg-white p-3">
                    {filteredClientes.length > 0 ? (
                      filteredClientes.map((cliente) => (
                        <button
                          key={cliente.id}
                          type="button"
                          onClick={() => handleSelectCliente(cliente)}
                          className="flex w-full items-start justify-between rounded-lg border border-gray-200 px-4 py-3 text-left hover:border-blue-300 hover:bg-blue-50"
                        >
                          <div>
                            <p className="font-semibold text-gray-800">{cliente.nombre}</p>
                            <p className="text-sm text-gray-500">{cliente.direccion || 'Sin dirección'}</p>
                          </div>
                          <span className="text-sm font-semibold text-blue-600">Usar</span>
                        </button>
                      ))
                    ) : (
                      <div className="rounded-lg border border-dashed border-gray-200 px-4 py-6 text-center text-sm text-gray-500">
                        No hay coincidencias. Puedes crear el cliente ahora mismo.
                      </div>
                    )}
                  </div>

                  <div className="space-y-4 rounded-lg border border-gray-200 bg-white p-4">
                    <h4 className="text-sm font-semibold text-gray-800">Alta rápida de cliente</h4>
                    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                      <FormInput
                        label="Nombre del cliente"
                        value={newClienteForm.nombre}
                        onChange={(event) => setNewClienteForm({ ...newClienteForm, nombre: event.target.value })}
                        required
                      />
                      <FormInput
                        label="Teléfono"
                        value={newClienteForm.telefono}
                        onChange={(event) => setNewClienteForm({ ...newClienteForm, telefono: event.target.value })}
                        placeholder="Opcional"
                      />
                    </div>
                    <FormInput
                      label="Dirección del cliente"
                      value={newClienteForm.direccion}
                      onChange={(event) => setNewClienteForm({ ...newClienteForm, direccion: event.target.value })}
                      placeholder="Opcional"
                    />
                    <div className="flex justify-end">
                      <Button type="button" variant="primary" onClick={handleCreateCliente}>
                        Crear cliente
                      </Button>
                    </div>
                  </div>
                </div>
              )}

              <FormInput
                label="Dirección completa"
                value={newPedido.direccion}
                onChange={(event) => setNewPedido({ ...newPedido, direccion: event.target.value })}
                placeholder="Ej: Av. Siempre Viva 742, Montevideo"
                required
              />
            </div>

            <div className="rounded-lg border border-gray-200 p-5">
              <div className="mb-4 flex items-center justify-between gap-4">
                <div>
                  <h3 className="text-sm font-semibold text-gray-800">Levantado</h3>
                  <p className="text-sm text-gray-500">Indica qué artículos se levantan en mostrador.</p>
                </div>
                <label className="flex items-center gap-3 text-sm font-semibold text-gray-700">
                  <input
                    type="checkbox"
                    checked={newPedido.sin_levantado_mostrador}
                    onChange={(event) => setNewPedido({
                      ...newPedido,
                      sin_levantado_mostrador: event.target.checked,
                      levantado: event.target.checked ? 'sin_mostrador' : 'con_mostrador',
                      levantado_en_mostrador: event.target.checked ? '' : newPedido.levantado_en_mostrador
                    })}
                    className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  Sin levantado en mostrador
                </label>
              </div>

              <FormInput
                label="Levantado en mostrador"
                value={newPedido.levantado_en_mostrador}
                onChange={(event) => setNewPedido({ ...newPedido, levantado_en_mostrador: event.target.value })}
                placeholder="Ej: Cemento, arena y pintura"
                required={!newPedido.sin_levantado_mostrador}
                disabled={newPedido.sin_levantado_mostrador}
              />

              {newPedido.sin_levantado_mostrador && (
                <p className="mt-3 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">
                  Todos los artículos del pedido se considerarán de encargue.
                </p>
              )}
            </div>

            <div className="rounded-lg border border-gray-200 p-5">
              <div className="mb-4 flex items-center justify-between gap-4">
                <div>
                  <h3 className="text-sm font-semibold text-gray-800">Ubicación</h3>
                  <p className="text-sm text-gray-500">Se muestra una vista previa en Google Maps usando la dirección completa.</p>
                </div>
                {newPedido.direccion.trim() && (
                  <a
                    href={`https://www.google.com/maps/search/?api=1&query=${mapQuery}`}
                    target="_blank"
                    rel="noreferrer"
                    className="text-sm font-semibold text-blue-600 hover:text-blue-700"
                  >
                    Abrir en Google Maps
                  </a>
                )}
              </div>

              {newPedido.direccion.trim() ? (
                <iframe
                  title="Vista previa de ubicación"
                  src={`https://www.google.com/maps?q=${mapQuery}&output=embed`}
                  className="h-72 w-full rounded-lg border-0"
                  loading="lazy"
                  referrerPolicy="no-referrer-when-downgrade"
                ></iframe>
              ) : (
                <div className="flex h-40 items-center justify-center rounded-lg bg-gray-50 text-sm text-gray-500">
                  Escribe una dirección completa para ver el mapa.
                </div>
              )}
            </div>

            <div>
              <label className="mb-4 block text-sm font-semibold text-gray-700">Items</label>
              <div className="space-y-4">
                {newPedido.items.map((item, index) => (
                  <div key={`${index}-${item.codigo}-${item.nombre}`} className="rounded-lg border border-gray-200 p-4">
                    <div className="grid grid-cols-1 gap-3 md:grid-cols-12">
                      <div className="md:col-span-2">
                        <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-gray-500">Código</label>
                        <input
                          type="text"
                          placeholder="ART-001"
                          value={item.codigo}
                          onFocus={() => setActiveItemIndex(index)}
                          onChange={(event) => updateItem(index, { codigo: event.target.value })}
                          className="w-full rounded-lg border-2 border-gray-200 px-4 py-2 focus:border-blue-500 focus:outline-none"
                        />
                      </div>
                      <div className="md:col-span-5">
                        <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-gray-500">Nombre</label>
                        <input
                          type="text"
                          placeholder="Nombre del producto"
                          value={item.nombre}
                          onFocus={() => setActiveItemIndex(index)}
                          onChange={(event) => updateItem(index, { nombre: event.target.value })}
                          className="w-full rounded-lg border-2 border-gray-200 px-4 py-2 focus:border-blue-500 focus:outline-none"
                        />
                      </div>
                      <div className="md:col-span-2">
                        <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-gray-500">Cantidad</label>
                        <input
                          type="number"
                          min="1"
                          placeholder="Cantidad"
                          value={item.cantidad}
                          onChange={(event) => updateItem(index, { cantidad: event.target.value })}
                          className="w-full rounded-lg border-2 border-gray-200 px-4 py-2 focus:border-blue-500 focus:outline-none"
                        />
                      </div>
                      <div className="md:col-span-3">
                        <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-gray-500">Precio</label>
                        <input
                          type="number"
                          min="0"
                          step="0.01"
                          placeholder="0.00"
                          value={item.precio}
                          onChange={(event) => updateItem(index, { precio: event.target.value })}
                          className="w-full rounded-lg border-2 border-gray-200 px-4 py-2 focus:border-blue-500 focus:outline-none"
                        />
                      </div>
                    </div>

                    {activeItemIndex === index && (item.codigo.trim() || item.nombre.trim()) && (
                      <div className="mt-3 rounded-lg border border-blue-100 bg-blue-50 p-2">
                        <p className="mb-2 px-2 text-xs font-semibold uppercase tracking-wide text-blue-600">Sugerencias</p>
                        <div className="max-h-40 space-y-1 overflow-y-auto">
                          {articulos
                            .filter((articulo) => {
                              const query = `${item.codigo} ${item.nombre}`.trim().toLowerCase()
                              return (
                                articulo.codigo?.toLowerCase().includes(query) ||
                                articulo.nombre.toLowerCase().includes(query)
                              )
                            })
                            .slice(0, 6)
                            .map((articulo) => (
                              <button
                                key={articulo.id}
                                type="button"
                                onMouseDown={() => selectArticulo(index, articulo)}
                                className="flex w-full items-center justify-between rounded-lg bg-white px-3 py-2 text-left hover:bg-blue-100"
                              >
                                <div>
                                  <p className="font-semibold text-gray-800">{articulo.nombre}</p>
                                  <p className="text-xs text-gray-500">{articulo.codigo || 'Sin código'}</p>
                                </div>
                                <span className="text-sm font-semibold text-blue-700">$ {Number(articulo.precio || 0).toFixed(2)}</span>
                              </button>
                            ))}
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>

              <div className="mt-4 flex justify-end">
                <Button type="button" variant="secondary" onClick={addItemRow}>
                  + Agregar item
                </Button>
              </div>
            </div>

            <div className="flex gap-4">
              <Button type="submit" variant="primary" fullWidth>
                Crear Pedido
              </Button>
              <Button type="button" variant="secondary" fullWidth onClick={() => onViewChange('pedidos')}>
                Cancelar
              </Button>
            </div>
          </form>
        </div>
      )}

      {view === 'conductores' && (
        <div>
          <h2 className="mb-6 text-2xl font-bold text-gray-800">Conductores</h2>
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
            {conductores.map((conductor) => {
              const pedidosAsignados = pedidos.filter((pedido) => pedido.conductor_id === conductor.id)
              const entregados = pedidosAsignados.filter((pedido) => pedido.estado === 'entregado').length

              return (
                <div key={conductor.id} className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
                  <div className="mb-4 flex items-center gap-4">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 font-bold text-blue-700">
                      {conductor.nombre?.[0]}
                    </div>
                    <div>
                      <h3 className="text-lg font-bold text-gray-800">{conductor.nombre}</h3>
                      <p className="text-sm text-gray-500">{conductor.email}</p>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4 rounded-lg bg-gray-50 p-4 text-sm">
                    <div>
                      <p className="text-gray-500">Pedidos asignados</p>
                      <p className="text-xl font-bold text-gray-800">{pedidosAsignados.length}</p>
                    </div>
                    <div>
                      <p className="text-gray-500">Entregados</p>
                      <p className="text-xl font-bold text-green-600">{entregados}</p>
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}

      {view === 'usuarios' && isAdmin && (
        <div className="space-y-8">
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <StatCard title="Total usuarios" value={usuarios.length} icon="👤" color="border-slate-500" />
            <StatCard title="Conductores" value={usuarios.filter((usuario) => usuario.rol === 'conductor').length} icon="🚚" color="border-blue-500" />
            <StatCard title="Creadores" value={usuarios.filter((usuario) => usuario.rol === 'creador_pedidos').length} icon="📝" color="border-emerald-500" />
          </div>

          <div className="grid grid-cols-1 gap-6 xl:grid-cols-[420px,1fr]">
            <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
              <h2 className="mb-2 text-2xl font-bold text-gray-800">Crear usuario</h2>
              <p className="mb-6 text-sm text-gray-500">Alta de administradores, conductores y usuarios que sólo crean pedidos.</p>

              <form onSubmit={handleCreateUsuario} className="space-y-4">
                <FormInput
                  label="Nombre"
                  value={newUsuarioForm.nombre}
                  onChange={(event) => setNewUsuarioForm({ ...newUsuarioForm, nombre: event.target.value })}
                  required
                />
                <FormInput
                  label="Email"
                  type="email"
                  value={newUsuarioForm.email}
                  onChange={(event) => setNewUsuarioForm({ ...newUsuarioForm, email: event.target.value })}
                  required
                />
                <FormInput
                  label="Contraseña"
                  type="password"
                  value={newUsuarioForm.password}
                  onChange={(event) => setNewUsuarioForm({ ...newUsuarioForm, password: event.target.value })}
                  required
                />
                <div className="space-y-2">
                  <label className="block text-sm font-semibold text-gray-700">Rol</label>
                  <select
                    value={newUsuarioForm.rol}
                    onChange={(event) => setNewUsuarioForm({ ...newUsuarioForm, rol: event.target.value })}
                    className="w-full rounded-lg border-2 border-gray-200 px-4 py-2 focus:border-blue-500 focus:outline-none"
                  >
                    <option value="conductor">Conductor</option>
                    <option value="creador_pedidos">Creador de pedidos</option>
                    <option value="admin">Administrador</option>
                  </select>
                </div>
                <div className="rounded-lg bg-slate-50 px-4 py-3 text-sm text-slate-600">
                  El rol administrador tiene acceso completo. El creador de pedidos puede cargar pedidos. El conductor queda para asignación y reparto.
                </div>
                <Button type="submit" variant="primary" fullWidth>
                  Crear usuario
                </Button>
              </form>
            </div>

            <div className="rounded-lg border border-gray-200 bg-white shadow-sm">
              <div className="border-b border-gray-200 px-6 py-4">
                <h3 className="text-lg font-bold text-gray-800">Usuarios registrados</h3>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead className="bg-gray-50 text-left text-gray-600">
                    <tr>
                      <th className="px-6 py-3 font-semibold">Nombre</th>
                      <th className="px-6 py-3 font-semibold">Email</th>
                      <th className="px-6 py-3 font-semibold">Rol</th>
                      <th className="px-6 py-3 font-semibold">Alta</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usuarios.map((usuarioItem) => (
                      <tr key={usuarioItem.id} className="border-t border-gray-100">
                        <td className="px-6 py-4 font-semibold text-gray-800">{usuarioItem.nombre}</td>
                        <td className="px-6 py-4 text-gray-600">{usuarioItem.email}</td>
                        <td className="px-6 py-4">
                          <span className="inline-flex rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700">
                            {roleLabels[usuarioItem.rol] || usuarioItem.rol}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-gray-500">
                          {usuarioItem.created_at ? new Date(usuarioItem.created_at).toLocaleDateString('es-UY') : '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {usuarios.length === 0 && (
                  <div className="px-6 py-8 text-center text-sm text-gray-500">
                    No hay usuarios registrados.
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {showDetalle && selectedPedido && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-lg bg-white p-8 shadow-2xl">
            <h2 className="mb-6 text-3xl font-bold text-gray-800">{selectedPedido.cliente}</h2>

            <div className="mb-6 grid grid-cols-1 gap-6 border-b pb-6 md:grid-cols-2">
              <div>
                <p className="mb-1 text-sm text-gray-600">Dirección</p>
                <p className="text-lg font-semibold text-gray-800">{selectedPedido.direccion}</p>
              </div>
              <div>
                <p className="mb-1 text-sm text-gray-600">Estado</p>
                <StateBadge estado={selectedPedido.estado} />
              </div>
              <div>
                <p className="mb-1 text-sm text-gray-600">Conductor</p>
                <p className="text-lg font-semibold text-gray-800">{selectedPedido.conductor_nombre || 'No asignado'}</p>
              </div>
              <div>
                <p className="mb-1 text-sm text-gray-600">Coordenadas</p>
                <p className="text-sm font-mono text-gray-800">
                  {selectedPedido.lat != null && selectedPedido.lng != null
                    ? `${Number(selectedPedido.lat).toFixed(6)}, ${Number(selectedPedido.lng).toFixed(6)}`
                    : 'Sin coordenadas guardadas'}
                </p>
              </div>
            </div>

            <div className="mb-6 rounded-lg border border-gray-200 p-5">
              <h3 className="mb-3 text-lg font-bold text-gray-800">Levantado</h3>
              {selectedPedido.sin_levantado_mostrador ? (
                <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">
                  Sin levantado en mostrador. Todos los artículos son de encargue.
                </p>
              ) : (
                <div>
                  <p className="mb-1 text-sm text-gray-600">Levantado en mostrador</p>
                  <p className="font-semibold text-gray-800">{selectedPedido.levantado_en_mostrador || 'No especificado'}</p>
                </div>
              )}
            </div>

            <div className="mb-6 rounded-lg border border-gray-200 p-5">
              <div className="mb-4 flex items-center justify-between gap-4">
                <h3 className="text-lg font-bold text-gray-800">Ubicación</h3>
                <a
                  href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(selectedPedido.direccion || '')}`}
                  target="_blank"
                  rel="noreferrer"
                  className="text-sm font-semibold text-blue-600 hover:text-blue-700"
                >
                  Abrir en Google Maps
                </a>
              </div>
              <iframe
                title={`Mapa de ${selectedPedido.cliente}`}
                src={`https://www.google.com/maps?q=${encodeURIComponent(selectedPedido.direccion || '')}&output=embed`}
                className="h-72 w-full rounded-lg border-0"
                loading="lazy"
                referrerPolicy="no-referrer-when-downgrade"
              ></iframe>
            </div>

            <div className="mb-6">
              <h3 className="mb-4 text-lg font-bold text-gray-800">Items</h3>
              <div className="overflow-hidden rounded-lg bg-gray-50">
                {selectedPedido.items?.map((item) => (
                  <div key={item.id} className="grid grid-cols-4 gap-4 border-b p-4 last:border-0">
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-500">Código</p>
                      <p className="font-semibold text-gray-800">{item.codigo || '-'}</p>
                    </div>
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-500">Nombre</p>
                      <p className="font-semibold text-gray-800">{item.nombre}</p>
                    </div>
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-500">Cantidad</p>
                      <p className="text-gray-700">{item.cantidad}</p>
                    </div>
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-500">Precio</p>
                      <p className="text-gray-700">$ {Number(item.precio || 0).toFixed(2)}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex gap-3">
              {isAdmin && selectedPedido.estado === 'pendiente' && (
                <Button
                  type="button"
                  variant="success"
                  fullWidth
                  onClick={() => {
                    setShowDetalle(false)
                    openAsignar(selectedPedido)
                  }}
                >
                  Asignar Conductor
                </Button>
              )}
              {isConductor && selectedPedido.conductor_id === user?.id && selectedPedido.estado === 'asignado' && (
                <Button
                  type="button"
                  variant="success"
                  fullWidth
                  onClick={() => handleAceptarPedido(selectedPedido)}
                >
                  Aceptar Pedido
                </Button>
              )}
              <Button type="button" variant="secondary" fullWidth onClick={() => setShowDetalle(false)}>
                Cerrar
              </Button>
            </div>
          </div>
        </div>
      )}

      {showAsignar && selectedPedido && isAdmin && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-2xl">
            <h2 className="mb-6 text-2xl font-bold text-gray-800">Asignar Conductor</h2>
            <form onSubmit={handleAsignar} className="space-y-4">
              <div className="space-y-2">
                <label className="block text-sm font-semibold text-gray-700">Conductor</label>
                <select
                  value={asignarForm.conductor_id}
                  onChange={(event) => setAsignarForm({ conductor_id: event.target.value })}
                  required
                  className="w-full rounded-lg border-2 border-gray-200 px-4 py-2 focus:border-blue-500 focus:outline-none"
                >
                  <option value="">Seleccionar...</option>
                  {conductores.map((conductor) => (
                    <option key={conductor.id} value={conductor.id}>
                      {conductor.nombre} ({conductor.email})
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex gap-3">
                <Button type="submit" variant="primary" fullWidth>
                  Asignar
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  fullWidth
                  onClick={() => {
                    setShowAsignar(false)
                    setAsignarForm({ conductor_id: '' })
                  }}
                >
                  Cancelar
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
