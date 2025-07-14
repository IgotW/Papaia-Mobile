package com.google.papaia.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val suffix: String?,
    val street: String,
    val barangay: String,
    val municipality: String,
    val province: String,
    val zipCode: String,
    val profilePicture: String?,
    val emailVerified: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)
