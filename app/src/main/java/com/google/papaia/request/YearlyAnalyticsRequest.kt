package com.google.papaia.request

data class YearlyAnalyticsRequest(
    val year: String,
    val predictions: Map<String, Int>
)