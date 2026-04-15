package com.example.ee494_smart_energy.api

data class PowerReading(
    val id: Long,
    val device_id: String,
    val voltage: Double,
    val current: Double,
    val power: Double,
    val energy: Double,
    val created_at: String
)