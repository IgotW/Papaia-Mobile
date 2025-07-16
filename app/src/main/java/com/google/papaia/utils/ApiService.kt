package com.google.papaia.utils

import com.google.papaia.model.User
import com.google.papaia.request.LoginRequest
import com.google.papaia.request.RegisterRequest
import com.google.papaia.response.ApiResponse
import com.google.papaia.response.LoginResponse
import com.google.papaia.response.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")

    @POST("/api/user")
    fun registerUser(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @POST("/api/login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>
}