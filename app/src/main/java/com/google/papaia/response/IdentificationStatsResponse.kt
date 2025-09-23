package com.google.papaia.response

data class IdentificationStatsResponse(
    val idNumber: String,
    val healthy: Int,
    val diseased: Int,
    val total: Int,
    val healthyPercentage: String
)