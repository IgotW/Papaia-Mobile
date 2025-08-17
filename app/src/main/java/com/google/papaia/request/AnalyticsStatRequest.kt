package com.google.papaia.request

data class AnalyticsStatRequest(
    val day: String,
    val predictions: Map<String, Int>
)