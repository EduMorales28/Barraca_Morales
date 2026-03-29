package com.barraca.conductor.ui.screens

import android.Manifest
import android.content.Context
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.barraca.conductor.services.CameraManager
import com.barraca.conductor.utils.PermissionHelper
import java.io.File

/**
 * Pantalla para capturar foto con cámara
 */
@Composable
fun CameraScreen(
    onPhotoTaken: (File) -> Unit,
    onBackClick: () -> Unit,
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as? LifecycleOwner
        ?: return

    val permissionHelper = remember { PermissionHelper(context) }
    val cameraManager = remember { CameraManager(context) }

    var hasPermission by remember {
        mutableStateOf(
            permissionHelper.hasPermission(PermissionHelper.CAMERA)
        )
    }
    var isTakingPhoto by remember { mutableStateOf(false) }
    var cameraReady by remember { mutableStateOf(false) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            onError("Se requiere permiso de cámara")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tomar Foto") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vista previa de la cámara
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        previewView = this

                        // Inicializar cámara cuando el preview esté listo
                        post {
                            cameraManager.startCamera(
                                lifecycleOwner = lifecycleOwner,
                                previewSurfaceProvider = surfaceProvider,
                                onCameraReady = {
                                    cameraReady = true
                                },
                                onError = { exc ->
                                    onError("Error al inicializar cámara: ${exc.message}")
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            )

            // Botones de acción
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isTakingPhoto) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(60.dp)
                    )
                } else {
                    Button(
                        onClick = {
                            if (cameraReady && !isTakingPhoto) {
                                isTakingPhoto = true

                                // Capturar foto en corrutina
                                // Nota: en una app real, esto iría en un ViewModel
                                Thread {
                                    val result = androidx.compose.ui.text.android.runBlocking {
                                        cameraManager.capturePhoto()
                                    }

                                    result.onSuccess { file ->
                                        // Comprimir imagen
                                        val compressResult = cameraManager.compressImage(file)
                                        compressResult.onSuccess { compressedFile ->
                                            onPhotoTaken(compressedFile)
                                        }.onFailure { exc ->
                                            onError("Error al comprimir: ${exc.message}")
                                            isTakingPhoto = false
                                        }
                                    }.onFailure { exc ->
                                        onError("Error capturando foto: ${exc.message}")
                                        isTakingPhoto = false
                                    }
                                }.start()
                            }
                        },
                        modifier = Modifier.size(80.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = cameraReady && !isTakingPhoto
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Tomar foto",
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // Info
            Text(
                text = if (cameraReady) "Presiona el botón para capturar" else "Inicializando cámara...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Black)
            )
        }
    }
}

/**
 * **VERSIÓN MEJORADA** - Pantalla de cámara con manejo de permisos y corrutinas
 * Integrada con ViewModel para mejor manejo del estado
 */
@Composable
fun CameraScreenWithPermissions(
    onPhotoTaken: (File) -> Unit,
    onBackClick: () -> Unit,
    cameraManager: CameraManager? = null,
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycle = LocalContext.current as? LifecycleOwner
        ?: return

    val permissionHelper = remember { PermissionHelper(context) }
    val camera = cameraManager ?: remember { CameraManager(context) }

    var permission by remember {
        mutableStateOf(
            permissionHelper.hasPermission(Manifest.permission.CAMERA)
        )
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permission = isGranted
        if (!isGranted) {
            onError("Permiso de cámara denegado")
        }
    }

    LaunchedEffect(Unit) {
        if (!permission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!permission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "❌ Permiso de Cámara Requerido",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Esta app necesita acceso a la cámara para capturar fotos de entrega",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Solicitar Permiso")
                }
            }
        }
    } else {
        CameraScreenContent(
            cameraManager = camera,
            lifecycleOwner = lifecycle,
            onPhotoTaken = onPhotoTaken,
            onBackClick = onBackClick,
            onError = onError
        )
    }
}

@Composable
private fun CameraScreenContent(
    cameraManager: CameraManager,
    lifecycleOwner: LifecycleOwner,
    onPhotoTaken: (File) -> Unit,
    onBackClick: () -> Unit,
    onError: (String) -> Unit
) {
    var isCapturing by remember { mutableStateOf(false) }
    var cameraReady by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capturar Foto") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Preview
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        cameraManager.startCamera(
                            lifecycleOwner = lifecycleOwner,
                            previewSurfaceProvider = surfaceProvider,
                            onCameraReady = { cameraReady = true },
                            onError = { onError(it.message ?: "Error cámara") }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            )

            // Botón captura
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        isCapturing = true
                        // Capturar en background
                    },
                    modifier = Modifier.size(80.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    enabled = cameraReady && !isCapturing
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}
