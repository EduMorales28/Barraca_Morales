// Gestor de Tokens JWT
// Archivo: android-app-kotlin/src/main/kotlin/com/barraca/conductor/utils/JwtTokenManager.kt

package com.barraca.conductor.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/**
 * Gestor de tokens JWT
 * Almacena y recupera tokens de autenticación seguros
 * 
 * Almacenamiento:
 * - ACCESS_TOKEN: Token corta duración (1h)
 * - REFRESH_TOKEN: Token larga duración (7 días)
 * - USER_EMAIL: Email del usuario logueado
 * - USER_TYPE: admin o conductor
 */
@Singleton
class JwtTokenManager @Inject constructor(
    context: Context
) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val mutex = Mutex()

    companion object {
        private const val PREFS_NAME = "com.barraca.conductor.jwt"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_EMAIL_KEY = "user_email"
        private const val USER_ID_KEY = "user_id"
        private const val USER_NAME_KEY = "user_name"
        private const val USER_TYPE_KEY = "user_type"
        private const val TOKEN_EXPIRY_KEY = "token_expiry"
    }

    /**
     * Obtener access token guardado
     */
    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }

    /**
     * Obtener refresh token guardado
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }

    /**
     * Guardar tokens después del login
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userEmail: String,
        userId: String,
        userName: String,
        userType: String
    ) {
        mutex.withLock {
            sharedPreferences.edit().apply {
                putString(ACCESS_TOKEN_KEY, accessToken)
                putString(REFRESH_TOKEN_KEY, refreshToken)
                putString(USER_EMAIL_KEY, userEmail)
                putString(USER_ID_KEY, userId)
                putString(USER_NAME_KEY, userName)
                putString(USER_TYPE_KEY, userType)
                putLong(TOKEN_EXPIRY_KEY, System.currentTimeMillis() + 3600000) // 1 hora
                apply()
            }
            Timber.d("📱 Tokens guardados para: $userEmail")
        }
    }

    /**
     * Actualizar access token (después de refresh)
     */
    suspend fun updateAccessToken(newAccessToken: String) {
        mutex.withLock {
            sharedPreferences.edit().apply {
                putString(ACCESS_TOKEN_KEY, newAccessToken)
                apply()
            }
            Timber.d("🔄 Access token actualizado")
        }
    }

    /**
     * Obtener datos del usuario
     */
    data class UserInfo(
        val email: String,
        val id: String,
        val name: String,
        val type: String // "admin" o "conductor"
    )

    fun getUserInfo(): UserInfo? {
        val email = sharedPreferences.getString(USER_EMAIL_KEY, null) ?: return null
        val id = sharedPreferences.getString(USER_ID_KEY, null) ?: return null
        val name = sharedPreferences.getString(USER_NAME_KEY, null) ?: return null
        val type = sharedPreferences.getString(USER_TYPE_KEY, null) ?: return null

        return UserInfo(email, id, name, type)
    }

    /**
     * Verificar si el token está próximo a expirar (< 5 minutos)
     */
    fun isTokenNearExpiry(): Boolean {
        val expiry = sharedPreferences.getLong(TOKEN_EXPIRY_KEY, 0)
        val currentTime = System.currentTimeMillis()
        val timeUntilExpiry = expiry - currentTime

        // Si falta menos de 5 minutos (300000 ms)
        return timeUntilExpiry < 300000 && timeUntilExpiry > 0
    }

    /**
     * Verificar si el usuario está autenticado
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }

    /**
     * Limpiar tokens (logout)
     */
    suspend fun clearTokens() {
        mutex.withLock {
            sharedPreferences.edit().apply {
                remove(ACCESS_TOKEN_KEY)
                remove(REFRESH_TOKEN_KEY)
                remove(USER_EMAIL_KEY)
                remove(USER_ID_KEY)
                remove(USER_NAME_KEY)
                remove(USER_TYPE_KEY)
                remove(TOKEN_EXPIRY_KEY)
                apply()
            }
            Timber.d("🗑️ Tokens eliminados - sesión cerrada")
        }
    }
}
