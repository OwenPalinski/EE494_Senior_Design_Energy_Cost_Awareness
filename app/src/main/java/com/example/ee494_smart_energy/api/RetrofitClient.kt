package com.example.ee494_smart_energy.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.84.75.118:3000/"
    // For emulator only

    val apiService: NyisoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NyisoApiService::class.java)
    }
}