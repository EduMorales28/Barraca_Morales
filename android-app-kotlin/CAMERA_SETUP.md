# Guía Completa: Cámara & Upload de Fotos con Multipart

## 📋 Tabla de Contenidos

1. [Resumen de Components](#resumen)
2. [Flujo Completo](#flujo-completo)
3. [Permisos en Runtime](#permisos)
4. [Uso en UI (Composables)](#uso-en-ui)
5. [Manejo de Errores](#manejo-errores)
6. [Ejemplos Prácticos](#ejemplos-prácticos)

---

## 🏗️ Resumen de Components

### 1. **CameraManager** (`services/CameraManager.kt`)

Encargado de toda la lógica de CameraX:

```kotlin
// Inicializar cámara
cameraManager.startCamera(
    lifecycleOwner = lifecycleOwner,
    previewSurfaceProvider = previewView.surfaceProvider,
    onCameraReady = { /* UI list */ },
    onError = { exception -> /* handle error */ }
)

// Capturar foto (suspend function)
val result = cameraManager.capturePhoto()

// Comprimir imagen
val compressed = cameraManager.compressImage(
    file,
    quality = 85,
    maxWidth = 1920,
    maxHeight = 1080
)

// Obtener tamaño
val sizeMB = cameraManager.getFileSizeMB(file)

// Eliminar archivo
cameraManager.deletePhotoFile(file)
```

### 2. **FotoRepository** (`data/repository/FotoRepository.kt`)

Abstracción para upload con manejo de errores:

```kotlin
// Subir foto simple
val result = fotoRepository.subirFoto(file, tipo = "entrega", referencia = "pedido123")

// Subir con reintentos automáticos
val result = fotoRepository.subirFotoConReintentos(file, maxIntentos = 3)

// Subir múltiples fotos
val result = fotoRepository.subirFotosMultiples(
    listOf(
        file1 to "entrega",
        file2 to "cliente",
        file3 to "producto"
    ),
    referencia = "pedido123"
)
```

### 3. **CameraViewModel** (`viewmodel/CameraViewModel.kt`)

State management reactivo:

```kotlin
// Estados posibles
- FotoUiState.Idle
- FotoUiState.CapturingPhoto
- FotoUiState.CompressingPhoto
- FotoUiState.UploadingPhoto
- FotoUiState.PhotoCaptured(file)
- FotoUiState.PhotoUploaded(response)
- FotoUiState.Error(message)

// Usar en UI
val fotoState = viewModel.fotoState.collectAsState()
val fotoCaptured = viewModel.fotoCaptured.collectAsState()
val uploadProgress = viewModel.uploadProgress.collectAsState()

// Métodos
viewModel.capturePhoto()
viewModel.compressPhoto(quality = 85)
viewModel.uploadPhoto(tipo = "entrega", conReintentos = true)
viewModel.deletePhoto()
```

### 4. **PermissionHelper** (`utils/PermissionHelper.kt`)

Manejo de permisos en tiempo real:

```kotlin
val permissionHelper = PermissionHelper(context)

// Verificar permiso
val hasCamera = permissionHelper.hasPermission(PermissionHelper.CAMERA)

// Verificar múltiples
val hasAll = permissionHelper.hasPermissions(
    PermissionHelper.CAMERA,
    PermissionHelper.ACCESS_FINE_LOCATION
)

// Composable para solicitar
CameraPermissionComposable(
    onPermissionGranted = { /* enable camera */ },
    onPermissionDenied = { /* show error */ },
    content = { /* show camera */ }
)
```

---

## 🔄 Flujo Completo

```
┌─────────────────┐
│   User Click    │ "Tomar Foto"
│    Button       │
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│  RequestPermission  │ Solicitar CAMERA
│  (Runtime)          │
└────────┬────────────┘
         │
         ├─── DENEGADO ──▶ Mostrar Error
         │
         └─── CONCEDIDO ──┐
                          ▼
                   ┌──────────────────┐
                   │  CameraScreen    │
                   │  Preview + Button│
                   └────────┬─────────┘
                            │
                            ▼
                   ┌──────────────────┐
                   │  CapturePhoto()  │ CapturingPhoto
                   │  CameraX         │
                   └────────┬─────────┘
                            │
         ┌──────────────────┼──────────────────┐
         │                  │                  │
      SUCCESS            FAIL              TIMEOUT
         │                  │                  │
         ▼                  ▼                  ▼
    ┌────────┐         ┌─────────┐      ┌──────────┐
    │ File   │         │ Error   │      │ Error    │
    │ Created│         │ Dialog  │      │ Timeout  │
    └────┬───┘         └─────────┘      └──────────┘
         │
         ▼
    ┌─────────────────┐
    │ CompressImage   │ CompressingPhoto
    │ (Opcional)      │
    └────┬────────────┘
         │
         ▼
    ┌─────────────────┐
    │ PhotoCaptured   │ Mostrar preview
    │ State           │
    └────┬────────────┘
         │
         ▼
    ┌──────────────────┐
    │ User Confirms    │ "Subir Foto"
    │ or Retake        │
    └────┬──────────────┘
         │
   ┌─────┴──────┐
   │             │
 RETAKE      CONFIRM
   │             │
   │             ▼
   │      ┌──────────────────┐
   │      │ uploadPhoto()    │ UploadingPhoto
   │      │ Multipart Request│
   │      └────┬─────────────┘
   │           │
   │      ┌────┼──────────┐
   │      │    │          │
   │      │ SUCCESS    ERROR/RETRY
   │      │    │          │
   │      │    ▼          ▼
   │      │ ┌────────┐ ┌─────────┐
   │      │ │Uploaded│ │Error or │
   │      │ │Success │ │Retry    │
   │      │ └────────┘ └─────────┘
   │      │
   └──────┘

```

---

## 🔐 Permisos en Runtime

### 1. AndroidManifest.xml

Los permisos ya están definidos:

```xml
<!-- Ya configurado -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### 2. Solicitar en Runtime (Compose)

```kotlin
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        // Mostrar CameraScreen
    } else {
        // Mostrar error
    }
}

LaunchedEffect(Unit) {
    launcher.launch(Manifest.permission.CAMERA)
}
```

### 3. Con PermissionHelper

```kotlin
val permissionHelper = PermissionHelper(context)

if (permissionHelper.hasPermission(PermissionHelper.CAMERA)) {
    // Usar cámara
} else {
    // Solicitar permiso
}
```

---

## 🎨 Uso en UI (Composables)

### Opción 1: CameraScreen Simple

```kotlin
@Composable
fun MiPantallaEntrega(
    viewModel: EntregaViewModel
) {
    var mostrarCamera by remember { mutableStateOf(false) }
    val fotoCapturada = viewModel.fotoCapturada.collectAsState()

    if (mostrarCamera) {
        CameraScreen(
            onPhotoTaken = { file ->
                viewModel.setFotoCapturada(file)
                mostrarCamera = false
            },
            onBackClick = { mostrarCamera = false },
            onError = { error -> /* handle */ }
        )
    } else {
        Column {
            // Mostrar foto o botón
            if (fotoCapturada.value != null) {
                Text("Foto capturada: ${fotoCapturada.value?.name}")
            }
            
            Button(onClick = { mostrarCamera = true }) {
                Text("Tomar Foto")
            }
        }
    }
}
```

### Opción 2: CameraScreen Con Permisos

```kotlin
@Composable
fun MiPantallaEntregaAvanzada(
    viewModel: CameraViewModel
) {
    val fotoState = viewModel.fotoState.collectAsState()
    var mostrarCamera by remember { mutableStateOf(false) }

    if (mostrarCamera) {
        CameraScreenWithPermissions(
            onPhotoTaken = { file ->
                // Comprimir automáticamente
                viewModel.compressPhoto(quality = 85)
                mostrarCamera = false
            },
            onBackClick = { mostrarCamera = false },
            onError = { error -> 
                // Mostrar snackbar con error
            }
        )
    }

    Column {
        when (val state = fotoState.value) {
            is FotoUiState.PhotoCaptured -> {
                Text("Foto: ${state.file.name}")
                DosFilas {
                    Button(onClick = { viewModel.uploadPhoto() }) {
                        Text("Subir")
                    }
                    Button(onClick = { viewModel.deletePhoto() }) {
                        Text("Recapturar")
                    }
                }
            }
            is FotoUiState.UploadingPhoto -> {
                CircularProgressIndicator()
            }
            is FotoUiState.PhotoUploaded -> {
                Text("Subida exitosa!")
            }
            is FotoUiState.Error -> {
                Text("Error: ${state.message}")
            }
            else -> {
                Button(onClick = { mostrarCamera = true }) {
                    Text("Tomar Foto")
                }
            }
        }
    }
}
```

---

## ⚠️ Manejo de Errores

### 1. Por Tipo de Error

```kotlin
when (val state = fotoState.value) {
    is FotoUiState.Error -> {
        when {
            state.message.contains("permiso", ignoreCase = true) -> {
                // Mostrar diálogo de permisos
                AlertDialog(title = "Permiso de Cámara",
                           text = "Se requiere acceso a la cámara")
            }
            state.message.contains("inicialización", ignoreCase = true) -> {
                // Error de hardware
                AlertDialog(title = "Error de Cámara",
                           text = "No se pudo inicializar la cámara")
            }
            state.message.contains("comprimiendo", ignoreCase = true) -> {
                // Error de procesamiento
                AlertDialog(title = "Error de Procesamiento",
                           text = "No se pudo procesar la imagen")
            }
            state.message.contains("subiendo", ignoreCase = true) -> {
                // Error de red/API
                AlertDialog(title = "Error de Subida",
                           text = "No se pudo subir la foto. Verifica tu conexión")
            }
            else -> {
                // Error genérico
                AlertDialog(title = "Error", text = state.message)
            }
        }
    }
}
```

### 2. Reintentos Automáticos

```kotlin
// Reintentar 3 veces automáticamente
viewModel.uploadPhoto(conReintentos = true)
```

El repositorio maneja automáticamente:
- Intento 1, 2, 3
- Espera entre reintentos
- Mensajes como "Intento 1 falló: ..."

---

## 💡 Ejemplos Prácticos

### Ejemplo 1: Integración en EntregaScreen

```kotlin
@Composable
fun PantallaEntregaConFoto(
    pedidoId: String,
    viewModel: EntregaViewModel = hiltViewModel(),
    cameraViewModel: CameraViewModel = hiltViewModel(),
    onSuccess: () -> Unit
) {
    val fotoCapturada = viewModel.fotoCapturada.collectAsState()
    val registrarState = viewModel.registrarState.collectAsState()
    var showCamera by remember { mutableStateOf(false) }

    if (showCamera) {
        CameraScreenWithPermissions(
            onPhotoTaken = { file ->
                viewModel.setFotoCapturada(file)
                showCamera = false
            },
            onBackClick = { showCamera = false }
        )
    } else {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            
            // Vista previa de foto
            if (fotoCapturada.value != null) {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📷 ${fotoCapturada.value?.name}")
                    }
                }
                
                Button(onClick = { showCamera = true }) {
                    Text("Recapturar Foto")
                }
            } else {
                Button(onClick = { showCamera = true },
                       modifier = Modifier.fillMaxWidth()) {
                    Text("Tomar Foto")
                }
            }

            // Formulario de entrega
            OutlinedTextField(
                value = viewModel.recibidoPor.collectAsState().value,
                onValueChange = { viewModel.setRecibidoPor(it) },
                label = { Text("Recibido por") }
            )

            // Botón submit
            Button(
                onClick = {
                    viewModel.registrarEntrega(
                        pedidoId = pedidoId,
                        cantidadTotal = 5
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = registrarState.value !is RegistrarEntregaUiState.Loading
            ) {
                when (registrarState.value) {
                    is RegistrarEntregaUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    else -> Text("Marcar como Entregado")
                }
            }
        }
    }
}
```

### Ejemplo 2: Capturar y Subir Inmediatamente

```kotlin
@Composable
fun RapidaCaptura(
    pedidoId: String,
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    val state = cameraViewModel.fotoState.collectAsState()
    
    LaunchedEffect(state.value) {
        when (state.value) {
            is FotoUiState.PhotoCaptured -> {
                // Subir automáticamente después de capturar
                cameraViewModel.uploadPhoto(
                    tipo = "entrega",
                    referencia = pedidoId,
                    conReintentos = true
                )
            }
            is FotoUiState.PhotoUploaded -> {
                // Hacer algo cuando se complete upload
                // (auto-close, navegar, etc)
            }
            is FotoUiState.Error -> {
                // Mostrar error
            }
            else -> {}
        }
    }

    CameraScreenWithPermissions(
        onPhotoTaken = { file ->
            // Foto ya se está subiendo automáticamente
        },
        onBackClick = { /* nav back */ }
    )
}
```

### Ejemplo 3: Con Compresión Manual

```kotlin
@Composable
fun EntregaConCompresiónManual(
    pedidoId: String,
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    val state = cameraViewModel.fotoState.collectAsState()
    val fotoSize = remember { mutableStateOf(0.0) }

    Column {
        when (val s = state.value) {
            is FotoUiState.PhotoCaptured -> {
                val sizeMB = cameraViewModel.getPhotoSizeMB()
                fotoSize.value = sizeMB

                Text("Tamaño: ${String.format("%.2f", sizeMB)} MB")

                // Ofrecer opciones de compresión
                Row {
                    Button(onClick = {
                        cameraViewModel.compressPhoto(quality = 95)
                    }) {
                        Text("Alta Calidad")
                    }
                    Button(onClick = {
                        cameraViewModel.compressPhoto(quality = 85)
                    }) {
                        Text("Normal")
                    }
                    Button(onClick = {
                        cameraViewModel.compressPhoto(quality = 70)
                    }) {
                        Text("Baja (Rápido)")
                    }
                }

                // Subir
                Button(onClick = {
                    cameraViewModel.uploadPhoto(referencia = pedidoId)
                }) {
                    Text("Subir Foto")
                }
            }
            is FotoUiState.CompressingPhoto -> {
                CircularProgressIndicator()
                Text("Comprimiendo...")
            }
            is FotoUiState.UploadingPhoto -> {
                LinearProgressIndicator()
                Text("Subiendo...")
            }
            else -> {
                Button(onClick = { /* capture */ }) {
                    Text("Tomar Foto")
                }
            }
        }
    }
}
```

---

## 📊 Diagrama de Dependencias

```
EntregaScreen
    │
    ├─► EntregaViewModel
    │   ├─► PedidoRepository
    │   └─► FotoCapturada (StateFlow<File?>)
    │
    └─► CameraScreen
        └─► CameraManager
            ├─► startCamera()
            ├─► capturePhoto()
            ├─► compressImage()
            └─► deletePhotoFile()

Cuando se sube:
    EntregaViewModel
        └─► PedidoRepository.registrarEntrega()
            └─► ConductorApiService.registrarEntrega()
                └─► Retrofit + OkHttp (Multipart)
```

---

## 🔍 Debugging

### Logs Importantes

```kotlin
// En CameraManager
Timber.d("Cámara inicializada")
Timber.d("Foto capturada: ${photoFile.absolutePath}")
Timber.d("Foto comprimida: ${compressedFile.length()} bytes")

// En FotoRepository
Timber.d("Subiendo foto: ${file.name} (${file.length()} bytes)")
Timber.e(exception, "Error subiendo foto")

// En ViewModel
Timber.d("Estado: ${fotoState.value}")
Timber.d("Upload iniciado para ${fotoCaptured.value?.name}")
```

### Verificar Permisos

```bash
# En Android Studio
adb shell pm list permissions | grep camera
adb shell pm list permissions | grep storage

# Verificar si app tiene permisos
adb shell pm list permissions -g | grep com.barraca.conductor
```

---

## ✅ Checklist de Integración

- [ ] CameraX dependencies en `build.gradle.kts`
- [ ] Permisos en `AndroidManifest.xml`
- [ ] `CameraManager.kt` creado
- [ ] `FotoRepository.kt` creado
- [ ] `CameraViewModel.kt` creado
- [ ] `CameraScreen.kt` implementado
- [ ] `PermissionHelper.kt` para permisos
- [ ] `EntregaViewModel` actualizado con foto state
- [ ] `ConductorApiService` con multipart endpoint
- [ ] Manejo de errores completo
- [ ] Testing de permisos
- [ ] Testing de upload

---

**Última actualización**: Marzo 2026
**Versión**: 1.0.0
