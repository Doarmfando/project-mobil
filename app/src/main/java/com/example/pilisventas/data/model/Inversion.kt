package com.example.pilisventas.data.model

data class Inversion(
    val id: String = "",
    val mes: String = "",
    val monto: Double = 0.0,
    val asignadaPorId: String = "",
    val asignadaPorNombre: String = "",
    val fecha: Long = System.currentTimeMillis()
)
