package com.example.pilisventas.ui.screens.registrar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilisventas.data.model.MetodoPago
import com.example.pilisventas.ui.screens.home.formatMonto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarVentaScreen(
    viewModel: RegistrarVentaViewModel,
    onVentaGuardada: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva venta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text("Productos", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

            uiState.items.forEachIndexed { index, item ->
                ItemBorradorCard(
                    item = item,
                    canRemove = uiState.items.size > 1,
                    onUpdate = { viewModel.updateItem(index, it) },
                    onRemove = { viewModel.removeItem(index) }
                )
            }

            OutlinedButton(
                onClick = viewModel::addItem,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Agregar otro producto")
            }

            // Total calculado automáticamente
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        text = formatMonto(uiState.total),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text("Método de pago", fontWeight = FontWeight.Medium, fontSize = 15.sp)

            Column {
                MetodoPago.entries.forEach { metodo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = uiState.metodoPago == metodo,
                            onClick = { viewModel.onMetodoPagoChange(metodo) }
                        )
                        Text(metodo.displayName, fontSize = 15.sp)
                    }
                }
            }

            OutlinedTextField(
                value = uiState.notas,
                onValueChange = viewModel::onNotasChange,
                label = { Text("Notas (opcional)") },
                placeholder = {
                    Text(if (uiState.metodoPago == MetodoPago.FIADO) "Nombre del cliente que debe" else "Notas adicionales")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Button(
                onClick = { viewModel.guardarVenta(onVentaGuardada) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar venta", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ItemBorradorCard(
    item: ItemBorrador,
    canRemove: Boolean,
    onUpdate: (ItemBorrador) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.descripcion,
                    onValueChange = { onUpdate(item.copy(descripcion = it)) },
                    label = { Text("Producto (opcional)") },
                    placeholder = { Text("Ej: Blusa roja talla M") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item.cantidadStr,
                    onValueChange = { onUpdate(item.copy(cantidadStr = it)) },
                    label = { Text("Cant.") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Text("×", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = item.precioStr,
                    onValueChange = { onUpdate(item.copy(precioStr = it)) },
                    label = { Text("Precio unit.") },
                    modifier = Modifier.weight(2f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            if (item.subtotal > 0) {
                Text(
                    text = "Subtotal: ${formatMonto(item.subtotal)}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
