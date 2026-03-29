package com.barraca.app.api

import com.barraca.app.LoginRequest
import com.barraca.app.LoginResponse
import com.barraca.app.Pedido
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("/mis-pedidos/{conductor_id}")
    suspend fun getMisPedidos(@Path("conductor_id") conductorId: Int): List<Pedido>

    @GET("/pedidos/{id}")
    suspend fun getPedidoDetalle(@Path("id") pedidoId: Int): Pedido

    @Multipart
    @POST("/entregas")
    suspend fun crearEntrega(
        @Part("pedido_id") pedidoId: Int,
        @Part("observaciones") observaciones: String,
        @Part foto: MultipartBody.Part
    )
}
