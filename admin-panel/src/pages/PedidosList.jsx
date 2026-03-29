import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
import PedidoCard from '../components/PedidoCard'
import PedidosFilters from '../components/PedidosFilters'
import { usePedidos } from '../hooks/usePedidos'

export default function PedidosList() {
  const navigate = useNavigate()
  const { filteredPedidos, filterPedidos } = usePedidos()
  const [displayPedidos, setDisplayPedidos] = useState(filteredPedidos)

  const handleFilterChange = ({ search, estado }) => {
    const filtered = filterPedidos(search, estado)
    setDisplayPedidos(filtered)
  }

  const handleAssignClick = (pedidoId) => {
    navigate(`/asignar-conductor/${pedidoId}`)
  }

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Title */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Gestión de Pedidos</h1>
        <p className="text-gray-600">Administra todos los pedidos de la barraca</p>
      </div>

      {/* Filters */}
      <PedidosFilters onFilterChange={handleFilterChange} />

      {/* Pedidos Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {displayPedidos.length > 0 ? (
          displayPedidos.map((pedido) => (
            <PedidoCard
              key={pedido.id}
              pedido={pedido}
              onAssignClick={handleAssignClick}
            />
          ))
        ) : (
          <div className="col-span-full text-center py-12">
            <p className="text-gray-500 text-lg mb-4">No se encontraron pedidos</p>
            <button className="px-6 py-2 bg-primary text-white rounded-lg hover:bg-orange-700 transition font-medium">
              Crear Pedido
            </button>
          </div>
        )}
      </div>

      {/* Pagination */}
      {displayPedidos.length > 0 && (
        <div className="flex items-center justify-center gap-2 mt-8">
          <button className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-100 transition">
            ← Anterior
          </button>
          <div className="flex gap-1">
            {[1, 2, 3].map((page) => (
              <button
                key={page}
                className={`w-10 h-10 rounded-lg transition ${
                  page === 1
                    ? 'bg-primary text-white'
                    : 'border border-gray-300 hover:bg-gray-100'
                }`}
              >
                {page}
              </button>
            ))}
          </div>
          <button className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-100 transition">
            Siguiente →
          </button>
        </div>
      )}
    </div>
  )
}
