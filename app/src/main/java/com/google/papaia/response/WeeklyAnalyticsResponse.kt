package com.google.papaia.response

import com.google.papaia.request.WeeklyAnalyticsRequest

data class WeeklyAnalyticsResponse(
    val weeklyStats: List<WeeklyAnalyticsRequest>
)