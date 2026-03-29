import { Link, useLocation } from 'react-router-dom'
import {
  LayoutDashboard,
  Package,
  Plus,
  Truck,
  Settings,
  ChevronRight,
} from 'lucide-react'

const menuItems = [
  {
    label: 'Dashboard',
    href: '/',
    icon: LayoutDashboard,
  },
  {
    label: 'Pedidos',
    href: '/pedidos',
    icon: Package,
  },
  {
    label: 'Nuevo Pedido',
    href: '/nuevo-pedido',
    icon: Plus,
  },
  {
    label: 'Conductores',
    href: '/conductores',
    icon: Truck,
  },
  {
    label: 'Configuración',
    href: '/configuracion',
    icon: Settings,
  },
]

export default function Sidebar({ isOpen }) {
  const location = useLocation()

  return (
    <aside
      className={`${
        isOpen ? 'w-64' : 'w-20'
      } bg-secondary text-white transition-all duration-300 flex flex-col shadow-lg`}
    >
      {/* Logo */}
      <div className={`h-16 flex items-center ${isOpen ? 'px-4' : 'px-2'} border-b border-gray-700`}>
        <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center font-bold">
          B
        </div>
        {isOpen && <span className="ml-3 font-bold text-lg">Barraca</span>}
      </div>

      {/* Menu items */}
      <nav className="flex-1 px-3 py-6 space-y-2">
        {menuItems.map((item) => {
          const Icon = item.icon
          const isActive = location.pathname === item.href

          return (
            <Link
              key={item.href}
              to={item.href}
              className={`flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                isActive
                  ? 'bg-primary text-white'
                  : 'text-gray-300 hover:bg-gray-700'
              } ${!isOpen && 'justify-center'}`}
              title={!isOpen ? item.label : ''}
            >
              <Icon size={20} />
              {isOpen && <span className="text-sm font-medium">{item.label}</span>}
              {isOpen && isActive && (
                <ChevronRight size={16} className="ml-auto" />
              )}
            </Link>
          )
        })}
      </nav>

      {/* Footer info */}
      {isOpen && (
        <div className="px-4 py-4 border-t border-gray-700 text-xs text-gray-400">
          <p className="text-center">v1.0.0</p>
          <p className="text-center mt-1">© 2026 Barraca</p>
        </div>
      )}
    </aside>
  )
}
