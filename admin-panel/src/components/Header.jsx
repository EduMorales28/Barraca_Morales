import { Menu, Bell, User, LogOut } from 'lucide-react'

export default function Header({ onMenuClick }) {
  return (
    <header className="bg-white border-b border-gray-200 shadow-sm">
      <div className="flex items-center justify-between h-16 px-6">
        {/* Left */}
        <div className="flex items-center gap-4">
          <button
            onClick={onMenuClick}
            className="p-2 hover:bg-gray-100 rounded-lg transition"
          >
            <Menu size={24} />
          </button>
          <h1 className="text-xl font-bold text-gray-900">Barraca Admin</h1>
        </div>

        {/* Right */}
        <div className="flex items-center gap-4">
          {/* Notifications */}
          <button className="relative p-2 hover:bg-gray-100 rounded-lg transition">
            <Bell size={20} className="text-gray-600" />
            <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
          </button>

          {/* User menu */}
          <div className="flex items-center gap-3 pl-4 border-l border-gray-200">
            <div className="text-right">
              <p className="text-sm font-medium text-gray-900">Admin</p>
              <p className="text-xs text-gray-500">Gerente</p>
            </div>
            <div className="w-10 h-10 bg-gradient-to-br from-primary to-orange-600 rounded-lg flex items-center justify-center">
              <User size={20} className="text-white" />
            </div>
            <button className="p-2 hover:bg-gray-100 rounded-lg transition">
              <LogOut size={20} className="text-gray-600" />
            </button>
          </div>
        </div>
      </div>
    </header>
  )
}
