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
 * Estados posibles para el detalle de un pedido
 */
sealed class PedidoDetailUiState {
    object Loading : PedidoDetailUiState()
    data class Success(val pedido: Pedido) : PedidoDetailUiState()
    data class Error(val message: String) : PedidoDetailUiState()
}

/**
 * Estados para la acción de actualizar/marcar entregado
 */
sealed class ActualizarPedidoUiState {
    object Idle : ActualizarPedidoUiState()
    object Loading : ActualizarPedidoUiState()
    object Success : ActualizarPedidoUiState()
    data class Error(val message: String) : ActualizarPedidoUiState()
}

/**
 * ViewModel para la pantalla de detalle de un pedido
 */
@HiltViewModel
class PedidoDetailViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    // ==================== STATE ====================

    private val _pedidoState = MutableStateFlow<PedidoDetailUiState>(PedidoDetailUiState.Loading)
    val pedidoState: StateFlow<PedidoDetailUiState> = _pedidoState.asStateFlow()

    private val _actualizarState = MutableStateFlow<ActualizarPedidoUiState>(
        ActualizarPedidoUiState.Idle
    )
    val actualizarState: StateFlow<ActualizarPedidoUiState> = _actualizarState.asStateFlow()

    // ==================== MÉTODOS ====================

    /**
     * Cargar detalle de un pedido
     */
    fun cargarPedido(pedidoId: String) {
        viewModelScope.launch {
            _pedidoState.value = PedidoDetailUiState.Loading

            val result = pedidoRepository.obtenerPedidoDetalle(pedidoId)

            result.onSuccess { pedido ->
                _pedidoState.value = PedidoDetailUiState.Success(pedido)
            }.onFailure { exception ->
                _pedidoState.value = PedidoDetailUiState.Error(
                    exception.message ?: "Error desconocido"
                )
            }
        }
    }

    /**
     * Marcar pedido como en ruta
     */
    fun marcarEnRuta(pedidoId: String) {
        actualizarPedidoEstado(pedidoId, "en_ruta")
    }

    /**
     * Marcar pedido como parcialmente entregado
     */
    fun marcarParcial(pedidoId: String) {
        actualizarPedidoEstado(pedidoId, "parcial")
    }

    /**
     * Actualizar estado del pedido
     */
    private fun actualizarPedidoEstado(
        pedidoId: String,
        estado: String,
        observaciones: String = ""
    ) {
        viewModelScope.launch {
            _actualizarState.value = ActualizarPedidoUiState.Loading

            val result = pedidoRepository.actualizarPedido(
                pedidoId = pedidoId,
                estado = estado,
                observaciones = observaciones.takeIf { it.isNotEmpty() }
            )

            result.onSuccess { pedidoActualizado ->
                _actualizarState.value = ActualizarPedidoUiState.Success
                _pedidoState.value = PedidoDetailUiState.Success(pedidoActualizado)
            }.onFailure { exception ->
                _actualizarState.value = ActualizarPedidoUiState.Error(
                    exception.message ?: "Error al actualizar"
                )
            }
        }
    }

    /**
     * Limpiar estado de actualización
     */
    fun limpiarEstadoActualizacion() {
        _actualizarState.value = ActualizarPedidoUiState.Idle
    }
}
