import { useState } from 'react'
import { mockPedidos } from '../data/mockData'

export function usePedidos() {
  const [pedidos, setPedidos] = useState(mockPedidos)
  const [filteredPedidos, setFilteredPedidos] = useState(mockPedidos)

  const filterPedidos = (search, estado) => {
    let result = pedidos

    // Filter by estado
    if (estado !== 'todos') {
      result = result.filter(p => p.estado === estado)
    }

    // Filter by search
    if (search) {
      const searchLower = search.toLowerCase()
      result = result.filter(p =>
        p.cliente.toLowerCase().includes(searchLower) ||
        p.direccion.toLowerCase().includes(searchLower) ||
        p.id.toLowerCase().includes(searchLower) ||
        p.email.toLowerCase().includes(searchLower)
      )
    }

    setFilteredPedidos(result)
    return result
  }

  const updatePedido = (pedidoId, updates) => {
    const updated = pedidos.map(p =>
      p.id === pedidoId ? { ...p, ...updates } : p
    )
    setPedidos(updated)
    setFilteredPedidos(updated)
  }

  return {
    pedidos,
    filteredPedidos,
    filterPedidos,
    updatePedido,
  }
}
