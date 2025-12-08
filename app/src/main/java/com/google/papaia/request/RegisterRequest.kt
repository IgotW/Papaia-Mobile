package com.google.papaia.request

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val suffix: String?,
    val contactNumber: String,
    val profilePicture: String? = ""
)
