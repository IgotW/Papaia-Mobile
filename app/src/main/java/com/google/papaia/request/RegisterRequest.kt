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
    val birthDate: String,
    val contactNumber: String,
    val street: String,
    val barangay: String,
    val municipality: String,
    val province: String,
    val zipCode: String,
    val profilePicture: String? = ""
)
