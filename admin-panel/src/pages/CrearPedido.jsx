import { useState } from 'react'
import { Plus, X } from 'lucide-react'
import { mockArticulos } from '../data/mockData'

export default function CrearPedido() {
  const [formData, setFormData] = useState({
    cliente: '',
    email: '',
    telefono: '',
    direccion: '',
    barrio: '',
    articulos: [],
  })

  const [articulosSeleccionados, setArticulosSeleccionados] = useState([])
  const [articuloActual, setArticuloActual] = useState({
    id: null,
    cantidad: 1,
  })

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }))
  }

  const agregarArticulo = () => {
    if (!articuloActual.id) return

    const articulo = mockArticulos.find(a => a.id === parseInt(articuloActual.id))
    const total = articulo.precio * articuloActual.cantidad

    const yaDebe = articulosSeleccionados.find(a => a.id === articuloActual.id)
    if (yaDebe) {
      setArticulosSeleccionados(prev =>
        prev.map(a =>
          a.id === articuloActual.id
            ? { ...a, cantidad: a.cantidad + articuloActual.cantidad, total: a.precio * (a.cantidad + articuloActual.cantidad) }
            : a
        )
      )
    } else {
      setArticulosSeleccionados(prev => [
        ...prev,
        {
          ...articulo,
          cantidad: articuloActual.cantidad,
          total,
        },
      ])
    }

    setArticuloActual({ id: null, cantidad: 1 })
  }

  const eliminarArticulo = (id) => {
    setArticulosSeleccionados(prev => prev.filter(a => a.id !== id))
  }

  const totalMonto = articulosSeleccionados.reduce((sum, a) => sum + a.total, 0)

  const handleSubmit = (e) => {
    e.preventDefault()
    // Create order logic here
    console.log({
      ...formData,
      articulos: articulosSeleccionados,
      monto: totalMonto,
    })
    alert('Pedido creado exitosamente!')
  }

  return (
    <div className="max-w-4xl mx-auto animate-fadeIn">
      {/* Title */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Crear Nuevo Pedido</h1>
        <p className="text-gray-600">Completa el formulario para crear un nuevo pedido de entrega</p>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Datos del Cliente */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Datos del Cliente</h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Nombre del Cliente *
              </label>
              <input
                type="text"
                name="cliente"
                value={formData.cliente}
                onChange={handleInputChange}
                placeholder="Juan García"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                placeholder="cliente@example.com"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Teléfono *
              </label>
              <input
                type="tel"
                name="telefono"
                value={formData.telefono}
                onChange={handleInputChange}
                placeholder="+54 9 11 2345-6789"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Barrio *
              </label>
              <input
                type="text"
                name="barrio"
                value={formData.barrio}
                onChange={handleInputChange}
                placeholder="Almagro"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
                required
              />
            </div>
          </div>

          <div className="mt-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Dirección de Entrega *
            </label>
            <input
              type="text"
              name="direccion"
              value={formData.direccion}
              onChange={handleInputChange}
              placeholder="Av. Rivadavia 1800"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
              required
            />
          </div>
        </div>

        {/* Articulos */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Artículos</h2>

          {/* Agregar articulo */}
          <div className="mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Seleccionar Artículo
                </label>
                <select
                  value={articuloActual.id || ''}
                  onChange={(e) =>
                    setArticuloActual(prev => ({
                      ...prev,
                      id: e.target.value ? parseInt(e.target.value) : null,
                    }))
                  }
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
                >
                  <option value="">Elegir artículo...</option>
                  {mockArticulos.map(art => (
                    <option key={art.id} value={art.id}>
                      {art.nombre} - ${art.precio}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Cantidad
                </label>
                <input
                  type="number"
                  min="1"
                  value={articuloActual.cantidad}
                  onChange={(e) =>
                    setArticuloActual(prev => ({
                      ...prev,
                      cantidad: parseInt(e.target.value) || 1,
                    }))
                  }
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>

              <div className="flex items-end">
                <button
                  type="button"
                  onClick={agregarArticulo}
                  className="w-full bg-primary text-white px-4 py-2 rounded-lg hover:bg-orange-700 transition font-medium flex items-center justify-center gap-2"
                >
                  <Plus size={18} />
                  Agregar
                </button>
              </div>
            </div>
          </div>

          {/* Lista de articulos */}
          {articulosSeleccionados.length > 0 ? (
            <div className="space-y-2">
              <div className="bg-gray-50 rounded-lg overflow-hidden">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200">
                      <th className="px-4 py-3 text-left font-semibold text-gray-700">
                        Artículo
                      </th>
                      <th className="px-4 py-3 text-right font-semibold text-gray-700">
                        Precio
                      </th>
                      <th className="px-4 py-3 text-center font-semibold text-gray-700">
                        Cantidad
                      </th>
                      <th className="px-4 py-3 text-right font-semibold text-gray-700">
                        Total
                      </th>
                      <th className="px-4 py-3 text-center font-semibold text-gray-700" />
                    </tr>
                  </thead>
                  <tbody>
                    {articulosSeleccionados.map(art => (
                      <tr key={art.id} className="border-b border-gray-200 hover:bg-white transition">
                        <td className="px-4 py-3">{art.nombre}</td>
                        <td className="px-4 py-3 text-right">${art.precio}</td>
                        <td className="px-4 py-3 text-center">{art.cantidad}</td>
                        <td className="px-4 py-3 text-right font-semibold">
                          ${art.total.toFixed(2)}
                        </td>
                        <td className="px-4 py-3 text-center">
                          <button
                            type="button"
                            onClick={() => eliminarArticulo(art.id)}
                            className="text-red-600 hover:text-red-800 transition"
                          >
                            <X size={18} />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Total */}
              <div className="flex justify-end">
                <div className="bg-primary text-white rounded-lg p-4 text-right">
                  <p className="text-sm font-medium mb-1">Monto Total:</p>
                  <p className="text-3xl font-bold">${totalMonto.toFixed(2)}</p>
                </div>
              </div>
            </div>
          ) : (
            <p className="text-center text-gray-500 py-8">No hay artículos agregados aún</p>
          )}
        </div>

        {/* Actions */}
        <div className="flex gap-4">
          <button
            type="button"
            className="flex-1 px-6 py-3 border border-gray-300 rounded-lg hover:bg-gray-100 transition font-medium text-gray-700"
          >
            Cancelar
          </button>
          <button
            type="submit"
            disabled={articulosSeleccionados.length === 0}
            className="flex-1 px-6 py-3 bg-primary text-white rounded-lg hover:bg-orange-700 transition font-medium disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Crear Pedido
          </button>
        </div>
      </form>
    </div>
  )
}
