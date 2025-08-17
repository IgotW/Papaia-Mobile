package com.google.papaia.request

data class DailyTipRequest(
    val userId: String,
    val text: String,
    val lastUpdated: String
)