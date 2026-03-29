import { MapPin, Package, Truck, Clock } from 'lucide-react'

const statusConfig = {
  pendiente: { bg: 'bg-yellow-100', text: 'text-yellow-800', label: 'Pendiente' },
  en_reparto: { bg: 'bg-blue-100', text: 'text-blue-800', label: 'En Reparto' },
  entregado: { bg: 'bg-green-100', text: 'text-green-800', label: 'Entregado' },
}

export default function PedidoCard({ pedido, onAssignClick }) {
  const status = statusConfig[pedido.estado] || statusConfig.pendiente

  return (
    <div className="bg-white rounded-lg shadow-sm hover:shadow-md transition border border-gray-200 overflow-hidden">
      {/* Header */}
      <div className="flex items-start justify-between p-4 border-b border-gray-100">
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-2">
            <h3 className="font-semibold text-gray-900 text-lg">
              Pedido #{pedido.id}
            </h3>
            <span className={`px-3 py-1 rounded-full text-xs font-medium ${status.bg} ${status.text}`}>
              {status.label}
            </span>
          </div>
          <p className="text-sm text-gray-600">Cliente: {pedido.cliente}</p>
        </div>
        <div className="text-right">
          <p className="text-lg font-bold text-primary">
            ${pedido.monto.toFixed(2)}
          </p>
          <p className="text-xs text-gray-500">{pedido.articulos.length} artículos</p>
        </div>
      </div>

      {/* Content */}
      <div className="p-4 space-y-3">
        {/* Location */}
        <div className="flex items-start gap-3">
          <MapPin size={18} className="text-gray-400 mt-1 flex-shrink-0" />
          <div>
            <p className="text-sm font-medium text-gray-900">{pedido.direccion}</p>
            <p className="text-xs text-gray-500">{pedido.barrio}</p>
          </div>
        </div>

        {/* Items summary */}
        <div className="flex items-start gap-3 bg-gray-50 rounded p-3">
          <Package size={18} className="text-gray-400 mt-0.5 flex-shrink-0" />
          <div className="flex-1">
            <p className="text-xs font-medium text-gray-600 mb-1">Artículos:</p>
            <div className="space-y-1">
              {pedido.articulos.slice(0, 2).map((item, idx) => (
                <p key={idx} className="text-xs text-gray-700">
                  • {item.nombre} x{item.cantidad}
                </p>
              ))}
              {pedido.articulos.length > 2 && (
                <p className="text-xs text-gray-500">
                  +{pedido.articulos.length - 2} más
                </p>
              )}
            </div>
          </div>
        </div>

        {/* Conductor info */}
        <div className="flex items-center gap-2">
          <Truck size={18} className="text-gray-400" />
          {pedido.conductor ? (
            <div className="flex-1">
              <p className="text-xs font-medium text-gray-900">{pedido.conductor.nombre}</p>
              <p className="text-xs text-gray-500">Placa: {pedido.conductor.placa}</p>
            </div>
          ) : (
            <p className="text-xs text-gray-500 italic">Sin asignar</p>
          )}
        </div>

        {/* Date */}
        <div className="flex items-center gap-2 text-xs text-gray-500">
          <Clock size={16} />
          <span>{new Date(pedido.fecha).toLocaleDateString('es-AR')}</span>
        </div>
      </div>

      {/* Footer */}
      <div className="bg-gray-50 px-4 py-3 flex items-center justify-between">
        <button className="text-sm font-medium text-primary hover:text-orange-700 transition">
          Ver Detalle
        </button>
        {!pedido.conductor && (
          <button
            onClick={() => onAssignClick?.(pedido.id)}
            className="text-sm font-medium bg-primary text-white px-4 py-2 rounded-lg hover:bg-orange-700 transition"
          >
            Asignar Conductor
          </button>
        )}
      </div>
    </div>
  )
}
