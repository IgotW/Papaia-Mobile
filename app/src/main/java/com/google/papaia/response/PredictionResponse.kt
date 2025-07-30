package com.google.papaia.response

data class PredictionResponse(
    val success: Boolean,
    val predictedLabel: String,
    val confidence: Double,
    val imageUrl: String,
    val userId: String
)