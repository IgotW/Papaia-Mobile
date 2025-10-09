package com.google.papaia.response

data class AnalyticsSummaryResponse(
    val previousDate: String,
    val latestDate: String,
    val previousPredictions: List<Identifications>,
    val latestPredictions: List<Identifications>,
    val summary: String
)