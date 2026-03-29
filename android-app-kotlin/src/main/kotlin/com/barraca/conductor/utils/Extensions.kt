package com.barraca.conductor.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extensiones útiles para la aplicación
 */

// ==================== STRINGS ====================

/**
 * Validar email
 */
fun String?.isValidEmail(): Boolean {
    if (this.isNullOrEmpty()) return false
    return ValidationPatterns.EMAIL_PATTERN.matches(this)
}

/**
 * Validar teléfono
 */
fun String?.isValidPhone(): Boolean {
    if (this.isNullOrEmpty()) return false
    return ValidationPatterns.PHONE_PATTERN.matches(this)
}

/**
 * Validar DNI
 */
fun String?.isValidDni(): Boolean {
    if (this.isNullOrEmpty()) return false
    return ValidationPatterns.DNI_PATTERN.matches(this)
}

/**
 * Capitalizar primera letra
 */
fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}

// ==================== NÚMEROS ====================

/**
 * Formato de moneda
 */
fun Double.formatCurrency(): String {
    return "$${String.format("%.2f", this)}"
}

/**
 * Formato de porcentaje
 */
fun Double.formatPercentage(): String {
    return "${String.format("%.1f", this)}%"
}

// ==================== FECHAS ====================

/**
 * Formato de fecha DD/MM/YYYY
 */
fun Long.formatDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return format.format(date)
}

/**
 * Formato de hora HH:MM
 */
fun Long.formatTime(): String {
    val date = Date(this)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Diferencia de tiempo legible (ej: "hace 5 minutos")
 */
fun Long.timeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < 60000 -> "Justo ahora"
        diff < 3600000 -> "${diff / 60000} minutos atrás"
        diff < 86400000 -> "${diff / 3600000} horas atrás"
        diff < 604800000 -> "${diff / 86400000} días atrás"
        else -> "${diff / 604800000} semanas atrás"
    }
}

// ==================== CONTEXTO ====================

/**
 * Verificar si hay conexión a internet
 */
fun Context.isOnline(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/**
 * Get IO dispatcher para operaciones de I/O
 */
fun getIODispatcher(): CoroutineDispatcher = Dispatchers.IO

/**
 * Get Default dispatcher para operaciones de CPU
 */
fun getDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

/**
 * Get Main dispatcher para actualizar UI
 */
fun getMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

// ==================== COLECCIONES ====================

/**
 * Agrupar lista por propiedad
 */
inline fun <T, K> List<T>.groupByProperty(selector: (T) -> K): Map<K, List<T>> {
    val result = linkedMapOf<K, MutableList<T>>()
    for (element in this) {
        val key = selector(element)
        val list = result.getOrPut(key) { mutableListOf() }
        list.add(element)
    }
    return result
}

/**
 * Obtener primer elemento o null
 */fun <T> List<T>?.firstOrNull(): T? {
    return this?.firstOrNull()
}

// ==================== RESULT ====================

/**
 * Extension para Result<T>
 */
inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (isSuccess) block(getOrNull()!!)
    return this
}

inline fun <T> Result<T>.onFailure(block: (Throwable) -> Unit): Result<T> {
    if (isFailure) block(exceptionOrNull()!!)
    return this
}
