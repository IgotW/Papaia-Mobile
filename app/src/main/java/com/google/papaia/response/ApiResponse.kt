package com.google.papaia.response

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val id: String? = null,
    val error: String? = null
)