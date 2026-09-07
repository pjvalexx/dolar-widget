package com.example.dolar

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CriptoYaRetrofitClient {
    private const val BASE_URL = "https://criptoya.com/"

    val instance: CriptoYaApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(CriptoYaApiService::class.java)
    }
}
