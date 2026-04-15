package com.example.ee494_smart_energy.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NyisoApiService {
    @GET("api/power-readings")
    suspend fun getPowerReadings(): List<PowerReading>
    @GET("api/devices")
    suspend fun getDeviceIds(): List<String>
    @GET("api/lbmp")
    suspend fun getLbmpData(
        @Query("date") date: String? = null
    ): NyisoResponse
}