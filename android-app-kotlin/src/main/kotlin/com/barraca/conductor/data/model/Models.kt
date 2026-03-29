package com.barraca.conductor.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// ==================== PEDIDO ====================

/**
 * Data class para representar un pedido asignado al conductor
 */
data class Pedido(
    @SerializedName("id")
    val id: String,

    @SerializedName("numero")
    val numero: String,

    @SerializedName("clienteId")
    val clienteId: String,

    @SerializedName("cliente")
    val cliente: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("telefono")
    val telefono: String,

    @SerializedName("direccion")
    val direccion: String,

    @SerializedName("barrio")
    val barrio: String,

    @SerializedName("latitud")
    val latitud: Double,

    @SerializedName("longitud")
    val longitud: Double,

    @SerializedName("montoTotal")
    val montoTotal: Double,

    @SerializedName("estado")
    val estado: String, // pendiente, en_ruta, parcial, entregado

    @SerializedName("fechaCreacion")
    val fechaCreacion: String,

    @SerializedName("fechaEntrega")
    val fechaEntrega: String?,

    @SerializedName("items")
    val items: List<ItemPedido>,

    @SerializedName("observaciones")
    val observaciones: String? = null,

    @SerializedName("fotoEntrega")
    val fotoEntrega: String? = null
) : Serializable

// ==================== ITEM PEDIDO ====================

/**
 * Representa un artículo dentro de un pedido
 */
data class ItemPedido(
    @SerializedName("id")
    val id: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String?,

    @SerializedName("cantidad")
    val cantidad: Int,

    @SerializedName("precioUnitario")
    val precioUnitario: Double,

    @SerializedName("estado")
    val estado: String // pendiente, levantado, entregado

) : Serializable

// ==================== ENTREGA ====================

/**
 * Data class para registrar una entrega completada
 */
data class Entrega(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("pedidoId")
    val pedidoId: String,

    @SerializedName("conductorId")
    val conductorId: String,

    @SerializedName("fechaEntrega")
    val fechaEntrega: String,

    @SerializedName("horaEntrega")
    val horaEntrega: String,

    @SerializedName("cantidadLevantada")
    val cantidadLevantada: Int,

    @SerializedName("recibidoPor")
    val recibidoPor: String,

    @SerializedName("dniRecibidor")
    val dniRecibidor: String,

    @SerializedName("latitud")
    val latitud: Double,

    @SerializedName("longitud")
    val longitud: Double,

    @SerializedName("observaciones")
    val observaciones: String? = null,

    @SerializedName("fotoUrl")
    val fotoUrl: String? = null,

    @SerializedName("firmaUrl")
    val firmaUrl: String? = null
)

// ==================== RESPUESTA API ====================

/**
 * Respuesta genérica del servidor
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: ErrorResponse? = null
)

/**
 * Estructura de error del servidor
 */
data class ErrorResponse(
    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("details")
    val details: String? = null
)

// ==================== REQUESTS ====================

/**
 * Request para marcar entrega completada
 */
data class EntregaRequest(
    val pedidoId: String,
    val recibidoPor: String,
    val dniRecibidor: String,
    val observaciones: String,
    val latitud: Double,
    val longitud: Double,
    val cantidadLevantada: Int
)

/**
 * Request para actualizar ubicación GPS
 */
data class UbicacionRequest(
    val conductorId: String,
    val latitud: Double,
    val longitud: Double,
    val timestamp: String
)

// ==================== USUARIO ====================

/**
 * Datos del conductor autenticado
 */
data class Usuario(
    @SerializedName("id")
    val id: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("telefono")
    val telefono: String,

    @SerializedName("rol")
    val rol: String, // conductor, admin, cliente

    @SerializedName("estado")
    val estado: String, // activo, inactivo

    @SerializedName("vehiculo")
    val vehiculo: Vehiculo?
)

/**
 * Información del vehículo del conductor
 */
data class Vehiculo(
    @SerializedName("id")
    val id: String,

    @SerializedName("modelo")
    val modelo: String,

    @SerializedName("placa")
    val placa: String,

    @SerializedName("capacidad")
    val capacidad: String,

    @SerializedName("año")
    val ano: Int
)

// ==================== RESPUESTA LOGIN ====================

/**
 * Respuesta del endpoint de login
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("expiresIn")
    val expiresIn: Long,

    @SerializedName("usuario")
    val usuario: Usuario
)
