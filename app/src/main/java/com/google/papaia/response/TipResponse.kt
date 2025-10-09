package com.google.papaia.response

import com.google.papaia.model.TipData

data class TipResponse(
    val tip: TipData? = null,
    val date: String? = null
)