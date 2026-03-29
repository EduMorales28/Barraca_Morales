import { Search, Filter, Download, Plus } from 'lucide-react'
import { useState } from 'react'

export default function PedidosFilters({ onFilterChange }) {
  const [search, setSearch] = useState('')
  const [estado, setEstado] = useState('todos')

  const handleSearchChange = (e) => {
    setSearch(e.target.value)
    onFilterChange?.({ search: e.target.value, estado })
  }

  const handleEstadoChange = (e) => {
    setEstado(e.target.value)
    onFilterChange?.({ search, estado: e.target.value })
  }

  return (
    <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
      <div className="flex flex-col gap-4">
        {/* Search and Status */}
        <div className="flex flex-col md:flex-row gap-4">
          {/* Search */}
          <div className="flex-1 relative">
            <Search size={20} className="absolute left-3 top-3 text-gray-400" />
            <input
              type="text"
              placeholder="Buscar por cliente, dirección o ID pedido..."
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
              value={search}
              onChange={handleSearchChange}
            />
          </div>

          {/* Status Filter */}
          <select
            value={estado}
            onChange={handleEstadoChange}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
          >
            <option value="todos">Todos los estados</option>
            <option value="pendiente">Pendientes</option>
            <option value="en_reparto">En Reparto</option>
            <option value="entregado">Entregados</option>
          </select>
        </div>

        {/* Action buttons */}
        <div className="flex gap-3 flex-wrap">
          <button className="flex items-center gap-2 px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition font-medium">
            <Filter size={18} />
            Más Filtros
          </button>
          <button className="flex items-center gap-2 px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition font-medium">
            <Download size={18} />
            Descargar
          </button>
          <button className="ml-auto flex items-center gap-2 px-4 py-2 bg-primary text-white hover:bg-orange-700 rounded-lg transition font-medium">
            <Plus size={18} />
            Nuevo Pedido
          </button>
        </div>
      </div>
    </div>
  )
}
