package com.google.papaia.request

data class ResetPasswordRequest (
    val userId: String,
    val newPassword: String
)