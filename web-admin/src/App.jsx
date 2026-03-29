import { useState, useEffect } from 'react'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import Layout from './components/Layout'
import { getNotificaciones, marcarNotificacionLeida } from './api'

export default function App() {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [currentView, setCurrentView] = useState('dashboard')
  const [notifications, setNotifications] = useState([])

  useEffect(() => {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      setUser(JSON.parse(savedUser))
    }
    setLoading(false)
  }, [])

  useEffect(() => {
    if (!user?.id) {
      setNotifications([])
      return
    }

    loadNotifications()

    const intervalId = window.setInterval(() => {
      loadNotifications()
    }, 15000)

    return () => window.clearInterval(intervalId)
  }, [user?.id])

  const loadNotifications = async () => {
    if (!user?.id) return

    try {
      const response = await getNotificaciones()
      setNotifications(response.data)
    } catch {
      setNotifications([])
    }
  }

  const handleMarkNotification = async (notificationId) => {
    try {
      await marcarNotificacionLeida(notificationId)
      await loadNotifications()
    } catch {
      return
    }
  }

  const handleLogin = (userData) => {
    setUser(userData)
    setCurrentView('dashboard')
    localStorage.setItem('user', JSON.stringify(userData))
  }

  const handleLogout = () => {
    setUser(null)
    setNotifications([])
    setCurrentView('dashboard')
    localStorage.removeItem('user')
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gradient-to-br from-slate-900 to-slate-800">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-white">Cargando...</p>
        </div>
      </div>
    )
  }

  return user ? (
    <Layout
      user={user}
      onLogout={handleLogout}
      currentView={currentView}
      onNavigate={setCurrentView}
      notifications={notifications}
      onReadNotification={handleMarkNotification}
    >
      <DashboardPage
        user={user}
        view={currentView}
        onViewChange={setCurrentView}
        onNotificationsChange={loadNotifications}
      />
    </Layout>
  ) : (
    <LoginPage onLogin={handleLogin} />
  )
}
