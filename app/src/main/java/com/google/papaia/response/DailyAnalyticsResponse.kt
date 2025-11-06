package com.google.papaia.response

import com.google.papaia.request.DailyAnalyticsStatRequest

data class DailyAnalyticsResponse(
    val dailyStats: List<DailyAnalyticsStatRequest>
)