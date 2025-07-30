package com.google.papaia.request

data class PasswordChangeRequest(
    val password: String,
    val newPassword: String
)