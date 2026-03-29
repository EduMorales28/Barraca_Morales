package com.barraca.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.barraca.app.LoginResponse
import com.barraca.app.Pedido
import com.barraca.app.viewmodel.AuthViewModel
import com.barraca.app.viewmodel.PedidosViewModel
import com.barraca.app.viewmodel.PedidosUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PedidosScreen(
    user: LoginResponse,
    authViewModel: AuthViewModel,
    pedidosViewModel: PedidosViewModel
) {
    val context = LocalContext.current
    val pedidos by pedidosViewModel.pedidos.collectAsState()
    val selectedPedido by pedidosViewModel.selectedPedido.collectAsState()
    val uiState by pedidosViewModel.uiState.collectAsState()
    
    var showDetalles by remember { mutableStateOf(false) }
    var showEntrega by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var observaciones by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        pedidosViewModel.loadPedidos(user.id)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            // Foto tomada
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && selectedPedido != null) {
            val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            cameraLauncher.launch(photoUri)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            TopAppBar(
                title = { Text("Barraca Morales") },
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 4.dp,
                actions = {
                    Button(
                        onClick = {
                            authViewModel.logout()
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.secondary
                        )
                    ) {
                        Text("Logout")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Bienvenido, ${user.nombre}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.subtitle1
            )

            if (uiState is PedidosUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState is PedidosUiState.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (uiState as PedidosUiState.Error).message,
                        color = Color.Red
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(pedidos) { pedido ->
                        PedidoCard(
                            pedido = pedido,
                            onClick = {
                                pedidosViewModel.selectPedido(pedido.id)
                                showDetalles = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Modal: Detalles del Pedido
        if (showDetalles && selectedPedido != null) {
            PedidoDetallesModal(
                pedido = selectedPedido!!,
                onClose = { showDetalles = false },
                onEntrega = {
                    showEntrega = true
                }
            )
        }

        // Modal: Registrar Entrega
        if (showEntrega && selectedPedido != null) {
            EntregaModal(
                pedido = selectedPedido!!,
                observaciones = observaciones,
                onObservacionesChange = { observaciones = it },
                onFotoClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onEnviar = {
                    if (photoUri != null) {
                        val imagePath = photoUri?.path ?: ""
                        pedidosViewModel.crearEntrega(selectedPedido!!.id, observaciones, imagePath)
                        showEntrega = false
                        observaciones = ""
                    }
                },
                onClose = { showEntrega = false }
            )
        }
    }
}

@Composable
fun PedidoCard(pedido: Pedido, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(pedido.cliente, style = MaterialTheme.typography.subtitle1)
                EstadoBadge(pedido.estado)
            }
            Text(pedido.direccion, style = MaterialTheme.typography.body2)
            Text("${pedido.lat}, ${pedido.lng}", style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun EstadoBadge(estado: String) {
    val backgroundColor = when (estado) {
        "pendiente" -> Color(0xFFFFC107)
        "asignado" -> Color(0xFF2196F3)
        "entregado" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            estado,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PedidoDetallesModal(
    pedido: Pedido,
    onClose: () -> Unit,
    onEntrega: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text(pedido.cliente) },
        text = {
            Column {
                Text("Dirección: ${pedido.direccion}")
                Text("Estado: ${pedido.estado}")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Items:", fontWeight = FontWeight.Bold)
                pedido.items.forEach { item ->
                    Text("• ${item.nombre} (${item.cantidad})")
                }
            }
        },
        confirmButton = {
            if (pedido.estado != "entregado") {
                Button(onClick = { onEntrega() }) {
                    Text("Entregar")
                }
            }
        },
        dismissButton = {
            Button(onClick = { onClose() }) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun EntregaModal(
    pedido: Pedido,
    observaciones: String,
    onObservacionesChange: (String) -> Unit,
    onFotoClick: () -> Unit,
    onEnviar: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("Registrar Entrega") },
        text = {
            Column {
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = onObservacionesChange,
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onFotoClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tomar foto")
                }
            }
        },
        confirmButton = {
            Button(onClick = onEnviar) {
                Text("Enviar")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("Cancelar")
            }
        }
    )
}
