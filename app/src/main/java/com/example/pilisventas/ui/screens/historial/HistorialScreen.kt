package com.example.pilisventas.ui.screens.historial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilisventas.data.model.MetodoPago
import com.example.pilisventas.ui.screens.home.VentaItem
import com.example.pilisventas.ui.screens.home.formatMonto
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarPickerDesde by remember { mutableStateOf(false) }
    var mostrarPickerHasta by remember { mutableStateOf(false) }

    val datePickerDesdeState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.fechaDesde
    )
    val datePickerHastaState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.fechaHasta
    )

    if (mostrarPickerDesde) {
        DatePickerDialog(
            onDismissRequest = { mostrarPickerDesde = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerDesdeState.selectedDateMillis?.let { viewModel.setFechaDesde(it) }
                    mostrarPickerDesde = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPickerDesde = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerDesdeState)
        }
    }

    if (mostrarPickerHasta) {
        DatePickerDialog(
            onDismissRequest = { mostrarPickerHasta = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerHastaState.selectedDateMillis?.let { viewModel.setFechaHasta(it) }
                    mostrarPickerHasta = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPickerHasta = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerHastaState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de ventas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selector de fechas
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { mostrarPickerDesde = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(formatFecha(uiState.fechaDesde), fontSize = 13.sp)
                    }
                    Text("→", modifier = Modifier.align(Alignment.CenterVertically), fontSize = 18.sp)
                    OutlinedButton(
                        onClick = { mostrarPickerHasta = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(formatFecha(uiState.fechaHasta), fontSize = 13.sp)
                    }
                }
            }

            // Card resumen del período
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total del período", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = formatMonto(uiState.totalPeriodo),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${uiState.ventas.size} ventas",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Desglose por método
            val metodosFiltrados = MetodoPago.entries.filter {
                (uiState.totalesPorMetodo[it] ?: 0.0) > 0
            }
            if (metodosFiltrados.isNotEmpty()) {
                item {
                    Text("Por método de pago", fontWeight = FontWeight.SemiBold)
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        metodosFiltrados.forEach { metodo ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(metodo.displayName, fontWeight = FontWeight.Medium)
                                    Text(
                                        formatMonto(uiState.totalesPorMetodo[metodo] ?: 0.0),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Lista de ventas
            item {
                Text("Ventas", fontWeight = FontWeight.SemiBold)
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.ventas.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay ventas en este período.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(uiState.ventas) { venta ->
                    VentaItem(venta = venta)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

fun formatFecha(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
