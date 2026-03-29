package com.barraca.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.barraca.app.ui.screens.LoginScreen
import com.barraca.app.ui.screens.PedidosScreen
import com.barraca.app.viewmodel.AuthViewModel
import com.barraca.app.viewmodel.PedidosViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            BarracaMoralesTheme {
                val authViewModel = remember { AuthViewModel(this@MainActivity) }
                val pedidosViewModel = remember { PedidosViewModel(this@MainActivity) }
                
                val user by authViewModel.user.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    if (user == null) {
                        LoginScreen(authViewModel)
                    } else {
                        PedidosScreen(user!!, authViewModel, pedidosViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BarracaMoralesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = darkColors(
            primary = androidx.compose.ui.graphics.Color(0xFF1F7EA8),
            primaryVariant = androidx.compose.ui.graphics.Color(0xFF1565A8),
            secondary = androidx.compose.ui.graphics.Color(0xFF26C6DA)
        ),
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
