package com.example.dolar

import retrofit2.http.GET

interface DolarApiService {
    @GET("v1/dolares/oficial")
suspend fun getOficialDolar(): DolarResponse
}