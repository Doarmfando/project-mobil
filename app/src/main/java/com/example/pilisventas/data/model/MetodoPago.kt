package com.example.pilisventas.data.model

enum class MetodoPago(val displayName: String) {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia"),
    TARJETA("Tarjeta"),
    YAPE("Yape"),
    OTRO("Otro")
}
