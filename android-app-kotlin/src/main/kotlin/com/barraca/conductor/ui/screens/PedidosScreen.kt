package com.barraca.conductor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RefreshCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.barraca.conductor.data.model.Pedido
import com.barraca.conductor.ui.composables.PedidoCard
import com.barraca.conductor.viewmodel.PedidosListUiState
import com.barraca.conductor.viewmodel.PedidosListViewModel

/**
 * Pantalla principal: Lista de pedidos asignados
 */
@Composable
fun PedidosScreen(
    viewModel: PedidosListViewModel,
    onPedidoClick: (Pedido) -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsState()
    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val selectedEstado = viewModel.selectedEstado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Pedidos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros por estado
            FiltrosEstado(
                selectedEstado = selectedEstado.value,
                onFiltroClick = { viewModel.filtrarPorEstado(it) }
            )

            // Contenido principal
            when (val state = uiState.value) {
                is PedidosListUiState.Loading -> {
                    CargandoContent()
                }

                is PedidosListUiState.Success -> {
                    if (state.pedidos.isEmpty()) {
                        PedidosVaciosContent()
                    } else {
                        ListaPedidosContent(
                            pedidos = state.pedidos,
                            isRefreshing = isRefreshing.value,
                            onRefresh = { viewModel.reintentar() },
                            onPedidoClick = onPedidoClick
                        )
                    }
                }

                is PedidosListUiState.Error -> {
                    ErrorContent(
                        mensaje = state.message,
                        onReintentar = { viewModel.reintentar() }
                    )
                }
            }
        }
    }
}

/**
 * Componente: Filtros por estado
 */
@Composable
private fun FiltrosEstado(
    selectedEstado: String?,
    onFiltroClick: (String) -> Unit
) {
    val estados = listOf("pendiente", "en_ruta", "parcial", "entregado")

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Text(
                text = "Filtrar por estado:",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                estados.forEach { estado ->
                    FilterChip(
                        selected = selectedEstado == estado,
                        onClick = { onFiltroClick(estado) },
                        label = {
                            Text(
                                text = estado.uppercase(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Componente: Lista de pedidos con pull-to-refresh
 */
@Composable
private fun ListaPedidosContent(
    pedidos: List<Pedido>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPedidoClick: (Pedido) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        items(pedidos) { pedido ->
            PedidoCard(
                pedido = pedido,
                onClick = onPedidoClick
            )
        }
    }
}

/**
 * Componente: Estado de carga
 */
@Composable
private fun CargandoContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando pedidos...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Componente: Sin pedidos
 */
@Composable
private fun PedidosVaciosContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📦",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay pedidos disponibles",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Cuando tengas pedidos asignados aparecerán aquí",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Componente: Error
 */
@Composable
private fun ErrorContent(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onReintentar
            ) {
                Icon(
                    imageVector = Icons.Default.RefreshCircle,
                    contentDescription = "Reintentar",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}
