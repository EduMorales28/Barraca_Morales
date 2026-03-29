package com.barraca.conductor.data.repository

import com.barraca.conductor.data.api.ApiClient
import com.barraca.conductor.data.api.ActualizarPedidoRequest
import com.barraca.conductor.data.model.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

/**
 * Repository que maneja la lógica de obtención de datos
 * Implementa el patrón Repository para separar la lógica de datos
 */
class PedidoRepository @Inject constructor() {

    private val apiService = ApiClient.apiService

    // ==================== PEDIDOS ====================

    /**
     * Obtener lista de pedidos asignados al conductor
     * @return Result con lista de pedidos o error
     */
    suspend fun obtenerMisPedidos(
        estado: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<List<Pedido>> = try {
        val response = apiService.obtenerMisPedidos(estado, page, limit)

        if (response.isSuccessful && response.body()?.success == true) {
            val pedidos = response.body()?.data ?: emptyList()
            Result.success(pedidos)
        } else {
            val error = response.body()?.error?.message ?: "Error desconocido"
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Obtener detalle de un pedido
     */
    suspend fun obtenerPedidoDetalle(pedidoId: String): Result<Pedido> = try {
        val response = apiService.obtenerPedidoDetalle(pedidoId)

        if (response.isSuccessful && response.body()?.success == true) {
            val pedido = response.body()?.data
            if (pedido != null) {
                Result.success(pedido)
            } else {
                Result.failure(Exception("Pedido no encontrado"))
            }
        } else {
            val error = response.body()?.error?.message ?: "Error al obtener pedido"
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Actualizar estado de un pedido
     */
    suspend fun actualizarPedido(
        pedidoId: String,
        estado: String,
        observaciones: String? = null
    ): Result<Pedido> = try {
        val request = ActualizarPedidoRequest(
            estado = estado,
            observaciones = observaciones
        )

        val response = apiService.actualizarPedido(pedidoId, request)

        if (response.isSuccessful && response.body()?.success == true) {
            val pedido = response.body()?.data
            if (pedido != null) {
                Result.success(pedido)
            } else {
                Result.failure(Exception("Error actualizar pedido"))
            }
        } else {
            val error = response.body()?.error?.message ?: "Error desconocido"
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== ENTREGAS ====================

    /**
     * Registrar entrega con foto
     * @param pedidoId ID del pedido
     * @param recibidoPor Nombre de quien recibe
     * @param dniRecibidor DNI de quien recibe
     * @param observaciones Notas de entrega
     * @param latitud Latitud de entrega
     * @param longitud Longitud de entrega
     * @param cantidadLevantada Cantidad entregada
     * @param fotoFile Archivo de foto opcional
     */
    suspend fun registrarEntrega(
        pedidoId: String,
        recibidoPor: String,
        dniRecibidor: String,
        observaciones: String,
        latitud: Double,
        longitud: Double,
        cantidadLevantada: Int,
        fotoFile: File? = null
    ): Result<Entrega> = try {
        // Convertir parámetros a RequestBody
        val pedidoIdBody = pedidoId.toRequestBody("text/plain".toMediaType())
        val recibidoPorBody = recibidoPor.toRequestBody("text/plain".toMediaType())
        val dniBody = dniRecibidor.toRequestBody("text/plain".toMediaType())
        val observacionesBody = observaciones.toRequestBody("text/plain".toMediaType())
        val latitudBody = latitud.toString().toRequestBody("text/plain".toMediaType())
        val longitudBody = longitud.toString().toRequestBody("text/plain".toMediaType())
        val cantidadBody = cantidadLevantada.toString().toRequestBody("text/plain".toMediaType())

        // Preparar archivo de foto
        val fotoPart = if (fotoFile != null && fotoFile.exists()) {
            val requestBody = fotoFile.asRequestBody("image/jpeg".toMediaType())
            MultipartBody.Part.createFormData("foto", fotoFile.name, requestBody)
        } else {
            null
        }

        // Realizar request
        val response = apiService.registrarEntrega(
            pedidoIdBody,
            recibidoPorBody,
            dniBody,
            observacionesBody,
            latitudBody,
            longitudBody,
            cantidadBody,
            fotoPart
        )

        if (response.isSuccessful && response.body()?.success == true) {
            val entrega = response.body()?.data
            if (entrega != null) {
                Result.success(entrega)
            } else {
                Result.failure(Exception("Error registrar entrega"))
            }
        } else {
            val error = response.body()?.error?.message ?: "Error desconocido"
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Registrar solo la ubicación GPS (sin foto)
     */
    suspend fun registrarUbicacion(
        conductorId: String,
        latitud: Double,
        longitud: Double
    ): Result<Unit> = try {
        val request = UbicacionRequest(
            conductorId = conductorId,
            latitud = latitud,
            longitud = longitud,
            timestamp = System.currentTimeMillis().toString()
        )

        val response = apiService.registrarUbicacion(request)

        if (response.isSuccessful && response.body()?.success == true) {
            Result.success(Unit)
        } else {
            val error = response.body()?.error?.message ?: "Error registrar ubicación"
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== USUARIO ====================

    /**
     * Obtener perfil del conductor
     */
    suspend fun obtenerPerfil(): Result<Usuario> = try {
        val response = apiService.obtenerPerfil()

        if (response.isSuccessful && response.body()?.success == true) {
            val usuario = response.body()?.data
            if (usuario != null) {
                Result.success(usuario)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } else {
            val error = response.body()?.error?.message ?: "Error obtener perfil"
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Cerrar sesión
     */
    suspend fun logout(): Result<Unit> = try {
        val response = apiService.logout()

        if (response.isSuccessful && response.body()?.success == true) {
            Result.success(Unit)
        } else {
            val error = response.body()?.error?.message ?: "Error logout"
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
