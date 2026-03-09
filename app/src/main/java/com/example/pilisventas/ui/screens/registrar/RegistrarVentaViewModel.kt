package com.example.pilisventas.ui.screens.registrar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilisventas.data.model.ItemVenta
import com.example.pilisventas.data.model.MetodoPago
import com.example.pilisventas.data.model.Venta
import com.example.pilisventas.data.repository.AuthRepository
import com.example.pilisventas.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItemBorrador(
    val descripcion: String = "",
    val cantidadStr: String = "1",
    val precioStr: String = ""
) {
    val subtotal: Double
        get() {
            val cantidad = cantidadStr.toIntOrNull() ?: 0
            val precio = precioStr.replace(",", ".").toDoubleOrNull() ?: 0.0
            return cantidad * precio
        }
}

data class RegistrarVentaUiState(
    val items: List<ItemBorrador> = listOf(ItemBorrador()),
    val metodoPago: MetodoPago = MetodoPago.EFECTIVO,
    val notas: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val total: Double get() = items.sumOf { it.subtotal }
}

class RegistrarVentaViewModel : ViewModel() {
    private val ventaRepository = VentaRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(RegistrarVentaUiState())
    val uiState: StateFlow<RegistrarVentaUiState> = _uiState.asStateFlow()

    fun addItem() = _uiState.update { it.copy(items = it.items + ItemBorrador(), error = null) }

    fun removeItem(index: Int) {
        if (_uiState.value.items.size <= 1) return
        _uiState.update { it.copy(items = it.items.filterIndexed { i, _ -> i != index }) }
    }

    fun updateItem(index: Int, item: ItemBorrador) {
        _uiState.update {
            val newItems = it.items.toMutableList()
            newItems[index] = item
            it.copy(items = newItems, error = null)
        }
    }

    fun onMetodoPagoChange(value: MetodoPago) = _uiState.update { it.copy(metodoPago = value) }
    fun onNotasChange(value: String) = _uiState.update { it.copy(notas = value) }

    fun guardarVenta(onSuccess: () -> Unit) {
        if (_uiState.value.total <= 0) {
            _uiState.update { it.copy(error = "Agregá al menos un producto con precio válido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val usuario = authRepository.getCurrentUsuario()
            val itemsVenta = _uiState.value.items
                .filter { it.subtotal > 0 }
                .map { item ->
                    ItemVenta(
                        descripcion = item.descripcion.trim(),
                        cantidad = item.cantidadStr.toIntOrNull() ?: 1,
                        precioUnitario = item.precioStr.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        subtotal = item.subtotal
                    )
                }
            val venta = Venta(
                items = itemsVenta,
                total = _uiState.value.total,
                metodoPago = _uiState.value.metodoPago.name,
                notas = _uiState.value.notas.trim(),
                vendedorId = usuario?.uid ?: "",
                vendedorNombre = usuario?.nombre ?: "",
                fecha = System.currentTimeMillis()
            )
            ventaRepository.registrarVenta(venta).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = "Error al guardar: ${error.message}") }
                }
            )
        }
    }
}
