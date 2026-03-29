package com.barraca.conductor.data.api

import com.barraca.conductor.data.model.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz Retrofit para todos los endpoints de la API
 * Contiene métodos para pedidos, entregas, ubicación, etc.
 */
interface ConductorApiService {

    // ==================== AUTENTICACIÓN ====================

    /**
     * Login del conductor
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    // ==================== PEDIDOS ====================

    /**
     * Obtener lista de pedidos asignados al conductor
     * @param estado opcional: filtrar por estado (pendiente, en_ruta, entregado)
     */
    @GET("conductor/mis-pedidos")
    suspend fun obtenerMisPedidos(
        @Query("estado") estado: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<Pedido>>>

    /**
     * Obtener detalle de un pedido específico
     */
    @GET("pedidos/{pedidoId}")
    suspend fun obtenerPedidoDetalle(
        @Path("pedidoId") pedidoId: String
    ): Response<ApiResponse<Pedido>>

    /**
     * Actualizar estado del pedido
     */
    @PUT("pedidos/{pedidoId}")
    suspend fun actualizarPedido(
        @Path("pedidoId") pedidoId: String,
        @Body request: ActualizarPedidoRequest
    ): Response<ApiResponse<Pedido>>

    // ==================== ENTREGAS ====================

    /**
     * Registrar entrega completada con foto
     * Usa multipart form data para enviar foto junto con datos
     */
    @Multipart
    @POST("entregas")
    suspend fun registrarEntrega(
        @Part("pedidoId") pedidoId: RequestBody,
        @Part("recibidoPor") recibidoPor: RequestBody,
        @Part("dniRecibidor") dniRecibidor: RequestBody,
        @Part("observaciones") observaciones: RequestBody,
        @Part("latitud") latitud: RequestBody,
        @Part("longitud") longitud: RequestBody,
        @Part("cantidadLevantada") cantidadLevantada: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<ApiResponse<Entrega>>

    /**
     * Registrar solo ubicación GPS (sin foto)
     */
    @POST("conductor/ubicacion")
    suspend fun registrarUbicacion(
        @Body request: UbicacionRequest
    ): Response<ApiResponse<Map<String, String>>>

    // ==================== USUARIO ====================

    /**
     * Obtener perfil del conductor logueado
     */
    @GET("conductor/perfil")
    suspend fun obtenerPerfil(): Response<ApiResponse<Usuario>>

    /**
     * Cerrar sesión
     */
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Map<String, String>>>

    // ==================== UPLOAD ====================

    /**
     * Subir foto individual (para pruebas o ajustes)
     */
    @Multipart
    @POST("upload/foto")
    suspend fun subirFoto(
        @Part foto: MultipartBody.Part,
        @Part("tipo") tipo: RequestBody = "entrega".toRequestBody("text/plain".toMediaType()),
        @Part("referencia") referencia: RequestBody = "".toRequestBody("text/plain".toMediaType())
    ): Response<ApiResponse<Map<String, String>>>

    // ==================== FIREBASE NOTIFICATIONS ====================

    /**
     * Actualizar token FCM del conductor
     * El backend lo usa para enviar notificaciones push
     * @param conductorId ID del conductor
     * @param tokenFCM Token de Firebase Cloud Messaging
     */
    @POST("conductor/{conductorId}/token-fcm")
    suspend fun actualizarTokenFCM(
        @Path("conductorId") conductorId: String,
        @Body body: TokenFCMRequest
    ): Response<ApiResponse<String>>
}

// ==================== REQUESTS ====================

/**
 * Request para login
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Request para actualizar pedido
 */
data class ActualizarPedidoRequest(
    val estado: String,
    val observaciones: String? = null
)

/**
 * Request para actualizar token FCM
 */
data class TokenFCMRequest(
    val token: String
)
