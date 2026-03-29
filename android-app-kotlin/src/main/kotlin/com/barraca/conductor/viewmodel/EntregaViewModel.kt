package com.barraca.conductor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barraca.conductor.data.model.Entrega
import com.barraca.conductor.data.repository.PedidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Estados para el registro de entrega
 */
sealed class RegistrarEntregaUiState {
    object Idle : RegistrarEntregaUiState()
    object Loading : RegistrarEntregaUiState()
    data class Success(val entrega: Entrega) : RegistrarEntregaUiState()
    data class Error(val message: String) : RegistrarEntregaUiState()
}

/**
 * Estados para la captura de foto
 */
sealed class CapturaFotoUiState {
    object Idle : CapturaFotoUiState()
    object Capturing : CapturaFotoUiState()
    data class Success(val file: File) : CapturaFotoUiState()
    data class Error(val message: String) : CapturaFotoUiState()
}

/**
 * ViewModel para la pantalla de entrega (tomar foto, observaciones, etc)
 */
@HiltViewModel
class EntregaViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    // ==================== STATE ====================

    private val _registrarState = MutableStateFlow<RegistrarEntregaUiState>(
        RegistrarEntregaUiState.Idle
    )
    val registrarState: StateFlow<RegistrarEntregaUiState> = _registrarState.asStateFlow()

    private val _capturaFotoState = MutableStateFlow<CapturaFotoUiState>(
        CapturaFotoUiState.Idle
    )
    val capturaFotoState: StateFlow<CapturaFotoUiState> = _capturaFotoState.asStateFlow()

    private val _fotoCapturada = MutableStateFlow<File?>(null)
    val fotoCapturada: StateFlow<File?> = _fotoCapturada.asStateFlow()

    private val _observaciones = MutableStateFlow("")
    val observaciones: StateFlow<String> = _observaciones.asStateFlow()

    private val _recibidoPor = MutableStateFlow("")
    val recibidoPor: StateFlow<String> = _recibidoPor.asStateFlow()

    private val _dniRecibidor = MutableStateFlow("")
    val dniRecibidor: StateFlow<String> = _dniRecibidor.asStateFlow()

    private val _cantidadLevantada = MutableStateFlow(0)
    val cantidadLevantada: StateFlow<Int> = _cantidadLevantada.asStateFlow()

    private val _latitud = MutableStateFlow(0.0)
    val latitud: StateFlow<Double> = _latitud.asStateFlow()

    private val _longitud = MutableStateFlow(0.0)
    val longitud: StateFlow<Double> = _longitud.asStateFlow()

    // ==================== MÉTODOS SETTERS ====================

    fun setFotoCapturada(file: File) {
        _fotoCapturada.value = file
        _capturaFotoState.value = CapturaFotoUiState.Success(file)
    }

    fun resetearCaptura() {
        _fotoCapturada.value = null
        _capturaFotoState.value = CapturaFotoUiState.Idle
    }

    fun marcarFotoCapturando() {
        _capturaFotoState.value = CapturaFotoUiState.Capturing
    }

    fun marcarErrorCaptura(message: String) {
        _capturaFotoState.value = CapturaFotoUiState.Error(message)
    }

    fun setObservaciones(text: String) {
        _observaciones.value = text
    }

    fun setRecibidoPor(text: String) {
        _recibidoPor.value = text
    }

    fun setDniRecibidor(text: String) {
        _dniRecibidor.value = text
    }

    fun setCantidadLevantada(cantidad: Int) {
        _cantidadLevantada.value = cantidad
    }

    fun setUbicacion(latitud: Double, longitud: Double) {
        _latitud.value = latitud
        _longitud.value = longitud
    }

    fun limpiarFormulario() {
        _fotoCapturada.value = null
        _observaciones.value = ""
        _recibidoPor.value = ""
        _dniRecibidor.value = ""
        _cantidadLevantada.value = 0
        _registrarState.value = RegistrarEntregaUiState.Idle
    }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Validar formulario antes de enviar
     */
    private fun validarFormulario(): Boolean {
        return _recibidoPor.value.isNotEmpty() &&
                _dniRecibidor.value.isNotEmpty() &&
                _cantidadLevantada.value > 0 &&
                _latitud.value != 0.0 &&
                _longitud.value != 0.0
    }

    /**
     * Registrar entrega completada
     */
    fun registrarEntrega(
        pedidoId: String,
        cantidadTotal: Int
    ) {
        if (!validarFormulario()) {
            _registrarState.value = RegistrarEntregaUiState.Error(
                "Debes llenar todos los campos requeridos"
            )
            return
        }

        viewModelScope.launch {
            _registrarState.value = RegistrarEntregaUiState.Loading

            val result = pedidoRepository.registrarEntrega(
                pedidoId = pedidoId,
                recibidoPor = _recibidoPor.value,
                dniRecibidor = _dniRecibidor.value,
                observaciones = _observaciones.value,
                latitud = _latitud.value,
                longitud = _longitud.value,
                cantidadLevantada = _cantidadLevantada.value,
                fotoFile = _fotoCapturada.value
            )

            result.onSuccess { entrega ->
                _registrarState.value = RegistrarEntregaUiState.Success(entrega)
            }.onFailure { exception ->
                _registrarState.value = RegistrarEntregaUiState.Error(
                    exception.message ?: "Error al registrar entrega"
                )
            }
        }
    }

    /**
     * Registrar solo ubicación (sin esperar a la entrega completa)
     */
    fun registrarUbicacion(conductorId: String) {
        viewModelScope.launch {
            pedidoRepository.registrarUbicacion(
                conductorId = conductorId,
                latitud = _latitud.value,
                longitud = _longitud.value
            )
        }
    }
}
