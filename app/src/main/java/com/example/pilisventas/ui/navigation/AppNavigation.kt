package com.example.pilisventas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pilisventas.ui.screens.dashboard.DashboardScreen
import com.example.pilisventas.ui.screens.dashboard.DashboardViewModel
import com.example.pilisventas.ui.screens.editar.EditarVentaScreen
import com.example.pilisventas.ui.screens.editar.EditarVentaViewModel
import com.example.pilisventas.ui.screens.editar.EditarVentaViewModelFactory
import com.example.pilisventas.ui.screens.historial.HistorialScreen
import com.example.pilisventas.ui.screens.historial.HistorialViewModel
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
    const val HISTORIAL = "historial"
    const val EDITAR_VENTA = "editar_venta"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { rol ->
                    val destino = if (rol.equals("JEFA", ignoreCase = true)) Routes.DASHBOARD else Routes.HOME_AYUDANTE
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
                onHistorial = { navController.navigate(Routes.HISTORIAL) },
                onEditarVenta = { venta -> navController.navigate("${Routes.EDITAR_VENTA}/${venta.id}") },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HISTORIAL) {
            val viewModel: HistorialViewModel = viewModel()
            HistorialScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.EDITAR_VENTA}/{ventaId}",
            arguments = listOf(navArgument("ventaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ventaId = backStackEntry.arguments?.getString("ventaId") ?: return@composable
            val viewModel: EditarVentaViewModel = viewModel(factory = EditarVentaViewModelFactory(ventaId))
            EditarVentaScreen(
                viewModel = viewModel,
                onGuardado = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
