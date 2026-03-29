package com.barraca.conductor.services

import android.content.Context
import com.barraca.conductor.data.api.ConductorApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Sincronizador de tokens FCM con backend
 * 
 * Responsabilidad:
 * - Obtener token FCM
 * - Enviarlo al backend
 * - Pasar el conductorId
 * - Guardar estado de sincronización
 */
class FCMTokenSynchronizer(
    private val context: Context,
    private val apiService: ConductorApiService
) {

    companion object {
        private const val TAG = "FCMTokenSync"
        private const val PREFS_NAME = "fcm_sync"
        private const val LAST_SENT_TOKEN = "last_sent_token"
    }

    /**
     * Sincronizar token con backend
     * Debería llamarse:
     * 1. Después de login (tenemos conductorId)
     * 2. Cuando llega un token nuevo
     * 3. Periódicamente (cada 24h)
     */
    fun sincronizarToken(conductorId: String) {
        val tokenManager = FCMTokenManager(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Obtener token actual
                val token = tokenManager.obtenerToken()
                
                if (token == null) {
                    Timber.e("$TAG: No se pudo obtener token")
                    return@launch
                }

                // Verificar si es un token nuevo
                if (esTokenNuevo(token)) {
                    // Enviar al backend
                    enviarTokenAlBackend(conductorId, token)
                    guardarTokenSincronizado(token)
                }
            } catch (e: Exception) {
                Timber.e(e, "$TAG: Error sincronizando token")
            }
        }
    }

    /**
     * Enviar token al backend
     * El backend lo almacena para enviar notificaciones después
     */
    private suspend fun enviarTokenAlBackend(conductorId: String, token: String) {
        try {
            // Llamar endpoint del backend
            val response = apiService.actualizarTokenFCM(conductorId, token)
            
            if (response.isSuccessful) {
                Timber.d("$TAG: Token enviado al backend exitosamente")
            } else {
                Timber.e("$TAG: Error al enviar token - ${response.code()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Exception enviando token")
        }
    }

    /**
     * Verificar si el token es nuevo (diferente del último guardado)
     */
    private fun esTokenNuevo(token: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ultimoToken = prefs.getString(LAST_SENT_TOKEN, "")
        return token != ultimoToken
    }

    /**
     * Guardar que el token fue sincronizado
     */
    private fun guardarTokenSincronizado(token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LAST_SENT_TOKEN, token).apply()
    }
}
