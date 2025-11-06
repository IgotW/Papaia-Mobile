package com.google.papaia.response

import com.google.papaia.request.YearlyAnalyticsRequest

data class YearlyAnalyticsResponse(
    val yearlyStats: List<YearlyAnalyticsRequest>
)