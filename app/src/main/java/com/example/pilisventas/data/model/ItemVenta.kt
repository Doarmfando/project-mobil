package com.example.pilisventas.data.model

data class ItemVenta(
    val descripcion: String = "",
    val cantidad: Int = 1,
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0
)
