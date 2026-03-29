// Composable para gestionar autenticación en Vue
// Archivo: admin-panel/src/composables/useAuth.js

import { ref, computed } from 'vue';
import * as authService from '../services/authService';

export const useAuth = () => {
  const user = ref(null);
  const isAuthenticated = ref(false);
  const isLoading = ref(false);
  const error = ref('');

  /**
   * Inicializar - verificar si ya existe sesión
   */
  const init = async () => {
    try {
      if (authService.isAuthenticated()) {
        const userData = await authService.verifyToken();
        user.value = userData;
        isAuthenticated.value = true;
      }
    } catch (e) {
      console.warn('Session check failed:', e);
      // Token inválido, limpiar
      await logout();
    }
  };

  /**
   * Login
   */
  const login = async (email, password, tipo = 'admin') => {
    try {
      isLoading.value = true;
      error.value = '';

      const response = await authService.login(email, password, tipo);
      
      user.value = response.user;
      isAuthenticated.value = true;
      
      return response;
    } catch (e) {
      error.value = e.response?.data?.error || e.message;
      isAuthenticated.value = false;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * Logout
   */
  const logout = async () => {
    try {
      await authService.logout();
    } catch (e) {
      console.error('Logout error:', e);
    } finally {
      user.value = null;
      isAuthenticated.value = false;
      error.value = '';
    }
  };

  /**
   * Refresh token
   */
  const refreshToken = async () => {
    try {
      const refreshToken = authService.getRefreshToken();
      if (refreshToken) {
        await authService.refreshAccessToken(refreshToken);
        return true;
      }
      return false;
    } catch (e) {
      console.error('Token refresh failed:', e);
      await logout();
      return false;
    }
  };

  /**
   * Computados
   */
  const isAdmin = computed(() => user.value?.tipo === 'admin');
  const isConductor = computed(() => user.value?.tipo === 'conductor');
  const userName = computed(() => user.value?.nombre || '');
  const userEmail = computed(() => user.value?.email || '');

  return {
    // State
    user,
    isAuthenticated,
    isLoading,
    error,
    
    // Methods
    init,
    login,
    logout,
    refreshToken,
    
    // Computed
    isAdmin,
    isConductor,
    userName,
    userEmail
  };
};

/**
 * Crear instancia global
 * 
 * Uso en main.js:
 * ```
 * const auth = useAuth();
 * app.provide('auth', auth);
 * await auth.init();
 * ```
 * 
 * Uso en componentes:
 * ```
 * const auth = inject('auth');
 * ```
 */
