package com.google.papaia.request

data class UpdateFcmRequest(
    val userId: String? = null,
    val fcmToken: String,
    val lat: Double? = null,
    val lon: Double? = null
)