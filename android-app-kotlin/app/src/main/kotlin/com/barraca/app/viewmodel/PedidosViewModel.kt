package com.barraca.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barraca.app.Pedido
import com.barraca.app.api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PedidosViewModel(context: Context) : ViewModel() {
    private val apiService = ApiClient.getApiService()
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

    private val _selectedPedido = MutableStateFlow<Pedido?>(null)
    val selectedPedido: StateFlow<Pedido?> = _selectedPedido

    private val _uiState = MutableStateFlow<PedidosUiState>(PedidosUiState.Idle)
    val uiState: StateFlow<PedidosUiState> = _uiState

    fun loadPedidos(conductorId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = PedidosUiState.Loading
                val pedidos = apiService.getMisPedidos(conductorId)
                _pedidos.value = pedidos
                _uiState.value = PedidosUiState.Success
            } catch (e: Exception) {
                _uiState.value = PedidosUiState.Error(e.message ?: "Error al cargar pedidos")
            }
        }
    }

    fun selectPedido(pedidoId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = PedidosUiState.Loading
                val pedido = apiService.getPedidoDetalle(pedidoId)
                _selectedPedido.value = pedido
                _uiState.value = PedidosUiState.Success
            } catch (e: Exception) {
                _uiState.value = PedidosUiState.Error(e.message ?: "Error al cargar detalles")
            }
        }
    }

    fun crearEntrega(pedidoId: Int, observaciones: String, imagePath: String) {
        viewModelScope.launch {
            try {
                _uiState.value = PedidosUiState.Loading
                
                val file = java.io.File(imagePath)
                val requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/*"), file)
                val multipart = okhttp3.MultipartBody.Part.createFormData("foto", file.name, requestBody)
                
                apiService.crearEntrega(pedidoId, observaciones, multipart)
                _uiState.value = PedidosUiState.Success
                loadPedidos(prefs.getInt("user_id", 0))
            } catch (e: Exception) {
                _uiState.value = PedidosUiState.Error(e.message ?: "Error al enviar entrega")
            }
        }
    }
}

sealed class PedidosUiState {
    object Idle : PedidosUiState()
    object Loading : PedidosUiState()
    object Success : PedidosUiState()
    data class Error(val message: String) : PedidosUiState()
}
