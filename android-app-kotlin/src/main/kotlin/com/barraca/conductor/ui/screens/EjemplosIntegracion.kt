package com.barraca.conductor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.barraca.conductor.viewmodel.CameraViewModel
import com.barraca.conductor.viewmodel.FotoUiState

/**
 * Ejemplo Práctico #1: Pantalla que integra cámara y upload
 * Demuestra el flujo completo de capturar y subir foto
 */
@Composable
fun EjemploPantallaCompleta(
    pedidoId: String,
    viewModel: CameraViewModel = hiltViewModel(),
    onFotoSubida: () -> Unit = {}
) {
    val fotoState = viewModel.fotoState.collectAsState()
    var showCamera by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Foto de Entrega - $pedidoId") })
        }
    ) { padding ->
        if (showCamera) {
            // Mostrar cámara con permisos automáticos
            CameraScreenWithPermissions(
                onPhotoTaken = { file ->
                    // La foto se capturó exitosamente
                    viewModel.compressPhoto(quality = 85)
                    showCamera = false
                },
                onBackClick = { showCamera = false },
                onError = { errorMsg ->
                    // Manejar error de cámara
                }
            )
        } else {
            // Pantalla principal
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    when (val state = fotoState.value) {
                        // 1. Sin foto aún
                        FotoUiState.Idle -> {
                            Button(
                                onClick = { showCamera = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Tomar Foto")
                            }
                        }

                        // 2. Capturando foto
                        FotoUiState.CapturingPhoto -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(Modifier.height(8.dp))
                                    Text("Capturando foto...")
                                }
                            }
                        }

                        // 3. Comprimiendo foto
                        FotoUiState.CompressingPhoto -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(Modifier.height(8.dp))
                                    Text("Comprimiendo imagen...")
                                }
                            }
                        }

                        // 4. Foto capturada - listo para subir
                        is FotoUiState.PhotoCaptured -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            "📷",
                                            style = MaterialTheme.typography.displayLarge
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "Foto capturada",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "${state.file.name} (${
                                                String.format(
                                                    "%.2f",
                                                    viewModel.getPhotoSizeMB()
                                                )
                                            } MB)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }

                            // Botones de acción
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.uploadPhoto(
                                            tipo = "entrega",
                                            referencia = pedidoId,
                                            conReintentos = true
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Green
                                    )
                                ) {
                                    Text("Subir ✓")
                                }

                                OutlinedButton(
                                    onClick = { showCamera = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Recapturar")
                                }
                            }
                        }

                        // 5. Subiendo foto
                        FotoUiState.UploadingPhoto -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(Modifier.height(8.dp))
                                    Text("Subiendo foto...")
                                }
                            }
                        }

                        // 6. Foto subida exitosamente
                        is FotoUiState.PhotoUploaded -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE8F5E9)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "✅",
                                        style = MaterialTheme.typography.displayMedium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Foto subida exitosamente",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "URL: ${state.response["url"] ?: "N/A"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onFotoSubida()
                                    viewModel.reset()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continuar")
                            }
                        }

                        // 7. Error en algún paso
                        is FotoUiState.Error -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        "❌ Error",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        state.message,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Intentar de nuevo")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ejemplo Práctico #2: Componente reutilizable
 * Para usar en cualquier pantalla que necesite foto
 */
@Composable
fun FotoCapturadorSimple(
    onFotoCapturada: (String) -> Unit = {}, // URL de foto subida
    onError: (String) -> Unit = {},
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    var showCamera by remember { mutableStateOf(false) }
    val fotoState = cameraViewModel.fotoState.collectAsState()

    // Monitorear cambios de estado
    LaunchedEffect(fotoState.value) {
        when (val state = fotoState.value) {
            is FotoUiState.PhotoUploaded -> {
                val url = state.response["url"] ?: ""
                onFotoCapturada(url)
            }
            is FotoUiState.Error -> {
                onError(state.message)
            }
            else -> {}
        }
    }

    if (showCamera) {
        CameraScreenWithPermissions(
            onPhotoTaken = { file ->
                cameraViewModel.compressPhoto(quality = 85)
                showCamera = false
            },
            onBackClick = { showCamera = false },
            onError = onError
        )
    } else {
        when (fotoState.value) {
            is FotoUiState.PhotoCaptured -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        cameraViewModel.uploadPhoto()
                    }) {
                        Text("Subir Foto")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        cameraViewModel.deletePhoto()
                        showCamera = true
                    }) {
                        Text("Cambiar")
                    }
                }
            }

            is FotoUiState.UploadingPhoto -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                Button(
                    onClick = { showCamera = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tomar Foto")
                }
            }
        }
    }
}

/**
 * Ejemplo Práctico #3: Con manejo de múltiples fotos
 */
@Composable
fun MultipleFotoCapturador(
    onFotosCompletas: (List<String>) -> Unit = {},
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    var showCamera by remember { mutableStateOf(false) }
    val fotosSubidas = remember { mutableStateOf<List<String>>(emptyList()) }
    val fotoState = cameraViewModel.fotoState.collectAsState()

    Column {
        Text(
            "Fotos capturadas: ${fotosSubidas.value.size}/3",
            style = MaterialTheme.typography.labelMedium
        )

        // Mostrar fotos capturadas
        fotosSubidas.value.forEach { url ->
            Text("✓ $url", style = MaterialTheme.typography.bodySmall)
        }

        // Si hay menos de 3 fotos, mostrar botón de capturar
        if (fotosSubidas.value.size < 3) {
            Button(onClick = { showCamera = true }) {
                Text("Foto ${fotosSubidas.value.size + 1}")
            }
        } else {
            Button(
                onClick = { onFotosCompletas(fotosSubidas.value) }
            ) {
                Text("Continuar")
            }
        }
    }

    if (showCamera) {
        CameraScreenWithPermissions(
            onPhotoTaken = { file ->
                cameraViewModel.uploadPhoto()
                showCamera = false
            },
            onBackClick = { showCamera = false }
        )
    }

    // Cuando se suba la foto
    LaunchedEffect(fotoState.value) {
        if (fotoState.value is FotoUiState.PhotoUploaded) {
            val url = (fotoState.value as FotoUiState.PhotoUploaded).response["url"] ?: ""
            fotosSubidas.value = fotosSubidas.value + url
            cameraViewModel.reset()
        }
    }
}
