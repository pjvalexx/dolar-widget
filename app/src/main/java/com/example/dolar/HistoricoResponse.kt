package com.example.dolar

// Respuesta del endpoint "por día" del histórico (un solo objeto, no lista).
data class HistoricoResponse(
    val fuente: String,
    val compra: Double?,
    val venta: Double?,
    val promedio: Double,
    val fecha: String
)
