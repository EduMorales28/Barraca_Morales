import { useState } from 'react'
import { login } from '../api'

export default function LoginPage({ onLogin }) {
  const [email, setEmail] = useState('admin@test.com')
  const [password, setPassword] = useState('1234')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const response = await login(email, password)
      onLogin(response.data)
    } catch (err) {
      setError(err.response?.data?.error || 'Error al iniciar sesión')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-600 via-blue-500 to-blue-700 flex items-center justify-center p-4">
      {/* Fondo decorativo */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-white/10 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-white/10 rounded-full blur-3xl"></div>
      </div>

      <div className="relative z-10 w-full max-w-md">
        {/* Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8 space-y-6">
          {/* Header */}
          <div className="text-center space-y-3">
            <div className="flex justify-center">
              <div className="w-16 h-16 bg-gradient-to-br from-blue-600 to-blue-700 rounded-2xl flex items-center justify-center text-white text-2xl font-bold shadow-lg">
                BM
              </div>
            </div>
            <h1 className="text-3xl font-bold text-gray-800">Barraca Morales</h1>
            <p className="text-gray-600">Sistema de Logística</p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:border-blue-500 transition-colors"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">Contraseña</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:border-blue-500 transition-colors"
                required
              />
            </div>

            {error && (
              <div className="p-4 bg-red-100 border-2 border-red-400 text-red-700 rounded-xl text-sm flex items-center gap-3">
                <span className="text-xl">⚠️</span>
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white font-bold py-3 px-4 rounded-xl disabled:opacity-50 disabled:cursor-not-allowed transition-all transform hover:shadow-lg active:scale-95"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="animate-spin">⏳</span> Iniciando sesión...
                </span>
              ) : (
                'Iniciar sesión'
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t-2 border-gray-200"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-white text-gray-500">Credenciales de prueba</span>
            </div>
          </div>

          {/* Credentials */}
          <div className="space-y-3 bg-gray-50 rounded-xl p-4">
            <div className="flex items-start gap-3">
              <span className="text-xl mt-1">👨‍💼</span>
              <div>
                <p className="font-semibold text-gray-800">Administrador</p>
                <p className="text-sm text-gray-600">admin@test.com</p>
                <p className="text-sm text-gray-600">Contraseña: 1234</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <span className="text-xl mt-1">👨‍🚚</span>
              <div>
                <p className="font-semibold text-gray-800">Conductor</p>
                <p className="text-sm text-gray-600">conductor1@test.com</p>
                <p className="text-sm text-gray-600">Contraseña: 1234</p>
              </div>
            </div>
          </div>

          {/* Footer */}
          <p className="text-center text-xs text-gray-500">
            Plataforma segura de gestión logística © 2026
          </p>
        </div>
      </div>
    </div>
  )
}
