package com.google.papaia.response

import com.google.papaia.utils.Timestamp

data class Identifications(
    val id: String,
    val prediction: String,
    val timestamp: Timestamp
)