package com.barraca.conductor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barraca.conductor.ui.screens.EntregaScreen
import com.barraca.conductor.ui.screens.PedidoDetailScreen
import com.barraca.conductor.ui.screens.PedidosScreen
import com.barraca.conductor.ui.theme.AppTypography
import com.barraca.conductor.utils.Constants
import com.barraca.conductor.viewmodel.EntregaViewModel
import com.barraca.conductor.viewmodel.PedidoDetailViewModel
import com.barraca.conductor.viewmodel.PedidosListViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.lightColorScheme

/**
 * Color scheme simple para Material Design 3
 */
private val LightColorScheme = lightColorScheme()

/**
 * Activity principal de la aplicación
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                ConductorApp()
            }
        }
    }
}

/**
 * Composable principal de la app con navegación
 */
@Composable
fun ConductorApp() {
    val navController = rememberNavController()

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography
    ) {
        ConductorNavGraph(navController = navController)
    }
}

/**
 * NavGraph con todas las pantallas
 */
@Composable
fun ConductorNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Constants.ROUTE_PEDIDOS
    ) {
        // Pantalla de lista de pedidos
        composable(Constants.ROUTE_PEDIDOS) {
            val viewModel = hiltViewModel<PedidosListViewModel>()

            PedidosScreen(
                viewModel = viewModel,
                onPedidoClick = { pedido ->
                    navController.navigate("pedido_detail/${pedido.id}")
                }
            )
        }

        // Pantalla de detalle de pedido
        composable(
            route = "pedido_detail/{pedidoId}",
            arguments = listOf(
                navArgument("pedidoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: return@composable
            val viewModel = hiltViewModel<PedidoDetailViewModel>()

            PedidoDetailScreen(
                pedidoId = pedidoId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onGoToEntrega = { pedidoId ->
                    navController.navigate("entrega/$pedidoId")
                }
            )
        }

        // Pantalla de entrega
        composable(
            route = "entrega/{pedidoId}",
            arguments = listOf(
                navArgument("pedidoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: return@composable
            
            // TODO: Obtener datos del pedido (en una app real, usar un state holder compartido)
            val pedidoDummy = com.barraca.conductor.data.model.Pedido(
                id = pedidoId,
                numero = "12345",
                clienteId = "client1",
                cliente = "Cliente Ejemplo",
                email = "client@example.com",
                telefono = "123456789",
                direccion = "Calle Principal 123",
                barrio = "Centro",
                latitud = 0.0,
                longitud = 0.0,
                montoTotal = 100.0,
                estado = "en_ruta",
                fechaCreacion = "2024-01-01",
                fechaEntrega = null,
                items = emptyList(),
                observaciones = null,
                fotoEntrega = null
            )
            
            val viewModel = hiltViewModel<EntregaViewModel>()

            EntregaScreen(
                pedido = pedidoDummy,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack(Constants.ROUTE_PEDIDOS, inclusive = false)
                }
            )
        }
    }
}
