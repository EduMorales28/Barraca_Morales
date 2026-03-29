import axios from 'axios'

const API_URL = 'http://localhost:3000'

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use((config) => {
  const storedUser = localStorage.getItem('user')

  if (storedUser) {
    const parsedUser = JSON.parse(storedUser)
    if (parsedUser?.token) {
      config.headers.Authorization = `Bearer ${parsedUser.token}`
    }
  }

  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && localStorage.getItem('user')) {
      localStorage.removeItem('user')
      window.location.reload()
    }

    return Promise.reject(error)
  }
)

export const login = (email, password) => api.post('/login', { email, password })
export const getUsuarios = () => api.get('/usuarios')
export const createUsuario = (data) => api.post('/usuarios', data)
export const getClientes = () => api.get('/clientes')
export const getArticulos = () => api.get('/articulos')
export const createCliente = (data) => api.post('/clientes', data)
export const getProveedores = () => api.get('/proveedores')
export const createProveedor = (data) => api.post('/proveedores', data)
export const createArticulo = (data) => api.post('/articulos', data)
export const getPedidos = () => api.get('/pedidos')
export const createPedido = (data) => api.post('/pedidos', data)
export const asignarPedido = (id, conductor_id) => api.post(`/pedidos/${id}/asignar`, { conductor_id })
export const aceptarPedido = (id) => api.post(`/pedidos/${id}/aceptar`)
export const updatePedido = (id, data) => api.put(`/pedidos/${id}`, data)
export const getDetallePedido = (id) => api.get(`/pedidos/${id}`)
export const getNotificaciones = () => api.get('/notificaciones')
export const marcarNotificacionLeida = (notificacionId) => api.post(`/notificaciones/${notificacionId}/leer`)

export default api
