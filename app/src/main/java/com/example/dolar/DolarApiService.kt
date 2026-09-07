package com.example.dolar

import retrofit2.http.GET
import retrofit2.http.Path

interface DolarApiService {
    @GET("v1/dolares")
    suspend fun getDolares(): List<DolarResponse>

    @GET("v1/euros")
    suspend fun getEuros(): List<DolarResponse>

    // Endpoint "por día" (el masivo v1/historicos/dolares está desactualizado
    // para la tasa oficial y no trae paralelo). fuente = "oficial" o "paralelo".
    // anio en 4 dígitos, mes y dia en 2 dígitos (ej: "2026", "09", "07").
    @GET("v1/historicos/dolares/{fuente}/{anio}/{mes}/{dia}")
    suspend fun getHistoricoDolar(
        @Path("fuente") fuente: String,
        @Path("anio") anio: String,
        @Path("mes") mes: String,
        @Path("dia") dia: String
    ): HistoricoResponse

    // Mismo patrón que el histórico de dólares, pero para euros. Verificado
    // que funciona igual (404 los días sin publicación, ej. fines de semana).
    @GET("v1/historicos/euros/{fuente}/{anio}/{mes}/{dia}")
    suspend fun getHistoricoEuro(
        @Path("fuente") fuente: String,
        @Path("anio") anio: String,
        @Path("mes") mes: String,
        @Path("dia") dia: String
    ): HistoricoResponse
}
