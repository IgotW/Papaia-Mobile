package com.google.papaia.model

import com.google.papaia.utils.Timestamp

data class TipData(
    val userId: String?,
    val text: String?,
    val weather: String?,
    val lastUpdated: Timestamp?,
    val date: String? = null
)