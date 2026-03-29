package com.barraca.conductor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barraca.conductor.data.model.Pedido
import com.barraca.conductor.data.repository.PedidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles para la lista de pedidos
 */
sealed class PedidosListUiState {
    object Loading : PedidosListUiState()
    data class Success(val pedidos: List<Pedido>) : PedidosListUiState()
    data class Error(val message: String) : PedidosListUiState()
}

/**
 * ViewModel para la pantalla de lista de pedidos
 * Maneja la lógica de obtención y filtrado de pedidos
 */
@HiltViewModel
class PedidosListViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    // ==================== STATE ====================

    private val _uiState = MutableStateFlow<PedidosListUiState>(PedidosListUiState.Loading)
    val uiState: StateFlow<PedidosListUiState> = _uiState.asStateFlow()

    private val _selectedEstado = MutableStateFlow<String?>(null)
    val selectedEstado: StateFlow<String?> = _selectedEstado.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ==================== MÉTODOS ====================

    /**
     * Cargar lista de pedidos
     */
    fun cargarPedidos(estado: String? = null) {
        viewModelScope.launch {
            _uiState.value = PedidosListUiState.Loading
            _isRefreshing.value = true

            val result = pedidoRepository.obtenerMisPedidos(estado = estado)

            result.onSuccess { pedidos ->
                _uiState.value = PedidosListUiState.Success(pedidos)
            }.onFailure { exception ->
                _uiState.value = PedidosListUiState.Error(
                    exception.message ?: "Error desconocido"
                )
            }

            _isRefreshing.value = false
        }
    }

    /**
     * Filtrar por estado
     */
    fun filtrarPorEstado(estado: String) {
        _selectedEstado.value = if (_selectedEstado.value == estado) null else estado
        cargarPedidos(estado = _selectedEstado.value)
    }

    /**
     * Reintentar carga
     */
    fun reintentar() {
        cargarPedidos(estado = _selectedEstado.value)
    }

    /**
     * Inicializar - cargar datos al crear el ViewModel
     */
    init {
        cargarPedidos()
    }
}
