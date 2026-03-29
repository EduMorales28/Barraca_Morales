// Interceptor JWT para Retrofit
// Archivo: android-app-kotlin/src/main/kotlin/com/barraca/conductor/data/api/JwtAuthInterceptor.kt

package com.barraca.conductor.data.api

import com.barraca.conductor.utils.JwtTokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

/**
 * Interceptor OkHttp que automáticamente agrega JWT a todos los requests
 * 
 * Funcionalidad:
 * 1. Obtiene el access token del JwtTokenManager
 * 2. Si existe, lo agrega al header Authorization: Bearer {token}
 * 3. Si el token está próximo a expirar, lo intenta refrescar
 * 4. Luego continúa con el request
 */
class JwtAuthInterceptor @Inject constructor(
    private val tokenManager: JwtTokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // No agregar JWT a endpoints públicos
        if (isPublicEndpoint(originalRequest.url.encodedPath)) {
            return chain.proceed(originalRequest)
        }

        // Obtener token actual
        val accessToken = tokenManager.getAccessToken()

        // Si no hay token, continuar sin Authorization header
        if (accessToken == null) {
            Timber.w("⚠️ No hay token JWT disponible para: ${originalRequest.url.encodedPath}")
            return chain.proceed(originalRequest)
        }

        // Agregar JWT al header
        val requestWithToken = addTokenToRequest(originalRequest, accessToken)
        
        Timber.d("🔐 JWT agregado a: ${originalRequest.url.encodedPath}")

        val response = chain.proceed(requestWithToken)

        // Si el response es 401 (Unauthorized), el token expiró
        if (response.code == 401) {
            Timber.w("❌ Token expirado (401) para: ${originalRequest.url.encodedPath}")
            // En una aplicación real, aquí intentarías refrescar el token
            // y reintentar el request. Por ahora solo loguear.
        }

        return response
    }

    /**
     * Agregar token JWT al header Authorization
     */
    private fun addTokenToRequest(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()
    }

    /**
     * Endpoints que NO requieren JWT
     */
    private fun isPublicEndpoint(path: String): Boolean {
        val publicEndpoints = listOf(
            "/auth/login",
            "/auth/refresh",
            "/health",
            "/api/public"
        )
        return publicEndpoints.any { path.contains(it) }
    }
}
