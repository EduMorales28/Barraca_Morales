package com.barraca.conductor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.barraca.conductor.data.model.Pedido
import com.barraca.conductor.viewmodel.MapViewModel
import com.barraca.conductor.viewmodel.PedidoDetailViewModel
import com.barraca.conductor.viewmodel.PedidoDetailUiState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * ===============================================
 * EJEMPLOS: Google Maps en Android Kotlin
 * ===============================================
 *
 * 7 ejemplos listos para copiar y pegar en tu proyecto
 * Desde lo más simple hasta integración completa
 */

// ======== EJEMPLO 1: Mapa Simple ========
/**
 * Mapa básico mostrando un punto
 * Complejidad: ⭐ Muy simple
 * Líneas: 25
 */
@Composable
fun Ejemplo1_MapaSimple() {
    val latLng = LatLng(-34.603722, -58.381592) // Buenos Aires

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition(latLng, 15f, 0f, 0f)
        }
    ) {
        Marker(
            state = MarkerState(position = latLng),
            title = "Mi Ubicación"
        )
    }
}

// ======== EJEMPLO 2: Mapa con Marcador Interactivo ========
/**
 * Mapa con marcador que muestra info al hacer click
 * Complejidad: ⭐⭐ Simple
 * Líneas: 50
 */
@Composable
fun Ejemplo2_MapaconMarcadorInteractivo() {
    val pedido = Pedido(
        id = "001",
        numero = "12345",
        clienteId = "cli001",
        cliente = "Juan García",
        email = "juan@example.com",
        telefono = "+54 9 11 1234-5678",
        direccion = "Calle Principal 123, CABA",
        barrio = "San Telmo",
        latitud = -34.603722,
        longitud = -58.381592,
        montoTotal = 150.0,
        estado = "pendiente",
        fechaCreacion = "2026-03-28",
        fechaEntrega = null,
        items = emptyList()
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(
            LatLng(pedido.latitud, pedido.longitud),
            16f, 0f, 0f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(
                position = LatLng(pedido.latitud, pedido.longitud)
            ),
            title = pedido.cliente,
            snippet = pedido.direccion,
            onClick = { 
                println("Clicked: ${pedido.cliente}") 
                false 
            }
        )
    }
}

// ======== EJEMPLO 3: Centrar Mapa en Ubicación ========
/**
 * Botón para centrar automáticamente el mapa
 * Complejidad: ⭐⭐ Simple
 * Líneas: 80
 */
@Composable
fun Ejemplo3_CentrarMapaEnUbicacion() {
    val mapViewModel: MapViewModel = hiltViewModel()
    val latLng = LatLng(-34.603722, -58.381592)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(latLng, 15f, 0f, 0f)
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = latLng),
                title = "Destino"
            )
        }

        Button(
            onClick = {
                mapViewModel.centrarEnUbicacion(latLng, zoom = 18f, animate = true)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Centrar aquí")
        }
    }
}

// ======== EJEMPLO 4: Múltiples Marcadores ========
/**
 * Mapa con 3 pedidos diferentes
 * Complejidad: ⭐⭐ Simple
 * Líneas: 120
 */
@Composable
fun Ejemplo4_MultiplesMarcadores() {
    val pedidos = listOf(
        Pedido(
            id = "001", numero = "001",
            clienteId = "c1", cliente = "Juan García",
            email = "juan@ex.com", telefono = "123",
            direccion = "Calle 1", barrio = "San Telmo",
            latitud = -34.603, longitud = -58.381,
            montoTotal = 100.0, estado = "pendiente",
            fechaCreacion = "2026-03-28", fechaEntrega = null,
            items = emptyList()
        ),
        Pedido(
            id = "002", numero = "002",
            clienteId = "c2", cliente = "María López",
            email = "maria@ex.com", telefono = "456",
            direccion = "Calle 2", barrio = "La Boca",
            latitud = -34.640, longitud = -58.362,
            montoTotal = 200.0, estado = "pendiente",
            fechaCreacion = "2026-03-28", fechaEntrega = null,
            items = emptyList()
        ),
        Pedido(
            id = "003", numero = "003",
            clienteId = "c3", cliente = "Pedro Martínez",
            email = "pedro@ex.com", telefono = "789",
            direccion = "Calle 3", barrio = "Recoleta",
            latitud = -34.595, longitud = -58.395,
            montoTotal = 150.0, estado = "pendiente",
            fechaCreacion = "2026-03-28", fechaEntrega = null,
            items = emptyList()
        )
    )

    val avgLat = pedidos.map { it.latitud }.average()
    val avgLng = pedidos.map { it.longitud }.average()

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition(LatLng(avgLat, avgLng), 13f, 0f, 0f)
        }
    ) {
        pedidos.forEach { pedido ->
            Marker(
                state = MarkerState(
                    position = LatLng(pedido.latitud, pedido.longitud)
                ),
                title = pedido.cliente,
                snippet = pedido.direccion
            )
        }
    }
}

// ======== EJEMPLO 5: Mapa con Información Detallada ========
/**
 * Mapa que muestra info del pedido al hacer click
 * Complejidad: ⭐⭐⭐ Medio
 * Líneas: 150
 */
@Composable
fun Ejemplo5_MapaConInformacionDetallada() {
    val pedido = Pedido(
        id = "001", numero = "12345",
        clienteId = "c1", cliente = "Juan García",
        email = "juan@ex.com", telefono = "+54 9 11 1234-5678",
        direccion = "Calle Principal 123, CABA", barrio = "San Telmo",
        latitud = -34.603722, longitud = -58.381592,
        montoTotal = 150.0, estado = "pendiente",
        fechaCreacion = "2026-03-28", fechaEntrega = null,
        items = emptyList()
    )

    var mostrarInfo by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition(
                    LatLng(pedido.latitud, pedido.longitud),
                    16f, 0f, 0f
                )
            }
        ) {
            Marker(
                state = MarkerState(
                    position = LatLng(pedido.latitud, pedido.longitud)
                ),
                title = pedido.cliente,
                snippet = pedido.direccion,
                onClick = {
                    mostrarInfo = true
                    false
                }
            )
        }

        // Info Card
        if (mostrarInfo) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = pedido.cliente,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = pedido.direccion,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Barrio: ${pedido.barrio}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Monto: \$${pedido.montoTotal}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Lat: ${String.format("%.4f", pedido.latitud)}, " +
                                "Lng: ${String.format("%.4f", pedido.longitud)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    Button(
                        onClick = { mostrarInfo = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

// ======== EJEMPLO 6: Integración Completa con ViewModel ========
/**
 * Uso completo con MapViewModel y navegación
 * Complejidad: ⭐⭐⭐⭐ Avanzado
 * Líneas: 200
 */
@Composable
fun Ejemplo6_IntegracionCompleataConViewModel(
    pedidoId: String,
    onBackClick: () -> Unit = {}
) {
    val pedidoDetailViewModel: PedidoDetailViewModel = hiltViewModel()
    val mapViewModel: MapViewModel = hiltViewModel()
    
    val pedidoState = pedidoDetailViewModel.pedidoState.collectAsState()
    val selectedMarker = mapViewModel.selectedMarker.collectAsState()

    LaunchedEffect(pedidoId) {
        pedidoDetailViewModel.cargarPedido(pedidoId)
    }

    when (val state = pedidoState.value) {
        is PedidoDetailUiState.Success -> {
            val pedido = state.pedido
            val latLng = LatLng(pedido.latitud, pedido.longitud)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition(latLng, 16f, 0f, 0f)
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Mapa de ${pedido.cliente}") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Atrás"
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = latLng),
                            title = pedido.cliente,
                            snippet = pedido.direccion,
                            onClick = {
                                mapViewModel.seleccionarMarcador(
                                    com.barraca.conductor.viewmodel.MapMarker(
                                        id = pedido.id,
                                        latLng = latLng,
                                        title = pedido.cliente,
                                        snippet = pedido.direccion
                                    )
                                )
                                false
                            }
                        )
                    }

                    if (selectedMarker.value != null) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.9f)
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = selectedMarker.value!!.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = selectedMarker.value!!.snippet ?: "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        is PedidoDetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is PedidoDetailUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${(state as PedidoDetailUiState.Error).message}")
            }
        }
    }
}

// ======== EJEMPLO 7: Mini Mapa Reutilizable ========
/**
 * Componente pequeño para incrustar en otras pantallas
 * Complejidad: ⭐⭐ Simple
 * Líneas: 60
 */
@Composable
fun Ejemplo7_MiniMapaReutilizable(
    latitud: Double,
    longitud: Double,
    titulo: String = "Ubicación",
    onMapClick: () -> Unit = {}
) {
    val latLng = LatLng(latitud, longitud)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition(latLng, 15f, 0f, 0f)
            },
            properties = MapProperties(mapType = MapType.Normal),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                scrollGesturesEnabled = false,
                zoomGesturesEnabled = false
            ),
            onMapClick = { onMapClick() }
        ) {
            Marker(
                state = MarkerState(position = latLng),
                title = titulo
            )
        }

        // Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Toca para expandir",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.background(
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.small
                ).padding(8.dp),
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

// ======== BONUS: Usando los Ejemplos ========

/**
 * Cómo usar los ejemplos en tu proyecto:
 * 
 * 1. Copia el código del ejemplo que quieras
 * 2. Pégalo en tu archivo de screens
 * 3. Ajusta el nombre de la función
 * 4. Importa las dependencias necesarias
 * 5. Úsalo en tu navegación
 * 
 * Ejemplo:
 * 
 * navHost {...
 *     composable("mapa-simple") {
 *         Ejemplo1_MapaSimple()
 *     }
 * }
 * 
 * Luego en tu Android manifest o config:
 * navController.navigate("mapa-simple")
 */

/**
 * Referencia de datos de prueba:
 * 
 * Buenos Aires: -34.603722, -58.381592
 * San Telmo: -34.635, -58.371
 * La Boca: -34.640, -58.362
 * Recoleta: -34.595, -58.395
 * Caballito: -34.599, -58.415
 * Flores: -34.633, -58.453
 */
