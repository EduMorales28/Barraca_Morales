<!-- Componente Login para Admin Panel -->
<!-- Archivo: admin-panel/src/components/LoginScreen.vue -->

<template>
  <div class="login-container">
    <div class="login-card">
      <!-- Logo/Header -->
      <div class="login-header">
        <h1>🚚 LogísticaMorales</h1>
        <p>Panel Administrativo</p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleLogin" class="login-form">
        <!-- Email Input -->
        <div class="form-group">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            placeholder="admin@logistica.com"
            required
            :disabled="isLoading"
          />
        </div>

        <!-- Password Input -->
        <div class="form-group">
          <label for="password">Contraseña</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="Tu contraseña"
            required
            :disabled="isLoading"
          />
        </div>

        <!-- User Type Selection -->
        <div class="form-group">
          <label for="tipo">Tipo de Usuario</label>
          <select
            id="tipo"
            v-model="form.tipo"
            required
            :disabled="isLoading"
          >
            <option value="admin">Administrador</option>
            <option value="conductor">Conductor</option>
          </select>
        </div>

        <!-- Remember Me -->
        <div class="form-checkbox">
          <input
            id="remember"
            v-model="form.rememberMe"
            type="checkbox"
            :disabled="isLoading"
          />
          <label for="remember">Recuérdame</label>
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="error-message">
          ⚠️ {{ errorMessage }}
        </div>

        <!-- Success Message -->
        <div v-if="successMessage" class="success-message">
          ✅ {{ successMessage }}
        </div>

        <!-- Submit Button -->
        <button
          type="submit"
          class="login-button"
          :disabled="isLoading"
        >
          <span v-if="isLoading">
            <!-- Loading spinner -->
            <span class="spinner"></span>
            Iniciando sesión...
          </span>
          <span v-else>
            Iniciar Sesión
          </span>
        </button>
      </form>

      <!-- Test Credentials -->
      <div class="test-credentials">
        <p><strong>Test:</strong></p>
        <ul>
          <li>Admin: admin@logistica.com / admin123</li>
          <li>Conductor: juan@conductor.com / admin123</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '../services/authService';

const router = useRouter();

const form = ref({
  email: 'admin@logistica.com',
  password: 'admin123',
  tipo: 'admin',
  rememberMe: false
});

const isLoading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

const handleLogin = async () => {
  try {
    errorMessage.value = '';
    successMessage.value = '';

    // Validación básica
    if (!form.value.email || !form.value.password) {
      errorMessage.value = 'Email y contraseña requeridos';
      return;
    }

    if (!form.value.email.includes('@')) {
      errorMessage.value = 'Email inválido';
      return;
    }

    isLoading.value = true;

    console.log('🔐 Intentando login para:', form.value.email);

    // Llamar API
    const response = await login(
      form.value.email,
      form.value.password,
      form.value.tipo
    );

    successMessage.value = `¡Bienvenido ${response.user.nombre}!`;

    // Redirigir a dashboard
    setTimeout(() => {
      router.push('/dashboard');
    }, 1000);

  } catch (error) {
    console.error('Error en login:', error);
    
    if (error.response?.status === 401) {
      errorMessage.value = 'Email o contraseña incorrectos';
    } else if (error.response?.data?.error) {
      errorMessage.value = error.response.data.error;
    } else {
      errorMessage.value = 'Error de conexión. Verifica que el backend está corriendo.';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.login-header p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 12px;
  font-weight: 600;
  color: #333;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-group input,
.form-group select {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.2s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group input:disabled,
.form-group select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.form-checkbox input[type="checkbox"] {
  cursor: pointer;
  width: 16px;
  height: 16px;
}

.form-checkbox label {
  cursor: pointer;
}

.error-message {
  padding: 12px;
  background-color: #fee;
  border: 1px solid #fcc;
  border-radius: 6px;
  color: #c33;
  font-size: 13px;
}

.success-message {
  padding: 12px;
  background-color: #efe;
  border: 1px solid #cfc;
  border-radius: 6px;
  color: #3c3;
  font-size: 13px;
}

.login-button {
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.test-credentials {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  font-size: 12px;
  color: #999;
}

.test-credentials p {
  margin: 0 0 8px 0;
  font-weight: 600;
}

.test-credentials ul {
  margin: 0;
  padding-left: 20px;
  list-style: none;
}

.test-credentials li {
  margin: 4px 0;
  font-family: 'Courier New', monospace;
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
}

@media (max-width: 480px) {
  .login-card {
    padding: 24px;
  }

  .login-header h1 {
    font-size: 22px;
  }
}
</style>
