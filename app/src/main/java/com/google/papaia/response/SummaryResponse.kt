package com.google.papaia.response

import com.google.papaia.model.DailySummary

data class SummaryResponse(
    val lastFiveDays: List<String>,
    val dailySummaries: List<DailySummary>,
    val trends: List<String>,
    val healthyTrend: String,
    val summary: String
)