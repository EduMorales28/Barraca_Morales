package com.barraca.conductor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barraca.conductor.data.model.Pedido
import com.barraca.conductor.ui.theme.EstadoEntregadoColor
import com.barraca.conductor.viewmodel.EntregaViewModel
import com.barraca.conductor.viewmodel.RegistrarEntregaUiState
import java.io.File

/**
 * Pantalla de entrega: tomar foto, observaciones, marcar como entregado
 */
@Composable
fun EntregaScreen(
    pedido: Pedido,
    viewModel: EntregaViewModel,
    onBackClick: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val registrarState = viewModel.registrarState.collectAsState()
    val fotoCapturada = viewModel.fotoCapturada.collectAsState()
    val observaciones = viewModel.observaciones.collectAsState()
    val recibidoPor = viewModel.recibidoPor.collectAsState()
    val dniRecibidor = viewModel.dniRecibidor.collectAsState()
    val cantidadLevantada = viewModel.cantidadLevantada.collectAsState()
    val latitud = viewModel.latitud.collectAsState()
    val longitud = viewModel.longitud.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Entrega") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Información del pedido
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Pedido #${pedido.numero}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = pedido.cliente,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Items: ${pedido.items.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // ==================== FOTO ====================

            item {
                Text(
                    text = "Foto de Entrega",
                    style = MaterialTheme.typography.headlineSmall
                )

                if (fotoCapturada.value != null) {
                    // Foto capturada
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📷 Foto capturada", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = fotoCapturada.value?.name ?: "Sin nombre",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { /* TODO: Abrir cámara para recapturar */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recapturar Foto")
                    }
                } else {
                    // Sin foto
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin foto capturada",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Button(
                        onClick = { /* TODO: Abrir cámara */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tomar Foto")
                    }
                }
            }

            // ==================== INFORMACIÓN DE ENTREGA ====================

            item {
                Text(
                    text = "Información de Entrega",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            // Recibido por
            item {
                OutlinedTextField(
                    value = recibidoPor.value,
                    onValueChange = { viewModel.setRecibidoPor(it) },
                    label = { Text("Nombre de quien recibe *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = registrarState.value !is RegistrarEntregaUiState.Loading
                )
            }

            // DNI
            item {
                OutlinedTextField(
                    value = dniRecibidor.value,
                    onValueChange = { viewModel.setDniRecibidor(it) },
                    label = { Text("DNI / Cédula *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = registrarState.value !is RegistrarEntregaUiState.Loading
                )
            }

            // Cantidad levantada
            item {
                OutlinedTextField(
                    value = cantidadLevantada.value.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { cantidad ->
                            viewModel.setCantidadLevantada(cantidad)
                        }
                    },
                    label = { Text("Cantidad de Items *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = registrarState.value !is RegistrarEntregaUiState.Loading
                )
            }

            // Observaciones
            item {
                OutlinedTextField(
                    value = observaciones.value,
                    onValueChange = { viewModel.setObservaciones(it) },
                    label = { Text("Observaciones (opcional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    enabled = registrarState.value !is RegistrarEntregaUiState.Loading
                )
            }

            // Ubicación GPS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "GPS",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ubicación GPS",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (latitud.value != 0.0 && longitud.value != 0.0) {
                            Text(
                                text = "Latitud: ${String.format("%.6f", latitud.value)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Longitud: ${String.format("%.6f", longitud.value)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = "Ubicación no disponible",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { 
                                // TODO: Obtener ubicación GPS actual
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Obtener Ubicación Actual")
                        }
                    }
                }
            }

            // ==================== BOTONES DE ACCIÓN ====================

            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón principal: Marcar como Entregado
                    Button(
                        onClick = { 
                            viewModel.registrarEntrega(
                                pedidoId = pedido.id,
                                cantidadTotal = pedido.items.size
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = registrarState.value !is RegistrarEntregaUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EstadoEntregadoColor
                        )
                    ) {
                        when (registrarState.value) {
                            is RegistrarEntregaUiState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            }

                            else -> {
                                Text("Marcar como Entregado")
                            }
                        }
                    }

                    // Botón secundario: Limpiar
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.limpiarFormulario() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = registrarState.value !is RegistrarEntregaUiState.Loading
                    ) {
                        Text("Limpiar Formulario")
                    }
                }
            }

            // Estados de respuesta
            item {
                when (val state = registrarState.value) {
                    is RegistrarEntregaUiState.Success -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xE0F7E3)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "✓ Entrega registrada exitosamente",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EstadoEntregadoColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onSuccess,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Continuar")
                                }
                            }
                        }
                    }

                    is RegistrarEntregaUiState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "❌ Error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
