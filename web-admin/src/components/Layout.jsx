import { useState } from 'react'

export default function Layout({ user, onLogout, currentView, onNavigate, children, notifications = [], onReadNotification }) {
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const isAdmin = user?.rol === 'admin'
  const canCreatePedidos = isAdmin || user?.rol === 'creador_pedidos'
  const unreadCount = notifications.filter((item) => !item.leida).length

  const menuItems = [
    { icon: '📊', label: 'Dashboard', id: 'dashboard' },
    { icon: '📦', label: 'Pedidos', id: 'pedidos' },
    ...(canCreatePedidos ? [{ icon: '➕', label: 'Crear Pedido', id: 'crear' }] : []),
    ...(isAdmin ? [{ icon: '🧾', label: 'Items', id: 'items' }] : []),
    ...(isAdmin ? [{ icon: '👥', label: 'Conductores', id: 'conductores' }] : []),
    ...(isAdmin ? [{ icon: '🔐', label: 'Usuarios', id: 'usuarios' }] : [])
  ]

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <div className={`${sidebarOpen ? 'w-64' : 'w-20'} bg-gradient-to-b from-slate-900 to-slate-800 text-white transition-all duration-300 flex flex-col fixed h-screen shadow-xl`}>
        {/* Logo */}
        <div className="p-6 border-b border-slate-700">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center font-bold text-lg">
              BM
            </div>
            {sidebarOpen && <div className="font-bold">Barraca Morales</div>}
          </div>
        </div>

        {/* Menu */}
        <nav className="flex-1 p-4 space-y-3">
          {menuItems.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => onNavigate?.(item.id)}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                currentView === item.id
                  ? 'bg-blue-600 text-white shadow-lg'
                  : 'text-gray-300 hover:bg-blue-600 hover:text-white'
              } ${!sidebarOpen ? 'justify-center' : ''}`}
            >
              <span className="text-xl">{item.icon}</span>
              {sidebarOpen && <span>{item.label}</span>}
            </button>
          ))}
        </nav>

        {/* Bottom Section */}
        <div className="p-4 border-t border-slate-700 space-y-3">
          <div className={`flex items-center gap-3 px-4 py-2 ${!sidebarOpen ? 'justify-center' : ''}`}>
            <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-sm font-bold">
              {user?.nombre?.[0]}
            </div>
            {sidebarOpen && (
              <div className="text-sm">
                <p className="font-semibold">{user?.nombre}</p>
                <p className="text-xs text-gray-400">{user?.rol}</p>
              </div>
            )}
          </div>
          <button
            onClick={onLogout}
            className="w-full flex items-center gap-3 px-4 py-2 text-red-400 hover:bg-red-500/20 rounded-lg transition-colors"
          >
            <span className="text-xl">🚪</span>
            {sidebarOpen && <span>Logout</span>}
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className={`${sidebarOpen ? 'ml-64' : 'ml-20'} flex-1 flex flex-col transition-all duration-300`}>
        {/* Top Bar */}
        <div className="bg-white border-b border-gray-200 px-8 py-4 flex items-center justify-between sticky top-0 z-10 shadow-sm">
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors text-xl"
          >
            {sidebarOpen ? '✕' : '☰'}
          </button>
          <h1 className="text-xl font-bold text-gray-800">Barraca Morales</h1>
          <div className="relative">
            <button
              type="button"
              onClick={() => setNotificationsOpen((prev) => !prev)}
              className="relative rounded-lg p-2 text-xl transition-colors hover:bg-gray-100"
            >
              🔔
              {unreadCount > 0 && (
                <span className="absolute -right-1 -top-1 inline-flex min-h-5 min-w-5 items-center justify-center rounded-full bg-red-500 px-1 text-xs font-bold text-white">
                  {unreadCount}
                </span>
              )}
            </button>

            {notificationsOpen && (
              <div className="absolute right-0 mt-3 w-96 overflow-hidden rounded-xl border border-gray-200 bg-white shadow-xl">
                <div className="border-b border-gray-200 px-4 py-3">
                  <h2 className="font-bold text-gray-800">Notificaciones</h2>
                </div>
                <div className="max-h-96 overflow-y-auto">
                  {notifications.length === 0 && (
                    <div className="px-4 py-6 text-sm text-gray-500">Sin notificaciones.</div>
                  )}
                  {notifications.map((notification) => (
                    <button
                      key={notification.id}
                      type="button"
                      onClick={() => onReadNotification?.(notification.id)}
                      className={`w-full border-b border-gray-100 px-4 py-3 text-left transition-colors hover:bg-gray-50 ${
                        notification.leida ? 'bg-white' : 'bg-blue-50'
                      }`}
                    >
                      <p className="text-sm font-semibold text-gray-800">{notification.mensaje}</p>
                      <p className="mt-1 text-xs text-gray-500">
                        {new Date(notification.created_at).toLocaleString('es-UY')}
                      </p>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-auto p-8">
          {children}
        </div>
      </div>
    </div>
  )
}
