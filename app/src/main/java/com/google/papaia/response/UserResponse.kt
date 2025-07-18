package com.google.papaia.response

import com.google.papaia.model.User

data class UserResponse(
    val success: Boolean,
    val message: String,
    val user: User
)