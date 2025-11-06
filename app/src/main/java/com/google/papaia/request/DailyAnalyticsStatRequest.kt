package com.google.papaia.request

data class DailyAnalyticsStatRequest(
    val day: String,
    val predictions: Map<String, Int>
)