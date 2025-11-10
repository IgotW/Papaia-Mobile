package com.google.papaia.response

data class PredictionResponse(
    val success: Boolean,
    val message: String?,
    val userId: String?,
//    val idNumber: String?,
    val farmId: String?,
    val imageUrl: String?,
    val prediction: String?,
    val confidence: Double?,
    val suggestions: String?,
    val id: String?,
    val timestamp: Any?
)