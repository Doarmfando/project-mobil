package com.example.pilisventas.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilisventas.data.model.Usuario
import com.example.pilisventas.data.model.Venta
import com.example.pilisventas.data.repository.AuthRepository
import com.example.pilisventas.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeAyudanteUiState(
    val usuario: Usuario? = null,
    val misVentasHoy: List<Venta> = emptyList(),
    val totalHoy: Double = 0.0
)

class HomeAyudanteViewModel : ViewModel() {
    private val ventaRepository = VentaRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(HomeAyudanteUiState())
    val uiState: StateFlow<HomeAyudanteUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val usuario = authRepository.getCurrentUsuario()
            _uiState.update { it.copy(usuario = usuario) }

            ventaRepository.getVentasDeHoy().catch { }.collect { ventas ->
                val misVentas = ventas.filter { it.vendedorId == usuario?.uid }
                _uiState.update {
                    it.copy(
                        misVentasHoy = misVentas,
                        totalHoy = misVentas.sumOf { v -> v.total }
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
