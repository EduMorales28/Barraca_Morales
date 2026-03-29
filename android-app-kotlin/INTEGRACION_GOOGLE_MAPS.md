# 📱 Guía de Integración: Google Maps en tu Navegación

## Paso 1: Verificar la Estructura Base

Ya tienes:
- ✅ `MapViewModel.kt` - Gestión de estado
- ✅ `MapScreen.kt` - Pantalla completa + componentes
- ✅ `GOOGLE_MAPS_SETUP.md` - Documentación detallada
- ✅ `EJEMPLOS_GOOGLE_MAPS.kt` - 7 ejemplos copy-paste
- ✅ Dependencias instaladas en `build.gradle.kts`

---

## Paso 2: Configurar API Key

### 2.1 Obtener la clave

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto (o usa uno existente)
3. Activa "Maps SDK for Android"
4. Crea una credencial "API Key"
5. Copia la clave

### 2.2 Agregar en AndroidManifest.xml

```xml
<application
    android:name=".ConductorApplication"
    ...>

    <!-- Google Maps API Key -->
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="AIzaSyD-XXXXXXXXXXXXXXXXXXXX" />

    <!-- Resto de tu configuración -->
    ...
</application>
```

⚠️ Reemplaza `AIzaSyD-XXXXXXXXXXXXXXXXXXXX` con tu clave real

---

## Paso 3: Actualizar Navegación

Abre tu archivo de navegación (probablemente `MainActivity.kt` o `NavHost`):

### 3.1 Si usas NavController composable:

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "pedidos"
    ) {
        // Pantalla principal de pedidos
        composable("pedidos") {
            PedidosScreen(
                onPedidoClick = { pedidoId ->
                    navController.navigate("pedido/$pedidoId")
                }
            )
        }

        // Detalle de pedido (ACTUALIZADO con mapa)
        composable(
            route = "pedido/{pedidoId}",
            arguments = listOf(
                navArgument("pedidoId") { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: return@composable
            val pedidoDetailViewModel: PedidoDetailViewModel = hiltViewModel()

            PedidoDetailScreen(
                pedidoId = pedidoId,
                viewModel = pedidoDetailViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onGoToEntrega = { id ->
                    navController.navigate("entrega/$id")
                },
                onGoToMapa = { id ->  // ← NUEVO
                    navController.navigate("mapa/$id")
                }
            )
        }

        // NUEVA RUTA: Pantalla de mapa completo
        composable(
            route = "mapa/{pedidoId}",
            arguments = listOf(
                navArgument("pedidoId") { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: return@composable
            
            // ViewModels
            val pedidoDetailViewModel: PedidoDetailViewModel = hiltViewModel()
            val mapViewModel: MapViewModel = hiltViewModel()
            
            // Cargar el pedido
            LaunchedEffect(pedidoId) {
                pedidoDetailViewModel.cargarPedido(pedidoId)
            }

            val pedidoState = pedidoDetailViewModel.pedidoState.collectAsState()

            // Mostrar pantalla según el estado
            when (val state = pedidoState.value) {
                is PedidoDetailUiState.Success -> {
                    MapScreen(
                        pedido = state.pedido,
                        viewModel = mapViewModel,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
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
                        val errorMessage = (state as PedidoDetailUiState.Error).message
                        Text("Error: $errorMessage")
                    }
                }
            }
        }

        // Pantalla de entrega (existente)
        composable(
            route = "entrega/{pedidoId}",
            arguments = listOf(
                navArgument("pedidoId") { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: return@composable
            val entregaViewModel: EntregaViewModel = hiltViewModel()

            EntregaScreen(
                pedidoId = pedidoId,
                viewModel = entregaViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
```

---

## Paso 4: Importes Necesarios

En cada archivo donde uses Google Maps, asegúrate de tener:

```kotlin
// MapViewModel.kt
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext

// MapScreen.kt
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.hilt.navigation.compose.hiltViewModel

// MainActivity.kt / NavHost
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
```

---

## Paso 5: Actualizar PedidoDetailViewModel (Si es necesario)

Tu `PedidoDetailViewModel` ya debería tener:

```kotlin
// Ya debería estar presente
fun cargarPedido(pedidoId: String) {
    viewModelScope.launch {
        // Cargar lógica
    }
}

// Ya debería tener estos estados
sealed class PedidoDetailUiState {
    object Loading : PedidoDetailUiState()
    data class Success(val pedido: Pedido) : PedidoDetailUiState()
    data class Error(val message: String) : PedidoDetailUiState()
}
```

✅ No necesita cambios si ya lo tienes así.

---

## Paso 6: Pruebas

### Test 1: Compilación
```bash
cd android-app-kotlin
./gradlew build
```

Debería compilar sin errores.

### Test 2: Visualización del Mapa
1. Abre la app
2. Navega a un pedido
3. Deberías ver un mini mapa en la pantalla de detalle
4. Click en el mini mapa → abre pantalla de mapa completo

### Test 3: Marcador Visible
En la pantalla de mapa:
- Deberías ver un marcador rojo en las coordenadas
- Click en marcador → muestra popup con nombre + dirección
- FAB "Ubicación" → centra en ubicación actual (si Google Play Services disponible)

---

## Paso 7: Casos Especiales

### Si no tienes Google Play Services en el emulador:

El mapa no mostrará imagen. Soluciones:
1. Usa emulador con "Google APIs" (no "Android API")
2. Prueba en dispositivo real
3. El mapa compilará pero mostrará "Google Play Services not available"

### Si quieres deshabilitar gestos en el mapa:

En `MapScreen.kt`, modifica:

```kotlin
GoogleMap(
    ...
    uiSettings = MapUiSettings(
        zoomControlsEnabled = false,      // Desabilita botones +/-
        myLocationButtonEnabled = false,  // Desabilita botón "Mi ubicación"
        scrollGesturesEnabled = false,    // Desabilita scroll
        zoomGesturesEnabled = false,      // Desabilita pinch
        rotationGesturesEnabled = false,  // Desabilita rotación
        tiltGesturesEnabled = false       // Desabilita inclinación
    )
)
```

### Si quieres múltiples marcadores:

Usa `MapaMultiplesPedidos` en lugar de `MapScreen`:

```kotlin
composable("ruta") {
    val mapViewModel: MapViewModel = hiltViewModel()
    val pedidosViewModel: PedidosListViewModel = hiltViewModel()
    
    val pedidosState = pedidosViewModel.pedidosState.collectAsState()
    
    when (val state = pedidosState.value) {
        is PedidosListUiState.Success -> {
            MapaMultiplesPedidos(
                pedidos = state.pedidos,
                viewModel = mapViewModel,
                onPedidoSelected = { pedidoId ->
                    navController.navigate("pedido/$pedidoId")
                }
            )
        }
        // ... loading/error states
    }
}
```

---

## Paso 8: Opcional - Ubicación en Tiempo Real

Si quieres mostrar la ubicación actual del conductor:

```kotlin
@Composable
fun MapScreenConUbicacionActual(
    pedido: Pedido,
    viewModel: MapViewModel,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Obtener ubicación actual
    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.actualizarUbicacionConductor(
                        LatLng(location.latitude, location.longitude)
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.e("MapScreen", "Permission denied: ${e.message}")
        }
    }

    // Resto del código igual
    MapScreen(
        pedido = pedido,
        viewModel = viewModel,
        onBackClick = onBackClick
    )
}
```

Requiere permisos en `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

✅ Ya están presentes en tu manifest.

---

## Paso 9: Compilar y Ejecutar

```bash
# Limpiar
./gradlew clean

# Compilar
./gradlew build

# Ejecutar en emulador
flutter run -d emulator-5554

# O si prefieres solo Android:
./gradlew installDebug
adb shell am start -n com.barraca.conductor/.MainActivity
```

---

## Paso 10: Verificar Integración

Checklist final:

- [ ] Compilación sin errores
- [ ] API Key configurada en AndroidManifest.xml
- [ ] Google Maps dependencia en build.gradle.kts
- [ ] MapViewModel creado y con Hilt
- [ ] MapScreen creado
- [ ] PedidoDetailScreen actualizado con `onGoToMapa`
- [ ] Rutas de navegación agregadas ("mapa/{pedidoId}")
- [ ] Mini mapa aparece en detalle del pedido
- [ ] Click en mini mapa lleva a pantalla completa
- [ ] Volver desde mapa completo vuelve a detalle
- [ ] Marcador visible en mapa
- [ ] Info popup funciona
- [ ] FAB para ubicación funciona (si Play Services)

---

## Troubleshooting Rápido

| Problema | Solución |
|----------|----------|
| Mapa en blanco | ✅ API Key en AndroidManifest |
|  | ✅ Google Play Services instalado |
|  | ✅ Conexión a internet |
| App no compila | ✅ Verifica imports |
|  | ✅ Verifica nombres de funciones |
| Navegación no funciona | ✅ Verifica ruta en NavHost |
|  | ✅ Verifica argumentos en navArgument |
| Marcador no aparece | ✅ Verifica lat/lng válidos |
|  | ✅ Verifica GoogleMap renderiza |
|  | ✅ Verifica Marker está dentro del GoogleMap{} |

---

## Referencia de Rutas

```
Pantalla           Ruta                NavController
─────────────────────────────────────────────────
Pedidos            "pedidos"           startDestination
Detalle            "pedido/{id}"       navigate("pedido/123")
Mapa ⭐ NUEVO      "mapa/{id}"         navigate("mapa/123")
Entrega            "entrega/{id}"      navigate("entrega/123")
```

---

## Recursos

- [GOOGLE_MAPS_SETUP.md](GOOGLE_MAPS_SETUP.md) - Documentación completa
- [RESUMEN_GOOGLE_MAPS.md](RESUMEN_GOOGLE_MAPS.md) - Overview técnico
- [EJEMPLOS_GOOGLE_MAPS.kt](EJEMPLOS_GOOGLE_MAPS.kt) - 7 ejemplos copy-paste

---

## Soporte

Si algo no funciona:

1. **Revisa los logs**: `adb logcat | grep "GoogleMap\|MapViewModel\|Maps"`
2. **Verifica la API Key**: Google Cloud Console → API Key
3. **Prueba con un pedido real**: Asegúrate que tiene lat/lng válidos
4. **Prueba en dispositivo real**: Algunos emuladores no tienen Google Play Services

---

**Estado**: ✅ Listo para integrar
**Fecha**: Marzo 2026
**Versión**: 1.0.0
