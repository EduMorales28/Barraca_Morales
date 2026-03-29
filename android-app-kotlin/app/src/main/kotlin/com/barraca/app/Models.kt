package com.barraca.app

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String
)

data class Pedido(
    val id: Int,
    val cliente: String,
    val direccion: String,
    val lat: Double,
    val lng: Double,
    val estado: String,
    val levantado: String,
    val conductor_id: Int?,
    val conductor_nombre: String?,
    val items: List<ItemPedido> = emptyList()
)

data class ItemPedido(
    val id: Int,
    val pedido_id: Int,
    val nombre: String,
    val cantidad: Int
)

data class EntregaRequest(
    val pedido_id: Int,
    val observaciones: String,
    // foto se envía en multipart
)
