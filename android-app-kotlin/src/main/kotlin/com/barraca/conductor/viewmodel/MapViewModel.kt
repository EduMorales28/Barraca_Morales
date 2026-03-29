package com.barraca.conductor.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Data class para representar un marcador en el mapa
 */
data class MapMarker(
    val id: String,
    val latLng: LatLng,
    val title: String,
    val snippet: String? = null,
    val icon: Int? = null // Drawable resource ID
)

/**
 * ViewModel para gestionar el mapa
 * Maneja:
 * - Posición de la cámara
 * - Marcadores
 * - Ubicación actual del conductor
 * - Zoom y animaciones
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ==================== STATE ====================

    /**
     * Marcadores a mostrar en el mapa
     */
    private val _markers = MutableStateFlow<List<MapMarker>>(emptyList())
    val markers: StateFlow<List<MapMarker>> = _markers.asStateFlow()

    /**
     * Marcador seleccionado
     */
    private val _selectedMarker = MutableStateFlow<MapMarker?>(null)
    val selectedMarker: StateFlow<MapMarker?> = _selectedMarker.asStateFlow()

    /**
     * Ubicación actual del conductor (si está disponible)
     */
    private val _conductorLocation = MutableStateFlow<LatLng?>(null)
    val conductorLocation: StateFlow<LatLng?> = _conductorLocation.asStateFlow()

    /**
     * Nivel de zoom actual
     */
    private val _zoomLevel = MutableStateFlow(15f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    /**
     * Indica si el mapa debe estar en animación
     */
    private val _isAnimating = MutableStateFlow(false)
    val isAnimating: StateFlow<Boolean> = _isAnimating.asStateFlow()

    // ==================== MÉTODOS ====================

    /**
     * Agregar un marcador al mapa
     */
    fun agregarMarcador(marker: MapMarker) {
        val currentMarkers = _markers.value.toMutableList()
        
        // Si el marcador ya existe, lo reemplaza
        val index = currentMarkers.indexOfFirst { it.id == marker.id }
        if (index != -1) {
            currentMarkers[index] = marker
        } else {
            currentMarkers.add(marker)
        }
        
        _markers.value = currentMarkers
    }

    /**
     * Agregar múltiples marcadores
     */
    fun agregarMarcadores(newMarkers: List<MapMarker>) {
        _markers.value = newMarkers
    }

    /**
     * Remover un marcador por ID
     */
    fun removerMarcador(markerId: String) {
        _markers.value = _markers.value.filter { it.id != markerId }
    }

    /**
     * Limpiar todos los marcadores
     */
    fun limpiarMarcadores() {
        _markers.value = emptyList()
        _selectedMarker.value = null
    }

    /**
     * Seleccionar un marcador
     */
    fun seleccionarMarcador(marker: MapMarker) {
        _selectedMarker.value = marker
    }

    /**
     * Centrar cámara en una ubicación específica
     * Útil para centrar en una dirección de entrega
     */
    fun centrarEnUbicacion(
        latLng: LatLng,
        zoom: Float = 16f,
        animate: Boolean = true
    ) {
        _zoomLevel.value = zoom
        if (animate) {
            _isAnimating.value = true
        }
    }

    /**
     * Centrar cámara en un marcador específico
     */
    fun centrarEnMarcador(markerId: String) {
        val marker = _markers.value.find { it.id == markerId }
        if (marker != null) {
            centrarEnUbicacion(marker.latLng)
            seleccionarMarcador(marker)
        }
    }

    /**
     * Centrar cámara en la ubicación actual del conductor
     */
    fun centrarEnConductor() {
        val location = _conductorLocation.value
        if (location != null) {
            centrarEnUbicacion(location, zoom = 18f)
        }
    }

    /**
     * Obtener la posición de cámara para un marcador
     * Útil para CameraPositionState
     */
    fun getCameraPosition(marker: MapMarker): CameraPosition {
        return CameraPosition(marker.latLng, _zoomLevel.value, 0f, 0f)
    }

    /**
     * Obtener la posición de cámara para un LatLng específico
     */
    fun getCameraPosition(latLng: LatLng): CameraPosition {
        return CameraPosition(latLng, _zoomLevel.value, 0f, 0f)
    }

    /**
     * Actualizar ubicación del conductor
     * Se usa cuando obtienes ubicación real mediante GPS
     */
    fun actualizarUbicacionConductor(latLng: LatLng) {
        _conductorLocation.value = latLng
    }

    /**
     * Cambiar nivel de zoom
     */
    fun cambiarZoom(newZoom: Float) {
        _zoomLevel.value = newZoom.coerceIn(5f, 21f)
    }

    /**
     * Indicar fin de animación
     */
    fun animacionCompletada() {
        _isAnimating.value = false
    }

    /**
     * Crear un marcador para un pedido
     * Usado en la pantalla de detalle del pedido
     */
    fun crearMarcadorPedido(
        pedidoId: String,
        latLng: LatLng,
        clienteNombre: String,
        direccion: String
    ): MapMarker {
        return MapMarker(
            id = pedidoId,
            latLng = latLng,
            title = clienteNombre,
            snippet = direccion
        )
    }
}
