package com.barraca.conductor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.barraca.conductor.data.model.Pedido
import com.barraca.conductor.ui.composables.ItemCard
import com.barraca.conductor.ui.theme.SuccessColor
import com.barraca.conductor.viewmodel.PedidoDetailViewModel
import com.barraca.conductor.viewmodel.PedidoDetailUiState
import com.barraca.conductor.viewmodel.ActualizarPedidoUiState

/**
 * Pantalla de detalle de un pedido
 * Muestra: cliente, dirección, artículos, mapa, acciones
 */
@Composable
fun PedidoDetailScreen(
    pedidoId: String,
    viewModel: PedidoDetailViewModel,
    onBackClick: () -> Unit = {},
    onGoToEntrega: (String) -> Unit = {},
    onGoToMapa: (String) -> Unit = {}
) {
    LaunchedEffect(pedidoId) {
        viewModel.cargarPedido(pedidoId)
    }

    val pedidoState = viewModel.pedidoState.collectAsState()
    val actualizarState = viewModel.actualizarState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Pedido") },
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
        when (val state = pedidoState.value) {
            is PedidoDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is PedidoDetailUiState.Success -> {
                PedidoDetailContent(
                    pedido = state.pedido,
                    actualizarState = actualizarState.value,
                    onMarcarEnRuta = { viewModel.marcarEnRuta(pedidoId) },
                    onMarcarParcial = { viewModel.marcarParcial(pedidoId) },
                    onGoToEntrega = { onGoToEntrega(pedidoId) },
                    onGoToMapa = { onGoToMapa(pedidoId) },
                    onLimpiarEstado = { viewModel.limpiarEstadoActualizacion() },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is PedidoDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Contenido del detalle del pedido
 */
@Composable
private fun PedidoDetailContent(
    pedido: Pedido,
    actualizarState: ActualizarPedidoUiState,
    onMarcarEnRuta: () -> Unit,
    onMarcarParcial: () -> Unit,
    onGoToEntrega: () -> Unit,
    onGoToMapa: (() -> Unit)? = null,
    onLimpiarEstado: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Información principal
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Pedido #${pedido.numero}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pedido.cliente,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = pedido.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = pedido.telefono,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Ubicación - Información
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
                            contentDescription = "Ubicación",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dirección de entrega",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pedido.direccion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Barrio: ${pedido.barrio}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Coordenadas: ${String.format("%.4f", pedido.latitud)}, ${String.format("%.4f", pedido.longitud)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Ubicación - Mini Mapa
            Text(
                text = "Mapa de ubicación",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
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
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Artículos
            Text(
                text = "Artículos (${pedido.items.size})",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(pedido.items) { item ->
            ItemCard(item = item)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Total
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total:",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "$${String.format("%.2f", pedido.montoTotal)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SuccessColor
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onGoToEntrega,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessColor
                    )
                ) {
                    Text("Proceder a Entrega")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onMarcarEnRuta,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pedido.estado != "en_ruta" && actualizarState !is ActualizarPedidoUiState.Loading
                ) {
                    Text("Marcar en Ruta")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onMarcarParcial,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pedido.estado != "entregado" && actualizarState !is ActualizarPedidoUiState.Loading
                ) {
                    Text("Marcar Parcial")
                }

                // Mostrar estado de actualización
                when (actualizarState) {
                    is ActualizarPedidoUiState.Loading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    is ActualizarPedidoUiState.Success -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Actualizado",
                            color = SuccessColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    is ActualizarPedidoUiState.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "❌ ${actualizarState.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    else -> {}
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
