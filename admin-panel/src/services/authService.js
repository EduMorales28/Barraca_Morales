// API Service para Admin Panel (Vue/JavaScript)
// Archivo: admin-panel/src/services/authService.js

import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:3000';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * Interceptor para agregar JWT a todos los requests
 * Automáticamente incluye el token en el header Authorization
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Interceptor para respuesta - manejar si token expiró
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Si es 401, el token expiró
    if (error.response?.status === 401) {
      // Limpiar tokens
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('userType');
      
      // En una app real, aquí intentarías refrescar el token
      // y reintentar la request
      
      // Redirigir a login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/**
 * Login - Obtener tokens JWT
 * 
 * @param {string} email - Email del usuario
 * @param {string} password - Contraseña
 * @param {string} tipo - Tipo de usuario (admin o conductor)
 * @returns {Promise<Object>} { accessToken, refreshToken, user }
 */
export const login = async (email, password, tipo = 'admin') => {
  try {
    const response = await api.post('/auth/login', {
      email,
      password,
      tipo
    });

    const { accessToken, refreshToken, user } = response.data.data;

    // Guardar en localStorage
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('userEmail', user.email);
    localStorage.setItem('userType', user.tipo);
    localStorage.setItem('userName', user.nombre);
    localStorage.setItem('userId', user.id);

    console.log('✅ Login exitoso para:', user.email);

    return {
      success: true,
      user,
      accessToken,
      refreshToken
    };
  } catch (error) {
    console.error('❌ Error en login:', error.response?.data?.error || error.message);
    throw error;
  }
};

/**
 * Refresh Token - Obtener nuevo accessToken
 * 
 * @param {string} refreshToken - Refresh token guardado
 * @returns {Promise<string>} Nuevo access token
 */
export const refreshAccessToken = async (refreshToken) => {
  try {
    const response = await api.post('/auth/refresh', {
      refreshToken
    });

    const newAccessToken = response.data.data.accessToken;
    localStorage.setItem('accessToken', newAccessToken);

    console.log('🔄 Access token refrescado');

    return newAccessToken;
  } catch (error) {
    console.error('❌ Error refrescando token:', error.message);
    throw error;
  }
};

/**
 * Logout - Cerrar sesión y limpiar datos
 */
export const logout = async () => {
  try {
    // Llamar al endpoint logout (opcional)
    await api.post('/auth/logout');
    
    // Limpiar localStorage
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userType');
    localStorage.removeItem('userName');
    localStorage.removeItem('userId');

    console.log('✅ Logout exitoso');

    return { success: true };
  } catch (error) {
    // Aunque error, limpiar tokens locales
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    
    console.error('⚠️ Error en logout endpoint:', error.message);
    console.log('✅ Sesión local limpiada');
    
    return { success: true };
  }
};

/**
 * Obtener token del localStorage
 */
export const getAccessToken = () => localStorage.getItem('accessToken');

export const getRefreshToken = () => localStorage.getItem('refreshToken');

/**
 * Obtener datos del usuario autenticado
 */
export const getAuthUser = () => {
  return {
    email: localStorage.getItem('userEmail'),
    name: localStorage.getItem('userName'),
    id: localStorage.getItem('userId'),
    type: localStorage.getItem('userType')
  };
};

/**
 * Verificar si usuario está autenticado
 */
export const isAuthenticated = () => {
  return !!localStorage.getItem('accessToken') && !!localStorage.getItem('refreshToken');
};

/**
 * Verificar token con el servidor
 */
export const verifyToken = async () => {
  try {
    const response = await api.get('/auth/verify');
    return response.data.data.user;
  } catch (error) {
    console.error('❌ Token inválido:', error.message);
    throw error;
  }
};

export default api;
