package com.example.pilisventas.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilisventas.data.model.MetodoPago
import com.example.pilisventas.data.model.Usuario
import com.example.pilisventas.data.model.Venta
import com.example.pilisventas.data.repository.AuthRepository
import com.example.pilisventas.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val usuario: Usuario? = null,
    val ventasHoy: List<Venta> = emptyList(),
    val totalHoy: Double = 0.0,
    val totalesPorMetodo: Map<MetodoPago, Double> = emptyMap(),
    val totalFiado: Double = 0.0,
    val cantidadFiados: Int = 0
)

class DashboardViewModel : ViewModel() {
    private val ventaRepository = VentaRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val usuario = authRepository.getCurrentUsuario()
            _uiState.update { it.copy(usuario = usuario) }

            ventaRepository.getVentasDeHoy().collect { ventas ->
                val totalesPorMetodo = MetodoPago.entries.associateWith { metodo ->
                    ventas.filter { it.metodoPago == metodo.name }.sumOf { it.total }
                }
                _uiState.update {
                    it.copy(
                        ventasHoy = ventas,
                        totalHoy = ventas.sumOf { v -> v.total },
                        totalesPorMetodo = totalesPorMetodo,
                        totalFiado = totalesPorMetodo[MetodoPago.FIADO] ?: 0.0,
                        cantidadFiados = ventas.count { v -> v.metodoPago == MetodoPago.FIADO.name }
                    )
                }
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        authRepository.logout()
        onLogout()
    }
}
