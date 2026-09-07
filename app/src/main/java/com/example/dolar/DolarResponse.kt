package com.example.dolar

data class DolarResponse(
    val moneda: String? = null, // "USD" o "EUR" (no viene en todos los endpoints viejos)
    val fuente: String,
    val nombre: String,
    val compra: Double?,
    val venta: Double?,
    val promedio: Double,
    val fechaActualizacion: String
)