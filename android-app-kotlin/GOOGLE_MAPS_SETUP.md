# 🗺️ Google Maps en Android Kotlin - Guía Completa

## Visión General

Has integrado **Google Maps Compose** en tu app Android Kotlin. Esto te permite:

✅ Mostrar mapas interactivos con Jetpack Compose
✅ Colocar marcadores en ubicaciones específicas (pedidos)
✅ Centrar cámara en pedidos o ubicación actual
✅ Mostrar información emergente al hacer clic
✅ Usar mapas compactos en otras pantallas

---

## 📦 Dependencias Incluidas

```gradle
// Google Maps
implementation 'com.google.maps.android:maps-compose:2.14.1'
implementation 'com.google.android.gms:play-services-maps:18.2.0'
implementation 'com.google.android.gms:play-services-location:21.1.0'
```

✅ **Ya están instaladas** en tu `build.gradle.kts`

---

## 🔑 Configuración de API Key

### Paso 1: Obtener tu API Key de Google

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Activa la API "Maps SDK for Android"
4. Crea una credencial de tipo "API Key"
5. Copia la clave

### Paso 2: Agregar en AndroidManifest.xml

```xml
<application>
    <!-- Google Maps API Key -->
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="TU_CLAVE_AQUI" />
    
    <!-- Resto de tu aplicación -->
</application>
```

### Paso 3: Restricciones de Seguridad

⚠️ **IMPORTANTE**: En producción, restringe tu clave a:
- Aplicaciones Android específicamente
- Certificado SHA-1 de tu app

---

## 🎯 Componentes Creados

### 1. MapViewModel

**Ubicación**: `viewmodel/MapViewModel.kt`

**Responsabilidades**:
- Gestionar marcadores
- Controlar posición de la cámara
- Manejar zooms y animaciones
- Almacenar ubicación del conductor

**Métodos principales**:

```kotlin
// Agregar un marcador
fun agregarMarcador(marker: MapMarker)

// Centrar cámara en una ubicación
fun centrarEnUbicacion(latLng: LatLng, zoom: Float = 16f, animate: Boolean = true)

// Centrar en un marcador específico
fun centrarEnMarcador(markerId: String)

// Actualizar ubicación actual del conductor
fun actualizarUbicacionConductor(latLng: LatLng)

// Crear marcador para un pedido
fun crearMarcadorPedido(pedidoId: String, latLng: LatLng, clienteNombre: String, direccion: String): MapMarker
```

---

### 2. MapScreen (Pantalla Completa)

**Ubicación**: `ui/screens/MapScreen.kt`

**Características**:
- Vista completa del mapa
- Marcador centrado en el pedido
- InfoBox con detalles del pedido
- Botones para ir a ubicación actual
- Animaciones suave

**Uso**:

```kotlin
@Composable
fun MapScreen(
    pedido: Pedido,
    viewModel: MapViewModel,
    onBackClick: () -> Unit = {}
)

// Ejemplo de uso en navegación:
MapScreen(
    pedido = pedido,
    viewModel = mapViewModel,
    onBackClick = { navController.popBackStack() }
)
```

---

### 3. MiniMapaPedido (Componente Compacto)

**Ubicación**: `ui/screens/MapScreen.kt`

**Características**:
- Mapa pequeño embebido en otras pantallas
- No permite interacción (gestos deshabilitados)
- Muestra marcador del pedido
- Click para ir a vista completa

**Uso en PedidoDetailScreen**:

```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
) {
    MiniMapaPedido(
        pedido = pedido,
        modifier = Modifier.fillMaxSize(),
        onMapClick = { onGoToMapa?.invoke(pedido.id) }
    )
}
```

---

### 4. MapaMultiplesPedidos (Vista de Ruta)

**Ubicación**: `ui/screens/MapScreen.kt`

**Características**:
- Mostrar múltiples pedidos en un mapa
- Centrar automáticamente en todos ellos
- Click en marcador para seleccionar pedido

**Uso**:

```kotlin
MapaMultiplesPedidos(
    pedidos = listOf(pedido1, pedido2, pedido3),
    viewModel = mapViewModel,
    modifier = Modifier.fillMaxSize(),
    onPedidoSelected = { pedidoId ->
        navController.navigate("pedido/$pedidoId")
    }
)
```

---

## 🔌 Integración en Navegación

### Actualizar tu NavHost

```kotlin
val navController = rememberNavController()

NavHost(
    navController = navController,
    startDestination = "pedidos"
) {
    composable("pedidos") {
        PedidosScreen(
            onPedidoClick = { pedidoId ->
                navController.navigate("pedido/$pedidoId")
            }
        )
    }

    composable("pedido/{pedidoId}") { backStackEntry ->
        val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: return@composable
        val pedidoDetailViewModel: PedidoDetailViewModel = hiltViewModel()
        val mapViewModel: MapViewModel = hiltViewModel()

        PedidoDetailScreen(
            pedidoId = pedidoId,
            viewModel = pedidoDetailViewModel,
            onBackClick = { navController.popBackStack() },
            onGoToEntrega = { pedidoId ->
                navController.navigate("entrega/$pedidoId")
            },
            onGoToMapa = { pedidoId ->
                navController.navigate("mapa/$pedidoId")
            }
        )
    }

    composable("mapa/{pedidoId}") { backStackEntry ->
        val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: return@composable
        val detailViewModel: PedidoDetailViewModel = hiltViewModel()
        val mapViewModel: MapViewModel = hiltViewModel()

        // Cargar el pedido
        LaunchedEffect(pedidoId) {
            detailViewModel.cargarPedido(pedidoId)
        }

        val pedidoState = detailViewModel.pedidoState.collectAsState()

        when (val state = pedidoState.value) {
            is PedidoDetailUiState.Success -> {
                MapScreen(
                    pedido = state.pedido,
                    viewModel = mapViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            is PedidoDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PedidoDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${(state as PedidoDetailUiState.Error).message}")
                }
            }
        }
    }

    composable("entrega/{pedidoId}") { backStackEntry ->
        val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: return@composable
        val entregaViewModel: EntregaViewModel = hiltViewModel()

        EntregaScreen(
            pedidoId = pedidoId,
            viewModel = entregaViewModel,
            onBackClick = { navController.popBackStack() }
        )
    }
}
```

---

## 📍 Casos de Uso

### Caso 1: Ver Mapa de un Pedido Individual

```kotlin
// Cuando el user abre un pedido y quiere ver el mapa
MapScreen(
    pedido = pedidoActual,
    viewModel = mapViewModel,
    onBackClick = { navController.popBackStack() }
)

// Automáticamente:
// 1. Carga marcador con coordenadas del pedido
// 2. Centra cámara en esa ubicación (zoom 16)
// 3. Muestra nombre del cliente y dirección
// 4. Agrega botón para ver ubicación actual
```

### Caso 2: Mini Mapa en Detalle del Pedido

```kotlin
// En PedidoDetailScreen ya está integrado:
MiniMapaPedido(
    pedido = pedido,
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp),
    onMapClick = { 
        navController.navigate("mapa/${pedido.id}")
    }
)

// Usuario puede:
// 1. Ver preview del mapa sin dejar la pantalla
// 2. Click para ir a mapa completo
```

### Caso 3: Mostrar Ruta de Múltiples Pedidos

```kotlin
// Vista de ruta del día
MapaMultiplesPedidos(
    pedidos = pedidosDelDia,
    viewModel = mapViewModel,
    modifier = Modifier.fillMaxSize(),
    onPedidoSelected = { pedidoId ->
        navController.navigate("pedido/$pedidoId")
    }
)

// Usuario puede:
// 1. Ver todos los pedidos en el mapa
// 2. Click en un marcador para ver detalles
// 3. Planificar mejor la ruta
```

---

## 🚀 Características Avanzadas

### 1. Actualizar Ubicación del Conductor en Tiempo Real

```kotlin
// En tu screen o ViewModel
val mapViewModel: MapViewModel = hiltViewModel()

// Cuando obtienes ubicación GPS
LaunchedEffect(Unit) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            mapViewModel.actualizarUbicacionConductor(
                LatLng(location.latitude, location.longitude)
            )
        }
    }
}

// El mapa automáticamente mostrará:
// 1. Tu ubicación actual como marcador azul
// 2. Botón FAB para centrar en ti
```

### 2. Animar Cámara

```kotlin
// Zoom suave a una ubicación
mapViewModel.centrarEnUbicacion(
    latLng = LatLng(pedido.latitud, pedido.longitud),
    zoom = 18f, // zoom más cercano
    animate = true // anima suavemente
)

// La animación tarda ~800ms automáticamente
```

### 3. Múltiples Marcadores Personalizados

```kotlin
// Crear marcador personalizado
val marcador = MapMarker(
    id = "pedido-001",
    latLng = LatLng(-34.603722, -58.381592),
    title = "Juan García",
    snippet = "Calle Principal 123, CABA",
    icon = R.drawable.ic_marker_delivery // Icono personalizado
)

mapViewModel.agregarMarcador(marcador)
```

### 4. Controlar Interacciones del Mapa

En `MapScreen.kt`, puedes modificar `MapUiSettings` para:

```kotlin
MapUiSettings(
    zoomControlsEnabled = true,      // Botones +/-
    myLocationButtonEnabled = false,  // Botón "Mi ubicación"
    scrollGesturesEnabled = true,     // Drag el mapa
    zoomGesturesEnabled = true,       // Pinch para zoom
    rotationGesturesEnabled = false,  // Rotar (deshabilitado)
    tiltGesturesEnabled = false       // Inclinar (deshabilitado)
)
```

---

## 🎨 Personalización Visual

### Cambiar Tipo de Mapa

```kotlin
GoogleMap(
    properties = MapProperties(
        mapType = com.google.maps.android.compose.MapType.Satellite // Satélite
        // Opciones: Normal, Hybrid, Satellite, Terrain
    )
)
```

### Colores de Marcadores

```kotlin
Marker(
    state = MarkerState(position = latLng),
    title = "Mi Marcador",
    // No hay soporte directo para color en Compose
    // Solución: usa icono personalizado con color
)
```

### Sombras y Efectos

Los marcadores automáticamente se animan cuando:
- Se agregan al mapa
- El usuario interactúa con ellos
- El mapa se recarga

---

## ⚡ Rendimiento & Optimizaciones

### 1. Lazy Loading de Marcadores

```kotlin
// Cargar solo marcadores dentro del viewport
fun cargarMarcadoresVisibles(bounds: LatLngBounds) {
    val marcadoresFiltrados = pedidos
        .filter { it.latitud in bounds.southwest.latitude..bounds.northeast.latitude }
        .map { crearMarcadorPedido(it) }
    
    viewModel.agregarMarcadores(marcadoresFiltrados)
}
```

### 2. Caché de Imágenes del Mapa

Google automáticamente cachéa tiles. No necesitas hacer nada.

### 3. Limpieza de Recursos

```kotlin
// Cuando salgas de la pantalla
DisposableEffect(Unit) {
    onDispose {
        mapViewModel.limpiarMarcadores()
    }
}
```

---

## 🧪 Testing

### Test de Navegación al Mapa

```kotlin
@Test
fun test_click_mapa_navega_a_pantalla_mapa() {
    // Dado un pedido visible
    composeTestRule.setContent {
        PedidoDetailScreen(
            pedidoId = "123",
            viewModel = pedidoDetailViewModel,
            onGoToMapa = { pedidoId ->
                // Verificar que se llama con el ID correcto
                assert(pedidoId == "123")
            }
        )
    }

    // Cuando hace click en el mapa
    composeTestRule.onNodeWithText("Mapa de ubicación").performClick()
    
    // Entonces se llama el callback
}
```

---

## ☑️ Checklist de Implementación

- [ ] Configurar Google Cloud Console
- [ ] Obtener API Key
- [ ] Agregar API Key en AndroidManifest.xml
- [ ] Compilar y probar en emulador/dispositivo
- [ ] Verificar que MapViewModel inyección funciona
- [ ] Verificar que MapScreen se muestra
- [ ] Verificar que MiniMapaPedido aparece en detalle
- [ ] Probar navegación entre pantallas
- [ ] Probar click en mapa abre vista completa
- [ ] Probar que marcador muestra información correcta
- [ ] Probar que FAB para "Mi ubicación" funciona
- [ ] Probar con múltiples pedidos

---

## 🐛 Troubleshooting

### Error: "Google PlayServices no instalado"

```
Error: Cannot load library...
```

**Solución**: Asegúrate de usar emulador con Google Play Services o dispositivo real.

### Error: "API Key inválida"

```
Error: MapsInitializationException
```

**Solución**:
1. Verifica la clave en Google Cloud Console
2. Asegúrate de que la API esté habilitada
3. Verifica las restriccciones de clave

### Mapa en blanco

```
Canvas is blank, no features loaded
```

**Soluciones**:
1. Verifica la API Key
2. Compila con `flutter clean && flutter pub get`
3. Reinicia el emulador
4. Verifica coordinadas (rango válido)

### App lenta con muchos marcadores

```
FPS drop cuando hay 100+ marcadores
```

**Soluciones**:
1. Aplicar lazy loading (cargar solo visibles)
2. Agrupar marcadores cercanos (clustering)
3. Reducir frecuencia de actualizaciones

---

## 📚 Documentación Adicional

- [Maps Compose en GitHub](https://github.com/googlemaps/android-maps-compose)
- [Google Maps Android API Docs](https://developers.google.com/maps/documentation/android-sdk)
- [Jetpack Compose Guide](https://developer.android.com/jetpack/compose)

---

## 🎯 Próximos Pasos

### Sugerencia 1: Obtener Ubicación en Tiempo Real

Integra `com.google.android.gms:play-services-location` para obtener GPS del conductor:

```kotlin
val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

fun obtenerUbicacionActual() {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            mapViewModel.actualizarUbicacionConductor(
                LatLng(location.latitude, location.longitude)
            )
        }
    }
}
```

### Sugerencia 2: Clustering de Marcadores

Si tienes 50+ pedidos, muestra grupos en vez de todos los marcadores:

```kotlin
// Instalar MarkerUtils clustering
implementation 'com.google.maps.android:maps-ktx:5.0.0'

// Usar ClusterManager para agrupar automáticamente
```

### Sugerencia 3: Polyline entre Pedidos

Dibuja líneas entre pedidos para visualizar ruta:

```kotlin
Polyline(
    points = listOf(
        LatLng(pedido1.latitud, pedido1.longitud),
        LatLng(pedido2.latitud, pedido2.longitud),
        LatLng(pedido3.latitud, pedido3.longitud)
    ),
    color = Color.Blue,
    width = 10f
)
```

---

## 📊 Tabla de Referencia Rápida

| Tarea | Método | Ubicación |
|-------|--------|-----------|
| Mostrar marcador | `agregarMarcador()` | MapViewModel |
| Centrar en ubicación | `centrarEnUbicacion()` | MapViewModel |
| Ver mapa completo | `MapScreen()` | MapScreen.kt |
| Mini mapa | `MiniMapaPedido()` | MapScreen.kt |
| Múltiples pedidos | `MapaMultiplesPedidos()` | MapScreen.kt |
| Ubicación conductor | `actualizarUbicacionConductor()` | MapViewModel |
| Info marcador | `selectedMarker` | MapViewModel StateFlow |

---

**Estado**: ✅ Ready to Use
**Versión**: 1.0.0
**Última actualización**: Marzo 2026
