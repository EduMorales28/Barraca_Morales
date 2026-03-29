/**
 * ======================================
 * COPY-PASTE READY EXAMPLES
 * ======================================
 * 
 * Ejemplos minimalistas listos para copiar en tu código
 * Requiere: Hilt + CameraViewModel inyectado
 */

// ============================================================
// EJEMPLO 1: USO BÁSICO EN 10 LÍNEAS
// ============================================================

@Composable
fun TomarFoto() {
    var foto by remember { mutableStateOf<String?>(null) }
    val viewModel: CameraViewModel = hiltViewModel()

    Column {
        if (foto != null) Text("✓ Foto: $foto")
        Button(onClick = { 
            // Abre cámara
        }) { Text("Tomar") }
    }
}

// ============================================================
// EJEMPLO 2: COMPLETO CON ESTADOS
// ============================================================

@Composable
fun TomarYSubirFoto() {
    val viewModel: CameraViewModel = hiltViewModel()
    val state = viewModel.fotoState.collectAsState()
    var abrirCamara by remember { mutableStateOf(false) }

    if (abrirCamara) {
        CameraScreenWithPermissions(
            onPhotoTaken = { file ->
                viewModel.uploadPhoto()
                abrirCamara = false
            },
            onBackClick = { abrirCamara = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (state.value) {
                FotoUiState.Idle -> {
                    Button(onClick = { abrirCamara = true }) {
                        Text("📷 Tomar Foto")
                    }
                }
                FotoUiState.CapturingPhoto -> {
                    CircularProgressIndicator()
                    Text("Capturando...")
                }
                is FotoUiState.PhotoCaptured -> {
                    Text("Foto lista")
                    Button(onClick = { viewModel.uploadPhoto() }) {
                        Text("Subir")
                    }
                }
                FotoUiState.UploadingPhoto -> {
                    CircularProgressIndicator()
                    Text("Subiendo...")
                }
                is FotoUiState.PhotoUploaded -> {
                    Text("✅ Completado")
                }
                is FotoUiState.Error -> {
                    Text("❌ ${state.value.message}", color = Color.Red)
                    Button(onClick = { viewModel.reset() }) {
                        Text("Reintentar")
                    }
                }
                else -> {}
            }
        }
    }
}

// ============================================================
// EJEMPLO 3: INTEGRACIÓN EN FORMULARIO
// ============================================================

@Composable
fun FormularioConFoto(
    onSubmit: (fotoUrl: String) -> Unit
) {
    val viewModel: CameraViewModel = hiltViewModel()
    val state = viewModel.fotoState.collectAsState()
    var nombre by remember { mutableStateOf("") }
    var showCamera by remember { mutableStateOf(false) }
    
    var fotoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.value) {
        if (state.value is FotoUiState.PhotoUploaded) {
            val response = (state.value as FotoUiState.PhotoUploaded).response
            fotoUrl = response["url"]
        }
    }

    if (showCamera) {
        CameraScreenWithPermissions(
            onPhotoTaken = { viewModel.uploadPhoto() },
            onBackClick = { showCamera = false }
        )
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(nombre, { nombre = it }, label = { Text("Nombre") })
            
            if (fotoUrl != null) {
                Text("Foto: ✓")
            } else {
                Button(onClick = { showCamera = true }) {
                    Text("Adjuntar Foto")
                }
            }
            
            Button(
                onClick = { onSubmit(fotoUrl ?: "") },
                enabled = nombre.isNotEmpty() && fotoUrl != null
            ) {
                Text("Enviar")
            }
        }
    }
}

// ============================================================
// EJEMPLO 4: SUBIR VARIAS FOTOS SECUENCIAL
// ============================================================

@Composable
fun MultiplosFotos() {
    val viewModel: CameraViewModel = hiltViewModel()
    val state = viewModel.fotoState.collectAsState()
    var fotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var showCamera by remember { mutableStateOf(false) }

    LaunchedEffect(state.value) {
        if (state.value is FotoUiState.PhotoUploaded) {
            val url = (state.value as FotoUiState.PhotoUploaded).response["url"] ?: ""
            fotos = fotos + url
            viewModel.reset()
        }
    }

    if (showCamera) {
        CameraScreenWithPermissions(
            onPhotoTaken = { viewModel.uploadPhoto() },
            onBackClick = { showCamera = false }
        )
    } else {
        Column {
            Text("${fotos.size}/3 fotos")
            if (fotos.size < 3) {
                Button(onClick = { showCamera = true }) {
                    Text("Foto ${fotos.size + 1}")
                }
            } else {
                Button(onClick = { /* procesar fotos */ }) {
                    Text("Continuar")
                }
            }
        }
    }
}

// ============================================================
// EJEMPLO 5: CON LOCALIZACIÓN (Optional)
// ============================================================

@Composable
fun FotoConUbicacion(
    onFotoConUbicacion: (fotoUrl: String, lat: Double, lng: Double) -> Unit
) {
    val cameraViewModel: CameraViewModel = hiltViewModel()
    val locationViewModel: LocationViewModel = hiltViewModel() // Si tienes
    val cameraState = cameraViewModel.fotoState.collectAsState()
    
    var showCamera by remember { mutableStateOf(false) }
    var fotoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cameraState.value) {
        if (cameraState.value is FotoUiState.PhotoUploaded) {
            val response = (cameraState.value as FotoUiState.PhotoUploaded).response
            fotoUrl = response["url"]
            
            // Obtener ubicación actual
            locationViewModel?.getUbicacion()?.let { (lat, lng) ->
                onFotoConUbicacion(fotoUrl ?: "", lat, lng)
            }
        }
    }

    if (showCamera) {
        CameraScreenWithPermissions(
            onPhotoTaken = { cameraViewModel.uploadPhoto() },
            onBackClick = { showCamera = false }
        )
    } else {
        Column {
            if (fotoUrl != null) {
                Text("✓ Foto con ubicación")
            } else {
                Button(onClick = { showCamera = true }) {
                    Text("Tomar Foto + GPS")
                }
            }
        }
    }
}

// ============================================================
// EJEMPLO 6: REUSABLE COMPONENT
// ============================================================

@Composable
fun BotonFoto(
    texto: String = "Tomar Foto",
    onFotoSubida: (url: String) -> Unit,
    onError: (error: String) -> Unit = {}
) {
    val viewModel: CameraViewModel = hiltViewModel()
    val state = viewModel.fotoState.collectAsState()
    var showCamera by remember { mutableStateOf(false) }

    LaunchedEffect(state.value) {
        if (state.value is FotoUiState.PhotoUploaded) {
            val url = (state.value as FotoUiState.PhotoUploaded).response["url"] ?: ""
            onFotoSubida(url)
            viewModel.reset()
        } else if (state.value is FotoUiState.Error) {
            onError((state.value as FotoUiState.Error).message)
        }
    }

    if (showCamera) {
        CameraScreenWithPermissions(
            onPhotoTaken = { viewModel.uploadPhoto() },
            onBackClick = { showCamera = false }
        )
    } else {
        when (state.value) {
            is FotoUiState.UploadingPhoto -> {
                CircularProgressIndicator()
            }
            else -> {
                Button(onClick = { showCamera = true }) {
                    Text(texto)
                }
            }
        }
    }
}

// ============================================================
// EJEMPLO 7: INTEGRACIÓN EN EntregaScreen
// ============================================================

@Composable
fun PantallaEntregaActualizada(
    pedidoId: String,
    entregaViewModel: EntregaViewModel = hiltViewModel(),
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    val fotoState = cameraViewModel.fotoState.collectAsState()
    val entregaState = entregaViewModel.registrarState.collectAsState()
    var showCamera by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Formulario
        OutlinedTextField(
            value = entregaViewModel.recibidoPor.collectAsState().value,
            onValueChange = { entregaViewModel.setRecibidoPor(it) },
            label = { Text("Recibido por") }
        )

        OutlinedTextField(
            value = entregaViewModel.dniRecibidor.collectAsState().value,
            onValueChange = { entregaViewModel.setDniRecibidor(it) },
            label = { Text("DNI") }
        )

        // BOTÓN FOTO
        if (showCamera) {
            CameraScreenWithPermissions(
                onPhotoTaken = { file ->
                    entregaViewModel.setFotoCapturada(file)
                    showCamera = false
                },
                onBackClick = { showCamera = false }
            )
        } else {
            Button(
                onClick = { showCamera = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📷 Tomar Foto")
            }
        }

        // BOTÓN SUBMIT
        Button(
            onClick = {
                entregaViewModel.registrarEntrega(
                    pedidoId = pedidoId,
                    cantidadTotal = 5
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = entregaState.value !is RegistrarEntregaUiState.Loading
        ) {
            Text("Marcar Entregado")
        }
    }
}

// ============================================================
// Quick Reference
// ============================================================

/*
ESTADOS POSIBLES:
- FotoUiState.Idle                      // inicial
- FotoUiState.CapturingPhoto            // tomando foto
- FotoUiState.CompressingPhoto          // comprimiendo
- FotoUiState.UploadingPhoto            // subiendo
- FotoUiState.PhotoCaptured(file)      // lista para subir
- FotoUiState.PhotoUploaded(response)   // completado ✓
- FotoUiState.Error(message)            // error ❌

MÉTODOS VIEWMODEL:
viewModel.capturePhoto()                        // tomar
viewModel.compressPhoto(quality = 85)           // comprimir
viewModel.uploadPhoto(tipo, ref, conReintentos) // subir
viewModel.deletePhoto()                         // eliminar
viewModel.reset()                               // resetear
viewModel.getPhotoSizeMB()                      // tamaño

COMPOSABLES:
CameraScreen -> sin permisos
CameraScreenWithPermissions -> con solicitud automática
EjemploPantallaCompleta -> todos los estados
FotoCapturadorSimple -> componente reutilizable
MultipleFotoCapturador -> varias fotos

INTEGRACIÓN MÍNIMA:
1. var showCamera by remember { mutableStateOf(false) }
2. if (showCamera) CameraScreenWithPermissions(...)
3. Button(onClick = { showCamera = true })
*/
