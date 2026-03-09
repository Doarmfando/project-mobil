package com.example.pilisventas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.pilisventas.ui.navigation.AppNavigation
import com.example.pilisventas.ui.theme.PilisVentasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PilisVentasTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
