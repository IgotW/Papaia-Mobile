package com.google.papaia.response

import com.google.papaia.model.TipData

data class TipResponse(
    val message: String? = null,
    val tip: TipData? = null
)