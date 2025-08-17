package com.google.papaia.response

import com.google.papaia.request.DailyTipRequest

data class DailyTipResponse(
    val message: String,
    val tip: DailyTipRequest
)