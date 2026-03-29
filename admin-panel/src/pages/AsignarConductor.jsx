import { useParams, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { ArrowLeft, CheckCircle } from 'lucide-react'
import MapComponent from '../components/MapComponent'
import { mockConductores, mockPedidos } from '../data/mockData'

export default function AsignarConductor() {
  const { pedidoId } = useParams()
  const navigate = useNavigate()
  const [conductorSeleccionado, setConductorSeleccionado] = useState(null)
  const [asignado, setAsignado] = useState(false)

  const pedido = mockPedidos.find(p => p.id === pedidoId)

  if (!pedido) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500 text-lg mb-4">Pedido no encontrado</p>
        <button
          onClick={() => navigate('/pedidos')}
          className="px-6 py-2 bg-primary text-white rounded-lg hover:bg-orange-700 transition font-medium"
        >
          Volver a Pedidos
        </button>
      </div>
    )
  }

  const conductoresDisponibles = mockConductores.filter(c => c.estado === 'disponible')

  const handleAsignar = () => {
    if (!conductorSeleccionado) return
    setAsignado(true)
    setTimeout(() => {
      navigate('/pedidos')
    }, 2000)
  }

  if (asignado) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <CheckCircle size={32} className="text-green-600" />
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            ¡Conductor Asignado!
          </h2>
          <p className="text-gray-600">
            El pedido ha sido asignado exitosamente. Redirigiendo...
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-5xl mx-auto animate-fadeIn">
      {/* Back button */}
      <button
        onClick={() => navigate('/pedidos')}
        className="flex items-center gap-2 text-primary hover:text-orange-700 transition font-medium mb-6"
      >
        <ArrowLeft size={20} />
        Volver a Pedidos
      </button>

      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          Asignar Conductor - Pedido #{pedidoId}
        </h1>
        <p className="text-gray-600">Selecciona un conductor para realizar la entrega</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Detalle del pedido */}
        <div className="lg:col-span-1 space-y-6">
          {/* Pedido Info */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Detalles del Pedido</h2>

            <div className="space-y-4">
              <div>
                <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide mb-1">
                  Cliente
                </p>
                <p className="text-gray-900 font-medium">{pedido.cliente}</p>
              </div>

              <div>
                <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide mb-1">
                  Teléfono
                </p>
                <p className="text-gray-900">{pedido.telefono}</p>
              </div>

              <div>
                <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide mb-1">
                  Monto Total
                </p>
                <p className="text-2xl font-bold text-primary">
                  ${pedido.monto.toFixed(2)}
                </p>
              </div>

              <div>
                <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide mb-1">
                  Artículos
                </p>
                <ul className="text-sm text-gray-700 space-y-1">
                  {pedido.articulos.map((item, idx) => (
                    <li key={idx} className="flex justify-between">
                      <span>{item.nombre}</span>
                      <span className="text-gray-600">x{item.cantidad}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        </div>

        {/* Seleccionar Conductor */}
        <div className="lg:col-span-2 space-y-6">
          {/* Mapa */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Ubicación de Entrega</h2>
            <MapComponent
              direccion={pedido.direccion}
              coordenadas={pedido.coordenadas}
            />
          </div>

          {/* Conductores disponibles */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              Conductores Disponibles ({conductoresDisponibles.length})
            </h2>

            {conductoresDisponibles.length > 0 ? (
              <div className="space-y-3">
                {conductoresDisponibles.map(conductor => (
                  <div
                    key={conductor.id}
                    onClick={() => setConductorSeleccionado(conductor.id)}
                    className={`p-4 rounded-lg border-2 cursor-pointer transition ${
                      conductorSeleccionado === conductor.id
                        ? 'border-primary bg-orange-50'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <div className="flex items-start justify-between mb-3">
                      <div>
                        <p className="font-semibold text-gray-900">
                          {conductor.nombre}
                        </p>
                        <p className="text-sm text-gray-600">{conductor.modelo}</p>
                      </div>
                      <span className="px-3 py-1 bg-green-100 text-green-800 text-xs rounded-full font-medium">
                        Disponible
                      </span>
                    </div>

                    <div className="grid grid-cols-3 gap-4 pt-3 border-t border-gray-200">
                      <div>
                        <p className="text-xs text-gray-600 font-medium mb-1">Placa</p>
                        <p className="font-semibold text-gray-900">{conductor.placa}</p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-600 font-medium mb-1">Capacidad</p>
                        <p className="font-semibold text-gray-900">{conductor.capacidad}</p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-600 font-medium mb-1">Teléfono</p>
                        <p className="font-semibold text-gray-900 text-sm">{conductor.telefono}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <p className="text-gray-500 mb-4">No hay conductores disponibles</p>
                <button className="px-6 py-2 bg-primary text-white rounded-lg hover:bg-orange-700 transition font-medium">
                  Notificar Conductor
                </button>
              </div>
            )}

            {/* Action buttons */}
            {conductoresDisponibles.length > 0 && (
              <div className="flex gap-4 mt-6 pt-6 border-t border-gray-200">
                <button
                  onClick={() => navigate('/pedidos')}
                  className="flex-1 px-6 py-3 border border-gray-300 rounded-lg hover:bg-gray-100 transition font-medium text-gray-700"
                >
                  Cancelar
                </button>
                <button
                  onClick={handleAsignar}
                  disabled={!conductorSeleccionado}
                  className="flex-1 px-6 py-3 bg-primary text-white rounded-lg hover:bg-orange-700 transition font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Asignar Conductor
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
