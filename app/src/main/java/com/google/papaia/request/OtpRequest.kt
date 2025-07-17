package com.google.papaia.request

data class OtpRequest(
    val email: String,
    val otp: String
)