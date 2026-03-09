package com.example.pilisventas.data.model

data class Venta(
    val id: String = "",
    val items: List<ItemVenta> = emptyList(),
    val total: Double = 0.0,
    val metodoPago: String = MetodoPago.EFECTIVO.name,
    val notas: String = "",
    val vendedorId: String = "",
    val vendedorNombre: String = "",
    val fecha: Long = System.currentTimeMillis()
)
