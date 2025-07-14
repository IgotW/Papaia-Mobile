package com.google.papaia.response

data class RegisterResponse(
    val success: Boolean,
    val message: String?,
    val id: String?,       // Will be null if not returned
    val error: String?     // Will be null on success
)
