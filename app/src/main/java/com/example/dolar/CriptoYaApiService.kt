package com.example.dolar

import retrofit2.http.GET

// ve.dolarapi.com no tiene tasa de USDT/Binance, así que esa dato sale de
// criptoya.com (API pública, sin API key). La respuesta es un objeto con
// una clave por exchange ("binancep2p", "okexp2p", etc.) -> se usa "binancep2p"
// porque es la referencia más común de "tasa USDT" en Venezuela.
interface CriptoYaApiService {
    @GET("api/usdt/ves/1")
    suspend fun getUsdtVes(): Map<String, ExchangeQuote>
}
