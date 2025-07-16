package com.google.papaia.response

import com.google.papaia.model.User

data class LoginResponse(
    val message: String,
    val token: String,
    val user: User
)