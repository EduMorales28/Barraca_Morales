# Guía de Implementación - App Android Conductor

## Fases de Desarrollo

### ✅ Fase 1: Estructura Base (COMPLETADA)

- [x] Proyecto Kotlin con Gradle
- [x] MVVM Architecture setup
- [x] Hilt Dependency Injection
- [x] Data models y API service
- [x] Repository pattern
- [x] ViewModels con State management
- [x] Jetpack Compose UI setup
- [x] Navigation Compose

### 🔄 Fase 2: Integración de Servicios (PRÓXIMA)

- [ ] **Autenticación**
  - Implementar login screen
  - Almacenar token JWT en EncryptedSharedPreferences
  - Interceptor Retrofit para agregar token automático
  - Refresh token on expiration

- [ ] **Cámara**
  - Usar CameraX para capturar fotos
  - Guardar en caché local
  - Comprimir antes de enviar
  - Previsualizador de foto

- [ ] **Ubicación GPS**
  - Usar Fused Location Provider
  - Permisos en tiempo real
  - GPS en background (opcional)
  - Mostrar ubicación actual en mapa

- [ ] **Google Maps**
  - Composable de mapa interactivo
  - Marcadores para ubicación de entrega
  - Ruta entre conductor y cliente
  - Distancia estimada

### 🔄 Fase 3: Características Avanzadas

- [ ] **Room Database**
  - Caché local de pedidos
  - Sincronización offline-first
  - Migración de esquema

- [ ] **Notificaciones**
  - Firebase Cloud Messaging
  - Push notifications de pedidos
  - Local notifications para recordatorios

- [ ] **Multimedia**
  - Galería de fotos de entregas
  - Firma digital (opcional)
  - Grabación de notas de voz

### ✅ Fase 4: Testing (FINAL)

- [ ] Unit tests (ViewModels, Repository)
- [ ] Integration tests (API calls)
- [ ] UI tests (Espresso/Compose Test)
- [ ] E2E testing

---

## Implementación Paso a Paso

### 1. Login Screen

```kotlin
// Crear LoginViewModel
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    // Implementar login logic
}

// Crear LoginScreen composable
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    // Email + Password inputs
    // Submit button
    // Error handling
}
```

### 2. Integración de Cámara

```kotlin
// Usar CameraX
val imageCapture = ImageCapture.Builder().build()
val cameraProvider = ProcessCameraProvider.getInstance(context).get()

cameraProvider.bindToLifecycle(
    lifecycleOwner,
    cameraSelector,
    preview,
    imageCapture
)
```

### 3. Ubicación GPS

```kotlin
// Usar Fused Location Provider
val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

fusedLocationClient.lastLocation.addOnSuccessListener { location ->
    viewModel.setUbicacion(
        location.latitude,
        location.longitude
    )
}
```

### 4. Maps en Compose

```kotlin
@Composable
fun MapScreen(
    latitud: Double,
    longitud: Double
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(latitud, longitud),
                15f
            )
        }
    ) {
        Marker(
            position = LatLng(latitud, longitud),
            title = "Entrega"
        )
    }
}
```

---

## Testing

### Unit Test

```kotlin
@RunWith(JUnit4::class)
class PedidosListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PedidosListViewModel
    private val repository = mockk<PedidoRepository>()

    @Before
    fun setup() {
        viewModel = PedidosListViewModel(repository)
    }

    @Test
    fun `obtener pedidos actualiza estado a success`() = runTest {
        // Arrange
        val pedidos = listOf(mockk<Pedido>())
        coEvery { repository.obtenerMisPedidos() } returns Result.success(pedidos)

        // Act
        viewModel.cargarPedidos()

        // Assert
        val uiState = viewModel.uiState.value
        assert(uiState is PedidosListUiState.Success)
    }
}
```

### UI Test

```kotlin
@RunWith(AndroidJUnit4::class)
class PedidosScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pedidoCard_displaysTitulo() {
        composeTestRule.setContent {
            PedidosScreen(viewModel, {})
        }

        composeTestRule
            .onNodeWithText("Mis Pedidos")
            .assertExists()
    }
}
```

---

## Errores Comunes y Soluciones

### 1. "Hilt modules not generated"

**Problema**: No se generan los archivos de Hilt

**Solución**:
```bash
./gradlew clean
./gradlew build
```

### 2. "Composable not compiling"

**Problema**: Error de tipo en Composable

**Solución**: Verificar que retornas `Unit` implícito

### 3. "API calls timeout"

**Problema**: Timeout en Retrofit

**Solución**: Aumentar timeout en `ApiClient.kt`

```kotlin
.connectTimeout(60, TimeUnit.SECONDS)
.readTimeout(60, TimeUnit.SECONDS)
```

### 4. "Permisos denegados"

**Problema**: Cámara o GPS no funcionan

**Solución**: Implementar `ActivityResultContracts.RequestPermission()`

---

## Performance Optimization

### 1. LazyColumn en lugar de Column

✅ **Correcto**:
```kotlin
LazyColumn {
    items(pedidos) { pedido ->
        PedidoCard(pedido)
    }
}
```

❌ **Incorrecto**:
```kotlin
Column {
    pedidos.forEach { pedido ->
        PedidoCard(pedido)
    }
}
```

### 2. mutableStateOf vs mutableListOf

✅ **Correcto**:
```kotlin
val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
```

❌ **Incorrecto**:
```kotlin
val pedidos = mutableListOf<Pedido>()
```

### 3. Evitar recomposiciones innecesarias

✅ **Memoization**:
```kotlin
val filtered = remember {
    pedidos.filter { it.estado == "entregado" }
}
```

---

## Deployment

### 1. Versioning

En `build.gradle.kts`:

```kotlin
versionCode = 2  // Incrementar siempre
versionName = "1.1.0"  // Semantic versioning
```

### 2. Signing

Generar keystore:

```bash
keytool -genkey -v -keystore conductor-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias conductor-key
```

### 3. Play Store

1. Genera `app-release.aab`
2. Sube a Play Console
3. Completa app info (permisos, privacy policy)
4. Deploy gradualmente (25% → 50% → 100%)

---

## Documentación Complementaria

- [Android Architecture Components](https://developer.android.com/topic/architecture)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Hilt Documentation](https://dagger.dev/hilt)
- [Retrofit Tutorial](https://square.github.io/retrofit/)
- [Coroutines Guide](https://kotlinlang.org/docs/coroutines-overview.html)

---

**Última actualización**: Enero 2024
**Versión App**: 1.0.0
