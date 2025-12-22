package com.example.dolar

import retrofit2.http.GET

interface DolarApiService {
    @GET("v1/dolares")
suspend fun getDolares(): List<DolarResponse>
}