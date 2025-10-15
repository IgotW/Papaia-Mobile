package com.google.papaia.model

data class DailySummary(
    val date: String,
    val counts: Map<String, Int>,
    val total: Int
)