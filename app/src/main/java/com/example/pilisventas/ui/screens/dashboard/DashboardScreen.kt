package com.example.pilisventas.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilisventas.data.model.MetodoPago
import com.example.pilisventas.data.model.Venta
import com.example.pilisventas.ui.screens.home.VentaItem
import com.example.pilisventas.ui.screens.home.formatMonto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNuevaVenta: () -> Unit,
    onHistorial: () -> Unit,
    onEditarVenta: (Venta) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.mostrarDialogInversion) {
        var montoStr by remember {
            mutableStateOf(uiState.inversion?.monto?.let { "%.2f".format(it) } ?: "")
        }
        AlertDialog(
            onDismissRequest = { viewModel.ocultarDialogInversion() },
            title = { Text("Inversión del mes") },
            text = {
                OutlinedTextField(
                    value = montoStr,
                    onValueChange = { montoStr = it },
                    label = { Text("Monto (S/)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val monto = montoStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    viewModel.guardarInversion(monto)
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.ocultarDialogInversion() }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dashboard", fontWeight = FontWeight.Bold)
                        Text("Hola, ${uiState.usuario?.nombre ?: "Jefa"}!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onHistorial) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevaVenta) {
                Icon(Icons.Default.Add, contentDescription = "Nueva venta")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card total del día
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total del día", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = formatMonto(uiState.totalHoy),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text("${uiState.ventasHoy.size} ventas", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Resumen del mes
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Resumen del mes", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ventas del mes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatMonto(uiState.totalDelMes), fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Inversión", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.inversion?.let { formatMonto(it.monto) } ?: "Sin asignar",
                                    fontWeight = FontWeight.Medium
                                )
                                TextButton(onClick = { viewModel.mostrarDialogInversion() }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                                    Text("Editar", fontSize = 12.sp)
                                }
                            }
                        }
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ganancia", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = formatMonto(uiState.ganancia),
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.ganancia >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Desglose por método de pago
            item {
                Text("Por método de pago", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
            }

            item {
                val metodosFiltrados = MetodoPago.entries.filter {
                    (uiState.totalesPorMetodo[it] ?: 0.0) > 0
                }
                if (metodosFiltrados.isEmpty()) {
                    Text("Sin ventas hoy.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        metodosFiltrados.forEach { metodo ->
                            MetodoRow(metodo = metodo, total = uiState.totalesPorMetodo[metodo] ?: 0.0)
                        }
                    }
                }
            }

            // Lista de ventas
            item {
                Text("Todas las ventas de hoy", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
            }

            if (uiState.ventasHoy.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay ventas hoy.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(uiState.ventasHoy) { venta ->
                    VentaItem(venta = venta, onEdit = { onEditarVenta(it) })
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun MetodoRow(metodo: MetodoPago, total: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(metodo.displayName, fontWeight = FontWeight.Medium)
            Text(formatMonto(total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
