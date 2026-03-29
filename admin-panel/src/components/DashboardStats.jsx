import { TrendingUp, Package, Clock, CheckCircle } from 'lucide-react'

export default function DashboardStats({ stats = {} }) {
  const defaultStats = {
    totalPedidos: 24,
    pendientes: 8,
    enReparto: 5,
    entregados: 11,
    ...stats,
  }

  const statItems = [
    {
      label: 'Total Pedidos',
      value: defaultStats.totalPedidos,
      icon: Package,
      color: 'bg-blue-100',
      textColor: 'text-blue-600',
      bgColor: 'bg-blue-50',
    },
    {
      label: 'Pendientes',
      value: defaultStats.pendientes,
      icon: Clock,
      color: 'bg-yellow-100',
      textColor: 'text-yellow-600',
      bgColor: 'bg-yellow-50',
    },
    {
      label: 'En Reparto',
      value: defaultStats.enReparto,
      icon: TrendingUp,
      color: 'bg-purple-100',
      textColor: 'text-purple-600',
      bgColor: 'bg-purple-50',
    },
    {
      label: 'Entregados',
      value: defaultStats.entregados,
      icon: CheckCircle,
      color: 'bg-green-100',
      textColor: 'text-green-600',
      bgColor: 'bg-green-50',
    },
  ]

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      {statItems.map((stat, idx) => {
        const Icon = stat.icon
        return (
          <div
            key={idx}
            className={`${stat.bgColor} rounded-lg p-6 border border-gray-200 hover:shadow-md transition`}
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">
                  {stat.label}
                </p>
                <p className="text-3xl font-bold text-gray-900">
                  {stat.value}
                </p>
              </div>
              <div className={`${stat.color} p-3 rounded-lg`}>
                <Icon size={24} className={stat.textColor} />
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}
