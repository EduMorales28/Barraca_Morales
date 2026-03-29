package com.barraca.conductor.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barraca.conductor.data.repository.FotoRepository
import com.barraca.conductor.services.CameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Estados para captura y upload de foto
 */
sealed class FotoUiState {
    object Idle : FotoUiState()
    object CapturingPhoto : FotoUiState()
    object CompressingPhoto : FotoUiState()
    object UploadingPhoto : FotoUiState()
    data class PhotoCaptured(val file: File) : FotoUiState()
    data class PhotoUploaded(val response: Map<String, String>) : FotoUiState()
    data class Error(val message: String) : FotoUiState()
}

/**
 * ViewModel para gestionar la captura y upload de fotos
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val fotoRepository: FotoRepository,
    private val context: Context
) : ViewModel() {

    private val cameraManager: CameraManager = CameraManager(context)

    // ==================== STATE ====================

    private val _fotoState = MutableStateFlow<FotoUiState>(FotoUiState.Idle)
    val fotoState: StateFlow<FotoUiState> = _fotoState.asStateFlow()

    private val _fotoCaptured = MutableStateFlow<File?>(null)
    val fotoCaptured: StateFlow<File?> = _fotoCaptured.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    // ==================== MÉTODOS PÚBLICOS ====================

    /**
     * Capturar foto con cámara
     */
    fun capturePhoto() {
        viewModelScope.launch {
            _fotoState.value = FotoUiState.CapturingPhoto

            val result = cameraManager.capturePhoto()

            result.onSuccess { file ->
                _fotoCaptured.value = file
                _fotoState.value = FotoUiState.PhotoCaptured(file)
            }.onFailure { exception ->
                _fotoState.value = FotoUiState.Error(
                    exception.message ?: "Error capturando foto"
                )
            }
        }
    }

    /**
     * Comprimir foto capturada
     */
    fun compressPhoto(quality: Int = 85) {
        val file = _fotoCaptured.value ?: return

        viewModelScope.launch {
            _fotoState.value = FotoUiState.CompressingPhoto

            val result = cameraManager.compressImage(file, quality)

            result.onSuccess { compressedFile ->
                _fotoCaptured.value = compressedFile
                _fotoState.value = FotoUiState.PhotoCaptured(compressedFile)
            }.onFailure { exception ->
                _fotoState.value = FotoUiState.Error(
                    exception.message ?: "Error comprimiendo foto"
                )
            }
        }
    }

    /**
     * Subir foto capturada a la API
     */
    fun uploadPhoto(
        tipo: String = "entrega",
        referencia: String = "",
        conReintentos: Boolean = true
    ) {
        val file = _fotoCaptured.value ?: run {
            _fotoState.value = FotoUiState.Error("No hay foto capturada")
            return
        }

        viewModelScope.launch {
            _fotoState.value = FotoUiState.UploadingPhoto
            _uploadProgress.value = 0f

            val result = if (conReintentos) {
                fotoRepository.subirFotoConReintentos(file, tipo, referencia)
            } else {
                fotoRepository.subirFoto(file, tipo, referencia)
            }

            result.onSuccess { response ->
                _uploadProgress.value = 1f
                _fotoState.value = FotoUiState.PhotoUploaded(response)
                // Limpiar después de 2 segundos
                kotlinx.coroutines.delay(2000)
                reset()
            }.onFailure { exception ->
                _fotoState.value = FotoUiState.Error(
                    exception.message ?: "Error subiendo foto"
                )
            }
        }
    }

    /**
     * Reset del estado
     */
    fun reset() {
        _fotoState.value = FotoUiState.Idle
        _fotoCaptured.value = null
        _uploadProgress.value = 0f
    }

    /**
     * Eliminar foto capturada
     */
    fun deletePhoto() {
        _fotoCaptured.value?.let { file ->
            if (cameraManager.deletePhotoFile(file)) {
                reset()
            }
        }
    }

    /**
     * Obtener tamaño de foto en MB
     */
    fun getPhotoSizeMB(): Double {
        return _fotoCaptured.value?.let { file ->
            cameraManager.getFileSizeMB(file)
        } ?: 0.0
    }
}
