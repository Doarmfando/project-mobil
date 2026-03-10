package com.example.pilisventas.ui.screens.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilisventas.data.model.MetodoPago
import com.example.pilisventas.data.model.Venta
import com.example.pilisventas.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class HistorialUiState(
    val fechaDesde: Long = primeroDeMes(),
    val fechaHasta: Long = finDeHoy(),
    val ventas: List<Venta> = emptyList(),
    val totalPeriodo: Double = 0.0,
    val totalesPorMetodo: Map<MetodoPago, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

private fun primeroDeMes(): Long = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun finDeHoy(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis

class HistorialViewModel : ViewModel() {
    private val ventaRepository = VentaRepository()

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        buscarVentas()
    }

    fun setFechaDesde(millis: Long) {
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        _uiState.update { it.copy(fechaDesde = startOfDay) }
        buscarVentas()
    }

    fun setFechaHasta(millis: Long) {
        val endOfDay = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        _uiState.update { it.copy(fechaHasta = endOfDay) }
        buscarVentas()
    }

    private fun buscarVentas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            ventaRepository.getVentasPorRango(
                _uiState.value.fechaDesde,
                _uiState.value.fechaHasta
            ).fold(
                onSuccess = { ventas ->
                    val totalesPorMetodo = MetodoPago.entries.associateWith { metodo ->
                        ventas.filter { it.metodoPago == metodo.name }.sumOf { it.total }
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            ventas = ventas,
                            totalPeriodo = ventas.sumOf { v -> v.total },
                            totalesPorMetodo = totalesPorMetodo
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }
}
