package com.google.papaia.response

import com.google.papaia.request.MonthlyAnalyticsRequest


data class MonthlyAnalyticsResponse(
    val monthlyStats: List<MonthlyAnalyticsRequest>
)