package com.google.papaia.response

import com.google.gson.annotations.SerializedName

data class PredictionHistoryResponse(
    val id: String,
    val userId: String,
    val imageUrl: String,
    val prediction: String,
    val confidence: Double,
    val suggestions: String?,
    val timestamp: String?,            // raw ISO string
)