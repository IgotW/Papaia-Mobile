package com.google.papaia.request

data class MonthlyAnalyticsRequest(
    val month: String,
    val predictions: Map<String, Int>
)