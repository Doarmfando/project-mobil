package com.example.pilisventas.ui.screens.editar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pilisventas.data.model.ItemVenta
import com.example.pilisventas.data.model.MetodoPago
import com.example.pilisventas.data.model.Venta
import com.example.pilisventas.data.repository.AuthRepository
import com.example.pilisventas.data.repository.VentaRepository
import com.example.pilisventas.ui.screens.registrar.ItemBorrador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditarVentaUiState(
    val ventaOriginal: Venta? = null,
    val items: List<ItemBorrador> = listOf(ItemBorrador()),
    val metodoPago: MetodoPago = MetodoPago.EFECTIVO,
    val notas: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val total: Double get() = items.sumOf { it.subtotal }
}

class EditarVentaViewModel(private val ventaId: String) : ViewModel() {
    private val ventaRepository = VentaRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(EditarVentaUiState())
    val uiState: StateFlow<EditarVentaUiState> = _uiState.asStateFlow()

    init {
        cargarVenta()
    }

    private fun cargarVenta() {
        viewModelScope.launch {
            ventaRepository.getVentaPorId(ventaId).fold(
                onSuccess = { venta ->
                    val items = venta.items.map { item ->
                        ItemBorrador(
                            descripcion = item.descripcion,
                            cantidadStr = item.cantidad.toString(),
                            precioStr = if (item.precioUnitario % 1.0 == 0.0)
                                item.precioUnitario.toInt().toString()
                            else item.precioUnitario.toString()
                        )
                    }.ifEmpty { listOf(ItemBorrador()) }
                    val metodo = runCatching { MetodoPago.valueOf(venta.metodoPago) }.getOrDefault(MetodoPago.EFECTIVO)
                    _uiState.update {
                        it.copy(
                            ventaOriginal = venta,
                            items = items,
                            metodoPago = metodo,
                            notas = venta.notas,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

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

    fun guardarEdicion(onSuccess: () -> Unit) {
        val state = _uiState.value
        val original = state.ventaOriginal ?: return
        if (state.total <= 0) {
            _uiState.update { it.copy(error = "Agregá al menos un producto con precio válido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val editor = authRepository.getCurrentUsuario()
            val itemsVenta = state.items
                .filter { it.subtotal > 0 }
                .map { item ->
                    ItemVenta(
                        descripcion = item.descripcion.trim(),
                        cantidad = item.cantidadStr.toIntOrNull() ?: 1,
                        precioUnitario = item.precioStr.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        subtotal = item.subtotal
                    )
                }
            val ventaActualizada = original.copy(
                items = itemsVenta,
                total = state.total,
                metodoPago = state.metodoPago.name,
                notas = state.notas.trim(),
                modificadoPorId = editor?.uid ?: "",
                modificadoPorNombre = editor?.nombre ?: "",
                fechaModificacion = System.currentTimeMillis()
            )
            ventaRepository.editarVenta(ventaId, ventaActualizada).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false, error = "Error al guardar: ${error.message}") }
                }
            )
        }
    }
}

class EditarVentaViewModelFactory(private val ventaId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = EditarVentaViewModel(ventaId) as T
}
