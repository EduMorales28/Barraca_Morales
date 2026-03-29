package com.barraca.conductor.services

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Servicio para gestionar FCM Token
 * Responsabilidades:
 * - Obtener token FCM
 * - Guardar token localmente
 * - Recuperar token
 * - Suscribirse a topics (opcional)
 */
class FCMTokenManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "fcm_preferences"
        private const val TOKEN_KEY = "fcm_token_key"
        private const val TAG = "FCMTokenManager"
    }

    /**
     * Obtener token FCM actual
     * Puede ser diferente cada vez que se llama
     */
    suspend fun obtenerToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Timber.d("$TAG: Token obtenido: $token")
            guardarTokenLocal(token)
            token
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error obteniendo token")
            obtenerTokenLocal()  // Retornar token guardado si existe
        }
    }

    /**
     * Guardar token en preferencias locales
     */
    private fun guardarTokenLocal(token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    /**
     * Obtener token guardado localmente
     */
    fun obtenerTokenLocal(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(TOKEN_KEY, null)
    }

    /**
     * Suscribirse a un topic
     * Útil para enviar notificaciones a grupos de conductores
     * 
     * Ejemplo: suscribirse("pedidos_caba") → todos recibirán si envías al topic "pedidos_caba"
     */
    fun suscribirseAlTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnSuccessListener {
                Timber.d("$TAG: Suscrito al topic: $topic")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "$TAG: Error suscribiendo al topic: $topic")
            }
    }

    /**
     * Desuscribirse de un topic
     */
    fun desuscribirseDelTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnSuccessListener {
                Timber.d("$TAG: Desuscrito del topic: $topic")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "$TAG: Error desuscribiendo del topic: $topic")
            }
    }

    /**
     * Habilitar o deshabilitar notificaciones automáticas
     */
    fun habilitarNotificacionesAutomaticas(habilitar: Boolean) {
        FirebaseMessaging.getInstance().isAutoInitEnabled = habilitar
        val estado = if (habilitar) "habilitado" else "deshabilitado"
        Timber.d("$TAG: FCM $estado")
    }
}
