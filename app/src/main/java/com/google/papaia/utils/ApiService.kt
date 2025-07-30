package com.google.papaia.utils

import com.google.papaia.model.User
import com.google.papaia.request.ForgotPassword1Request
import com.google.papaia.request.LoginRequest
import com.google.papaia.request.OtpRequest
import com.google.papaia.request.PasswordChangeRequest
import com.google.papaia.request.RegisterRequest
import com.google.papaia.request.ResetPasswordRequest
import com.google.papaia.response.ApiResponse
import com.google.papaia.response.ChangePasswordResponse
import com.google.papaia.response.LoginResponse
import com.google.papaia.response.OtpResponse
import com.google.papaia.response.PredictionResponse
import com.google.papaia.response.RegisterResponse
import com.google.papaia.response.UserResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @Headers("Content-Type: application/json")

    @GET("/api/user/{id}")
    fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): Call<UserResponse>

    @POST("/api/user")
    fun registerUser(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @POST("/api/login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("/api/forgot-password")
    fun sendOtp(@Body request: ForgotPassword1Request): Call<ApiResponse>

    @POST("/api/verify-otp")
    fun verifyOtp(@Body request: OtpRequest): Call<OtpResponse>

    @POST("/api/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ApiResponse>

    @Multipart
    @POST("/api/predict")
    fun predictDisease(
        @Part image: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Call<PredictionResponse>

    @PUT("/api/user/{id}")
    fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body updatedData: Map<String, String>
    ): Call<ApiResponse>

    @PUT("/api/password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body passwordChangeRequest: PasswordChangeRequest
    ): Call<ChangePasswordResponse>
}