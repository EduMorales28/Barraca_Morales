package com.barraca.conductor.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.barraca.conductor.data.model.Pedido
import com.barraca.conductor.data.model.ItemPedido
import com.barraca.conductor.ui.theme.*

/**
 * Card para mostrar un pedido en la lista
 */
@Composable
fun PedidoCard(
    pedido: Pedido,
    onClick: (Pedido) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick(pedido) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado: número y estado
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${pedido.numero}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                EstadoBadge(pedido.estado)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cliente
            Text(
                text = "Cliente: ${pedido.cliente}",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Dirección
            Text(
                text = "📍 ${pedido.direccion}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Cantidad de items
            Text(
                text = "Items: ${pedido.items.size} | Total: \$${String.format("%.2f", pedido.montoTotal)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Teléfono
            Text(
                text = "☎️ ${pedido.telefono}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666)
            )
        }
    }
}

/**
 * Badge para mostrar el estado del pedido
 */
@Composable
fun EstadoBadge(estado: String) {
    val (color, texto) = when (estado) {
        "pendiente" -> EstadoPendienteColor to "Pendiente"
        "en_ruta" -> EstadoEnRutaColor to "En Ruta"
        "parcial" -> EstadoParcialColor to "Parcial"
        "entregado" -> EstadoEntregadoColor to "Entregado"
        else -> Color(0xFF999999) to estado
    }

    Box(
        modifier = Modifier
            .background(color = color, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Card para mostrar un item dentro de un pedido
 */
@Composable
fun ItemCard(
    item: ItemPedido,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (!item.descripcion.isNullOrEmpty()) {
                    Text(
                        text = item.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Cant: ${item.cantidad}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "\$${String.format("%.2f", item.precioUnitario * item.cantidad)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SuccessColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
