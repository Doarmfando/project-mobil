package com.example.pilisventas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pilisventas.ui.screens.dashboard.DashboardScreen
import com.example.pilisventas.ui.screens.dashboard.DashboardViewModel
import com.example.pilisventas.ui.screens.home.HomeAyudanteScreen
import com.example.pilisventas.ui.screens.home.HomeAyudanteViewModel
import com.example.pilisventas.ui.screens.login.LoginScreen
import com.example.pilisventas.ui.screens.login.LoginViewModel
import com.example.pilisventas.ui.screens.registrar.RegistrarVentaScreen
import com.example.pilisventas.ui.screens.registrar.RegistrarVentaViewModel

object Routes {
    const val LOGIN = "login"
    const val HOME_AYUDANTE = "home_ayudante"
    const val REGISTRAR_VENTA = "registrar_venta"
    const val DASHBOARD = "dashboard"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { rol ->
                    val destino = if (rol == "JEFA") Routes.DASHBOARD else Routes.HOME_AYUDANTE
                    navController.navigate(destino) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME_AYUDANTE) {
            val viewModel: HomeAyudanteViewModel = viewModel()
            HomeAyudanteScreen(
                viewModel = viewModel,
                onNuevaVenta = { navController.navigate(Routes.REGISTRAR_VENTA) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME_AYUDANTE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTRAR_VENTA) {
            val viewModel: RegistrarVentaViewModel = viewModel()
            RegistrarVentaScreen(
                viewModel = viewModel,
                onVentaGuardada = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            val viewModel: DashboardViewModel = viewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNuevaVenta = { navController.navigate(Routes.REGISTRAR_VENTA) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
    }
}
