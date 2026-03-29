package com.barraca.conductor.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.barraca.conductor.MainActivity
import com.barraca.conductor.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

/**
 * Servicio para recibir mensajes de Firebase Cloud Messaging (FCM)
 * 
 * Este servicio se ejecuta cuando llega una notificación push:
 * 1. Notificación en foreground (app abierta)
 * 2. Notificación en background (app cerrada)
 * 3. Mensajes de datos sin UI
 * 4. Cambios de token FCM
 * 
 * Uso:
 * - Firebase envía mensaje con topic o al token del dispositivo
 * - Este servicio recibe automáticamente
 * - Se procesa y se muestra notificación
 */
class ConductorFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "pedidos_canal"
        private const val CHANNEL_NAME = "Notificaciones de Pedidos"
    }

    /**
     * Llamado cuando llega un mensaje de Firebase
     * Puede ser:
     * - Notificación (UI) - firebase nota el app automáticamente
     * - Datos (JSON) - manejado aquí
     * - Ambos
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("$TAG: Mensaje recibido")

        // Hay 2 tipos de datos que puede tener:
        // 1. remoteMessage.notification - UI integrada
        // 2. remoteMessage.data - JSON personalizado

        if (remoteMessage.notification != null) {
            // Notificación estándar con título y cuerpo
            mostrarNotificacion(
                titulo = remoteMessage.notification?.title ?: "Conductores App",
                cuerpo = remoteMessage.notification?.body ?: ""
            )
            Timber.d("$TAG: Notificación estándar: ${remoteMessage.notification?.title}")
        }

        if (remoteMessage.data.isNotEmpty()) {
            // Datos personalizados (JSON)
            val tipo = remoteMessage.data["tipo"] ?: "general"
            val pedidoId = remoteMessage.data["pedidoId"]
            val titulo = remoteMessage.data["titulo"] ?: "Notificación"
            val cuerpo = remoteMessage.data["cuerpo"] ?: ""

            Timber.d("$TAG: Datos recibidos - Tipo: $tipo, PedidoId: $pedidoId")

            // Procesar según tipo
            when (tipo) {
                "pedido_asignado" -> procesarPedidoAsignado(pedidoId, titulo, cuerpo)
                "pedido_actualizado" -> procesarPedidoActualizado(pedidoId, titulo, cuerpo)
                "alerta" -> mostrarNotificacion(titulo, cuerpo)
                else -> mostrarNotificacion(titulo, cuerpo)
            }
        }
    }

    /**
     * Llamado cuando el token de FCM cambia
     * Esto sucede cuando:
     * - El app se instala por primera vez
     * - El usuario desinstala/reinstala el app
     * - El usuario limpia datos del app
     * - Firebase renueva el token (cada ~6 meses)
     */
    override fun onNewToken(token: String) {
        Timber.d("$TAG: Token FCM nuevo: $token")

        // Guardar el token para enviar al backend
        guardarTokenFCM(token)

        // Aquí deberías enviar el token al backend
        // para poder enviar notificaciones a este dispositivo
        enviarTokenAlBackend(token)
    }

    /**
     * Procesar notificación cuando un pedido es asignado
     */
    private fun procesarPedidoAsignado(pedidoId: String?, titulo: String, cuerpo: String) {
        // Log del evento
        Timber.d("$TAG: Pedido asignado - ID: $pedidoId")

        // Aquí podrías:
        // 1. Reproducir sonido personalizado
        // 2. Vibrar
        // 3. Encender pantalla
        // 4. Guardar en base de datos local
        // 5. Actualizar UI si el app está abierto

        mostrarNotificacion(titulo, cuerpo)
    }

    /**
     * Procesar notificación cuando un pedido se actualiza
     */
    private fun procesarPedidoActualizado(pedidoId: String?, titulo: String, cuerpo: String) {
        Timber.d("$TAG: Pedido actualizado - ID: $pedidoId")
        mostrarNotificacion(titulo, cuerpo)
    }

    /**
     * Mostrar notificación en el sistema
     */
    private fun mostrarNotificacion(titulo: String, cuerpo: String, pedidoId: String? = null) {
        // Crear canal de notificación (Android 8+)
        crearCanalNotificacion()

        // Intent cuando toca la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (pedidoId != null) {
                putExtra("pedidoId", pedidoId)
                putExtra("accion", "ver_pedido")
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir notificación
        val notificacion = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Ícono pequeño
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)  // Descartar al tocar
            .setContentIntent(pendingIntent)  // Intent al tocar
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // Mostrar heads-up
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))  // Texto largo expandible
            .build()

        // Mostrar notificación
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificacion)

        Timber.d("$TAG: Notificación mostrada - $titulo")
    }

    /**
     * Crear canal de notificación (requerido para Android 8+)
     */
    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de asignación y actualización de pedidos"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Guardar token FCM en preferencias locales
     */
    private fun guardarTokenFCM(token: String) {
        val sharedPref = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("fcm_token", token).apply()
        Timber.d("$TAG: Token guardado localmente")
    }

    /**
     * Enviar token FCM al backend
     * Esto permite que el backend sepa a qué dispositivo enviar notificaciones
     */
    private fun enviarTokenAlBackend(token: String) {
        // TODO: Implementar llamada a tu API para guardar el token
        // Ejemplo:
        // apiService.actualizarTokenFCM(conductorId, token)
        Timber.d("$TAG: Token debería ser enviado al backend")
    }
}
