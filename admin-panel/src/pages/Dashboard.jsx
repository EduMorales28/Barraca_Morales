import { BarChart3, TrendingUp, Users, Package } from 'lucide-react'
import DashboardStats from '../components/DashboardStats'
import { mockPedidos, mockConductores } from '../data/mockData'

export default function Dashboard() {
  const stats = {
    totalPedidos: mockPedidos.length,
    pendientes: mockPedidos.filter(p => p.estado === 'pendiente').length,
    enReparto: mockPedidos.filter(p => p.estado === 'en_reparto').length,
    entregados: mockPedidos.filter(p => p.estado === 'entregado').length,
  }

  const recentPedidos = mockPedidos.slice(0, 5)

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Title */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Dashboard</h1>
        <p className="text-gray-600">Bienvenido al panel de control de Barraca</p>
      </div>

      {/* Stats */}
      <DashboardStats stats={stats} />

      {/* Main content grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Orders */}
        <div className="lg:col-span-2 bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
              <Package size={20} className="text-primary" />
              Pedidos Recientes
            </h2>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200">
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                    Pedido
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                    Cliente
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                    Monto
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                    Estado
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                    Fecha
                  </th>
                </tr>
              </thead>
              <tbody>
                {recentPedidos.map((pedido) => {
                  const statusColors = {
                    pendiente: 'bg-yellow-100 text-yellow-800',
                    en_reparto: 'bg-blue-100 text-blue-800',
                    entregado: 'bg-green-100 text-green-800',
                  }

                  return (
                    <tr
                      key={pedido.id}
                      className="border-b border-gray-200 hover:bg-gray-50 transition"
                    >
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="font-semibold text-gray-900">{pedido.id}</span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-gray-700">{pedido.cliente}</span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="font-semibold text-gray-900">
                          ${pedido.monto.toFixed(2)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`px-3 py-1 rounded-full text-xs font-semibold ${
                            statusColors[pedido.estado]
                          }`}
                        >
                          {pedido.estado === 'en_reparto'
                            ? 'En Reparto'
                            : pedido.estado === 'pendiente'
                            ? 'Pendiente'
                            : 'Entregado'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                        {new Date(pedido.fecha).toLocaleDateString('es-AR')}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>

          <div className="px-6 py-4 border-t border-gray-200 text-center">
            <a href="/pedidos" className="text-primary font-medium hover:text-orange-700 transition">
              Ver todos los pedidos →
            </a>
          </div>
        </div>

        {/* Conductores Activos */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
              <Users size={20} className="text-primary" />
              Conductores Activos
            </h2>
          </div>

          <div className="space-y-3 p-6">
            {mockConductores.slice(0, 4).map((conductor) => (
              <div key={conductor.id} className="p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition">
                <div className="flex items-start justify-between mb-2">
                  <div>
                    <p className="font-semibold text-gray-900 text-sm">
                      {conductor.nombre}
                    </p>
                    <p className="text-xs text-gray-600">Placa: {conductor.placa}</p>
                  </div>
                  <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded-full font-medium">
                    {conductor.estado}
                  </span>
                </div>
                <div className="flex items-center justify-between text-xs text-gray-600">
                  <span>{conductor.modelo}</span>
                  <span className="font-medium">
                    {conductor.pedidosActuales} pedidos
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-lg border border-gray-200 text-center">
          <BarChart3 size={24} className="text-primary mx-auto mb-2" />
          <p className="text-2xl font-bold text-gray-900">82%</p>
          <p className="text-xs text-gray-600">Tasa de entrega</p>
        </div>
        <div className="bg-white p-4 rounded-lg border border-gray-200 text-center">
          <TrendingUp size={24} className="text-green-600 mx-auto mb-2" />
          <p className="text-2xl font-bold text-gray-900">+12%</p>
          <p className="text-xs text-gray-600">Crecimiento mes</p>
        </div>
        <div className="bg-white p-4 rounded-lg border border-gray-200 text-center">
          <Package size={24} className="text-blue-600 mx-auto mb-2" />
          <p className="text-2xl font-bold text-gray-900">$45.2k</p>
          <p className="text-xs text-gray-600">Ingresos mes</p>
        </div>
        <div className="bg-white p-4 rounded-lg border border-gray-200 text-center">
          <Users size={24} className="text-purple-600 mx-auto mb-2" />
          <p className="text-2xl font-bold text-gray-900">256</p>
          <p className="text-xs text-gray-600">Clientes activos</p>
        </div>
      </div>
    </div>
  )
}
