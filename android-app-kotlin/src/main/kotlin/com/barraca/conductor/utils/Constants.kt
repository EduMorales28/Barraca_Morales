package com.barraca.conductor.utils

/**
 * Constantes de la aplicación
 */
object Constants {

    // URLs
    const val BASE_URL = "http://localhost:3000/v1/"
    const val API_TIMEOUT_SECONDS = 30L

    // Preferencias
    const val PREFS_NAME = "LogisticaMorales"
    const val PREF_TOKEN = "auth_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_NAME = "user_name"

    // Estados de pedidos
    const val ESTADO_PENDIENTE = "pendiente"
    const val ESTADO_EN_RUTA = "en_ruta"
    const val ESTADO_PARCIAL = "parcial"
    const val ESTADO_ENTREGADO = "entregado"

    // Rutas de navegación
    const val ROUTE_PEDIDOS = "pedidos"
    const val ROUTE_PEDIDO_DETAIL = "pedido_detail/{pedidoId}"
    const val ROUTE_ENTREGA = "entrega/{pedidoId}"

    // Tamaño de paginación
    const val PAGE_SIZE = 20

    // Archivo temporal para fotos
    const val TEMP_PHOTO_FILENAME = "entrega_photo.jpg"
}

/**
 * Mensajes de error
 */
object ErrorMessages {
    const val NETWORK_ERROR = "Error de conexión. Verifica tu internet."
    const val VALIDATION_ERROR = "Por favor completa todos los campos requeridos."
    const val UNKNOWN_ERROR = "Ha ocurrido un error inesperado."
    const val INVALID_CREDENTIALS = "Email o contraseña incorrectos."
    const val SESSION_EXPIRED = "Tu sesión ha expirado. Por favor inicia sesión de nuevo."
}

/**
 * Patrones de validación
 */
object ValidationPatterns {
    val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
    val PHONE_PATTERN = Regex("^[0-9\\-\\+\\s()]{7,}$")
    val DNI_PATTERN = Regex("^[0-9]{6,10}$")
}
