// ViewModel para Login con JWT
// Archivo: android-app-kotlin/src/main/kotlin/com/barraca/conductor/viewmodel/LoginViewModel.kt

package com.barraca.conductor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barraca.conductor.data.api.ConductorApiService
import com.barraca.conductor.data.model.LoginRequest
import com.barraca.conductor.utils.JwtTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para gestionar login con autenticación JWT
 * 
 * Estados:
 * - Idle: Esperando entrada del usuario
 * - Loading: Procesando login
 * - Success: Login exitoso
 * - Error: Error en login
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ConductorApiService,
    private val tokenManager: JwtTokenManager
) : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var loginSuccess by mutableStateOf(false)
        private set

    fun updateEmail(newEmail: String) {
        email = newEmail
        errorMessage = ""
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
        errorMessage = ""
    }

    /**
     * Realizar login
     * 
     * Pasos:
     * 1. Validar campos
     * 2. Llamar endpoint /auth/login
     * 3. Guardar tokens en JwtTokenManager
     * 4. Navegar a Home
     */
    fun login() {
        // Validación
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email y contraseña requeridos"
            return
        }

        if (!email.contains("@")) {
            errorMessage = "Email inválido"
            return
        }

        isLoading = true
        errorMessage = ""

        viewModelScope.launch {
            try {
                Timber.d("🔐 Intentando login para: $email")

                // Llamar API
                val response = apiService.login(
                    LoginRequest(
                        email = email,
                        password = password,
                        tipo = "conductor"  // o "admin"
                    )
                )

                if (response.isSuccessful) {
                    val loginResponse = response.body()?.data
                    if (loginResponse != null) {
                        Timber.d("✅ Login exitoso para: $email")

                        // Guardar tokens
                        tokenManager.saveTokens(
                            accessToken = loginResponse.accessToken,
                            refreshToken = loginResponse.refreshToken,
                            userEmail = loginResponse.user.email,
                            userId = loginResponse.user.id,
                            userName = loginResponse.user.nombre,
                            userType = loginResponse.user.tipo
                        )

                        // Marcar como success
                        loginSuccess = true
                        errorMessage = ""
                    } else {
                        errorMessage = "Respuesta inválida del servidor"
                    }
                } else {
                    // Error en response
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Timber.e("❌ Error login: ${response.code()} - $errorBody")
                    errorMessage = when (response.code()) {
                        401 -> "Email o contraseña incorrectos"
                        400 -> "Datos incompletos"
                        else -> "Error en el servidor"
                    }
                }
            } catch (e: Exception) {
                Timber.e("💥 Excepción en login: ${e.message}")
                errorMessage = "Error de conexión: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Logout - Limpiar tokens
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // Llamar endpoint logout (opcional)
                apiService.logout()
            } catch (e: Exception) {
                Timber.w("⚠️ Error llamando logout endpoint: ${e.message}")
            } finally {
                // Siempre limpiar tokens locales
                tokenManager.clearTokens()
                resetState()
            }
        }
    }

    /**
     * Reset de estado
     */
    fun resetState() {
        email = ""
        password = ""
        errorMessage = ""
        loginSuccess = false
        isLoading = false
    }

    /**
     * Verificar si usuario ya está autenticado
     */
    fun isUserAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }
}
