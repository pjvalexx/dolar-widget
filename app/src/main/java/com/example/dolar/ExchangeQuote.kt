package com.example.dolar

// Una cotización de un exchange en la respuesta de criptoya.com
// (esa API trae más campos como totalAsk/totalBid; los ignoramos).
data class ExchangeQuote(
    val ask: Double,
    val bid: Double,
    val time: Long
)
