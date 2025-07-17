package com.google.papaia.response

data class OtpResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null
)