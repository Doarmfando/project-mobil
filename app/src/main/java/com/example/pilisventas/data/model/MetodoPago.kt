package com.example.pilisventas.data.model

enum class MetodoPago(val displayName: String) {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia"),
    TARJETA("Tarjeta"),
    FIADO("Fiado"),
    OTRO("Otro")
}
