package com.barraca.conductor.data.repository

import com.barraca.conductor.data.api.ApiClient
import com.barraca.conductor.services.CameraManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

/**
 * Repository específico para manejar upload de fotos
 */
class FotoRepository @Inject constructor() {

    private val apiService = ApiClient.apiService

    /**
     * Subir foto individual
     * @param file Archivo de foto
     * @param tipo Tipo de foto (entrega, cliente, etc)
     * @param referencia Referencia del pedido o contexto
     */
    suspend fun subirFoto(
        file: File,
        tipo: String = "entrega",
        referencia: String = ""
    ): Result<Map<String, String>> = try {
        if (!file.exists()) {
            return Result.failure(Exception("Archivo no existe: ${file.absolutePath}"))
        }

        // Crear multipart
        val requestBody = file.asRequestBody("image/jpeg".toMediaType())
        val photoPart = MultipartBody.Part.createFormData("foto", file.name, requestBody)

        val tipoBody = tipo.toRequestBody("text/plain".toMediaType())
        val referenciaBody = referencia.toRequestBody("text/plain".toMediaType())

        // Realizar request
        val response = apiService.subirFoto(
            foto = photoPart,
            tipo = tipoBody,
            referencia = referenciaBody
        )

        if (response.isSuccessful && response.body()?.success == true) {
            val data = response.body()?.data
            if (data != null) {
                Result.success(data)
            } else {
                Result.failure(Exception("Sin datos en respuesta"))
            }
        } else {
            val error = response.body()?.error?.message ?: "Error desconocido"
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Subir foto con reintentos
     */
    suspend fun subirFotoConReintentos(
        file: File,
        tipo: String = "entrega",
        referencia: String = "",
        maxIntentos: Int = 3
    ): Result<Map<String, String>> {
        var ultimoError: Exception? = null

        repeat(maxIntentos) { intento ->
            val resultado = subirFoto(file, tipo, referencia)

            resultado.onSuccess {
                return Result.success(it)
            }.onFailure { error ->
                ultimoError = Exception("Intento ${intento + 1} falló: ${error.message}")
            }
        }

        return Result.failure(
            ultimoError ?: Exception("Error desconocido después de $maxIntentos intentos")
        )
    }

    /**
     * Subir varias fotos en paralelo
     */
    suspend fun subirFotosMultiples(
        fotos: List<Pair<File, String>>, // File to tipo
        referencia: String = ""
    ): Result<List<Map<String, String>>> = try {
        val resultados = mutableListOf<Map<String, String>>()

        fotos.forEach { (file, tipo) ->
            val resultado = subirFoto(file, tipo, referencia)

            resultado.onSuccess { data ->
                resultados.add(data)
            }.onFailure { error ->
                return Result.failure(error)
            }
        }

        Result.success(resultados)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
