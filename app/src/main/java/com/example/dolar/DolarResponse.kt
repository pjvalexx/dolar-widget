package com.example.dolar

data class DolarResponse(
    val fuente: String,
    val nombre: String,
    val compra: Double,
    val venta: Double,
    val promedio: Double,
    val fechaActualizacion: String
)