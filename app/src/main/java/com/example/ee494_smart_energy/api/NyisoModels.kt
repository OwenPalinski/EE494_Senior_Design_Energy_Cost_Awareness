package com.example.ee494_smart_energy.api

data class NyisoResponse(
    val marketDate: String,
    val source: String,
    val totalRows: Int,
    val data: List<LbmpRow>
)

data class LbmpRow(
    val name: String?,
    val ptid: Int?,
    val hour: String?,
    val lbmp: Double?,
    val marginalCostLosses: Double?,
    val marginalCostCongestion: Double?
)