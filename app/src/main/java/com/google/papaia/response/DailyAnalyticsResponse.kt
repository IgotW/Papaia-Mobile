package com.google.papaia.response

import com.google.papaia.request.AnalyticsStatRequest

data class DailyAnalyticsResponse(
    val dailyStats: List<AnalyticsStatRequest>
)