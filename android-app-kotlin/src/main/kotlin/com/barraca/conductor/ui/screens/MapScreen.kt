package com.barraca.conductor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.barraca.conductor.data.model.Pedido
import com.barraca.conductor.viewmodel.MapMarker
import com.barraca.conductor.viewmodel.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * Pantalla de mapa completa para visualizar ubicaciones de pedidos
 * 
 * Características:
 * - Mostrar marcador con lat/lng del pedido
 * - Centrar cámara en la ubicación
 * - Información popup con detalles del pedido
 * - Botón para ir a ubicación actual
 */
@Composable
fun MapScreen(
    pedido: Pedido,
    viewModel: MapViewModel,
    onBackClick: () -> Unit = {}
) {
    val markers = viewModel.markers.collectAsState()
    val selectedMarker = viewModel.selectedMarker.collectAsState()
    val conductorLocation = viewModel.conductorLocation.collectAsState()
    val zoomLevel = viewModel.zoomLevel.collectAsState()
    val isAnimating = viewModel.isAnimating.collectAsState()

    // Configurar el marcador del pedido cuando se carga
    LaunchedEffect(pedido) {
        val marker = viewModel.crearMarcadorPedido(
            pedidoId = pedido.id,
            latLng = LatLng(pedido.latitud, pedido.longitud),
            clienteNombre = pedido.cliente,
            direccion = pedido.direccion
        )
        viewModel.agregarMarcador(marker)
        viewModel.centrarEnUbicacion(
            LatLng(pedido.latitud, pedido.longitud),
            zoom = 16f,
            animate = true
        )
    }

    // Estado inicial de la cámara
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(
            target = LatLng(pedido.latitud, pedido.longitud),
            zoom = 16f
        )
    }

    // Animar la cámara cuando se solicita centrado
    LaunchedEffect(zoomLevel.value, isAnimating.value) {
        if (isAnimating.value) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        target = LatLng(pedido.latitud, pedido.longitud),
                        zoom = zoomLevel.value
                    )
                ),
                durationMs = 800
            )
            viewModel.animacionCompletada()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Entrega") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Mapa
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Marcar ubicación del pedido
                Marker(
                    state = MarkerState(position = LatLng(pedido.latitud, pedido.longitud)),
                    title = pedido.cliente,
                    snippet = pedido.direccion,
                    onClick = {
                        viewModel.seleccionarMarcador(
                            MapMarker(
                                id = pedido.id,
                                latLng = LatLng(pedido.latitud, pedido.longitud),
                                title = pedido.cliente,
                                snippet = pedido.direccion
                            )
                        )
                        false // No consumir el evento
                    }
                )

                // Marcar ubicación del conductor si está disponible
                conductorLocation.value?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Tu ubicación",
                        snippet = "Posición actual",
                        onClick = { false }
                    )
                }
            }

            // InfoBox con información del marcador seleccionado
            if (selectedMarker.value != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = selectedMarker.value!!.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (selectedMarker.value!!.snippet != null) {
                            Text(
                                text = selectedMarker.value!!.snippet!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Lat: ${String.format("%.4f", pedido.latitud)}, " +
                                    "Lng: ${String.format("%.4f", pedido.longitud)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Botón flotante para ir a ubicación actual
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = { viewModel.centrarEnConductor() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Mi ubicación",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Botón para volver al marcador del pedido
            if (selectedMarker.value?.id != pedido.id) {
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp, 80.dp, 16.dp, 16.dp),
                    onClick = { viewModel.centrarEnMarcador(pedido.id) },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Ir a pedido",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}

/**
 * Composable compacto de mapa para integrar en otras pantallas
 * Muestra un pequeño mapa con marcador, puede hacer click para ir a pantalla completa
 * 
 * Uso:
 * ```
 * MiniMapaPedido(
 *     pedido = pedido,
 *     modifier = Modifier.height(300.dp),
 *     onMapClick = { navController.navigate("mapa/$pedidoId") }
 * )
 * ```
 */
@Composable
fun MiniMapaPedido(
    pedido: Pedido,
    modifier: Modifier = Modifier,
    onMapClick: () -> Unit = {}
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(
            target = LatLng(pedido.latitud, pedido.longitud),
            zoom = 15f
        )
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = com.google.maps.android.compose.MapType.Normal),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                scrollGesturesEnabled = false,
                zoomGesturesEnabled = false,
                rotationGesturesEnabled = false,
                tiltGesturesEnabled = false
            ),
            onMapClick = { onMapClick() }
        ) {
            Marker(
                state = MarkerState(position = LatLng(pedido.latitud, pedido.longitud)),
                title = pedido.cliente,
                snippet = pedido.direccion
            )
        }

        // Overlay semi-transparente con instrucción
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Toca para ver mapa completo",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(8.dp)
            )
        }
    }
}

/**
 * Composable para mostrar múltiples marcadores (útil para listas de pedidos)
 */
@Composable
fun MapaMultiplesPedidos(
    pedidos: List<Pedido>,
    viewModel: MapViewModel,
    modifier: Modifier = Modifier,
    onPedidoSelected: (String) -> Unit = {}
) {
    val markers = viewModel.markers.collectAsState()
    val selectedMarker = viewModel.selectedMarker.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        // Calcular centro entre todos los pedidos
        if (pedidos.isNotEmpty()) {
            val avgLat = pedidos.map { it.latitud }.average()
            val avgLng = pedidos.map { it.longitud }.average()
            position = CameraPosition(
                target = LatLng(avgLat, avgLng),
                zoom = 13f
            )
        }
    }

    // Cargar marcadores cuando cambian los pedidos
    LaunchedEffect(pedidos) {
        val newMarkers = pedidos.map { pedido ->
            viewModel.crearMarcadorPedido(
                pedidoId = pedido.id,
                latLng = LatLng(pedido.latitud, pedido.longitud),
                clienteNombre = pedido.cliente,
                direccion = pedido.direccion
            )
        }
        viewModel.agregarMarcadores(newMarkers)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        pedidos.forEach { pedido ->
            Marker(
                state = MarkerState(position = LatLng(pedido.latitud, pedido.longitud)),
                title = pedido.cliente,
                snippet = pedido.direccion,
                onClick = {
                    viewModel.seleccionarMarcador(
                        MapMarker(
                            id = pedido.id,
                            latLng = LatLng(pedido.latitud, pedido.longitud),
                            title = pedido.cliente,
                            snippet = pedido.direccion
                        )
                    )
                    onPedidoSelected(pedido.id)
                    false
                }
            )
        }
    }
}
