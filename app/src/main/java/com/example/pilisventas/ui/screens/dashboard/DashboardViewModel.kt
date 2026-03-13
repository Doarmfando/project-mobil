package com.example.pilisventas.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilisventas.data.model.Inversion
import com.example.pilisventas.data.model.MetodoPago
import com.example.pilisventas.data.model.Usuario
import com.example.pilisventas.data.model.Venta
import com.example.pilisventas.data.repository.AuthRepository
import com.example.pilisventas.data.repository.InversionRepository
import com.example.pilisventas.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val usuario: Usuario? = null,
    val ventasHoy: List<Venta> = emptyList(),
    val totalHoy: Double = 0.0,
    val totalesPorMetodo: Map<MetodoPago, Double> = emptyMap(),
    val totalDelMes: Double = 0.0,
    val inversion: Inversion? = null,
    val mostrarDialogInversion: Boolean = false
) {
    val ganancia: Double get() = totalDelMes - (inversion?.monto ?: 0.0)
}

class DashboardViewModel : ViewModel() {
    private val ventaRepository = VentaRepository()
    private val authRepository = AuthRepository()
    private val inversionRepository = InversionRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Ventas del mes antes de hoy (estático, no cambia durante el día)
    private var totalMesSinHoy: Double = 0.0

    init {
        loadData()
        cargarDatosMes()
    }

    private fun loadData() {
        viewModelScope.launch {
            val usuario = authRepository.getCurrentUsuario()
            _uiState.update { it.copy(usuario = usuario) }

            ventaRepository.getVentasDeHoy().catch { }.collect { ventas ->
                val totalHoy = ventas.sumOf { v -> v.total }
                val totalesPorMetodo = MetodoPago.entries.associateWith { metodo ->
                    ventas.filter { it.metodoPago == metodo.name }.sumOf { it.total }
                }
                _uiState.update {
                    it.copy(
                        ventasHoy = ventas,
                        totalHoy = totalHoy,
                        totalDelMes = totalMesSinHoy + totalHoy,
                        totalesPorMetodo = totalesPorMetodo
                    )
                }
            }
        }
    }

    fun cargarDatosMes() {
        viewModelScope.launch {
            val mes = mesActual()
            val primeroDeMes = primeroDeMes()
            val inicioDeHoy = inicioDeHoy()

            // Ventas del mes excluyendo hoy (para no duplicar con el flow en tiempo real)
            ventaRepository.getVentasPorRango(primeroDeMes, inicioDeHoy - 1).fold(
                onSuccess = { ventas ->
                    totalMesSinHoy = ventas.sumOf { v -> v.total }
                    _uiState.update { it.copy(totalDelMes = totalMesSinHoy + it.totalHoy) }
                },
                onFailure = { }
            )

            inversionRepository.getInversionDelMes(mes).fold(
                onSuccess = { inv -> _uiState.update { it.copy(inversion = inv) } },
                onFailure = { }
            )
        }
    }

    fun mostrarDialogInversion() = _uiState.update { it.copy(mostrarDialogInversion = true) }
    fun ocultarDialogInversion() = _uiState.update { it.copy(mostrarDialogInversion = false) }

    fun guardarInversion(monto: Double) {
        viewModelScope.launch {
            val usuario = _uiState.value.usuario ?: return@launch
            val invActual = _uiState.value.inversion
            val nueva = (invActual ?: Inversion()).copy(
                mes = mesActual(),
                monto = monto,
                asignadaPorId = usuario.uid,
                asignadaPorNombre = usuario.nombre,
                fecha = System.currentTimeMillis()
            )
            inversionRepository.setInversion(nueva).fold(
                onSuccess = {
                    _uiState.update { it.copy(mostrarDialogInversion = false) }
                    cargarDatosMes()
                },
                onFailure = { }
            )
        }
    }

    fun logout(onLogout: () -> Unit) {
        authRepository.logout()
        onLogout()
    }

    private fun mesActual(): String {
        val cal = Calendar.getInstance()
        return "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    private fun primeroDeMes(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun inicioDeHoy(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
