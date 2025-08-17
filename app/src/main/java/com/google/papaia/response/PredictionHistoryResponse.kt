package com.google.papaia.response

data class PredictionHistoryResponse(
    val id: String,
    val userId: String,
    val imageUrl: String,
    val prediction: String,
    val confidence: Double,
    val timestamp: String // Already formatted as "MM/dd/yyyy hh:mm a"
)