package com.google.papaia.request

data class WeeklyAnalyticsRequest(
    val week: String,
    val predictions: Map<String, Int>
)