package com.google.papaia.utils

import com.google.papaia.request.ForgotPassword1Request
import com.google.papaia.request.LatLonRequest
import com.google.papaia.request.LoginRequest
import com.google.papaia.request.OtpRequest
import com.google.papaia.request.PasswordChangeRequest
import com.google.papaia.request.RegisterRequest
import com.google.papaia.request.ResetPasswordRequest
import com.google.papaia.request.UpdateFcmRequest
import com.google.papaia.response.ApiResponse
import com.google.papaia.response.ChangePasswordResponse
import com.google.papaia.response.LoginResponse
import com.google.papaia.response.OtpResponse
import com.google.papaia.response.PredictionResponse
import com.google.papaia.response.RegisterResponse
import com.google.papaia.response.UserResponse
import com.google.papaia.response.DailyAnalyticsResponse
import com.google.papaia.response.DailyTipResponse
import com.google.papaia.response.FarmDetailsResponse
import com.google.papaia.response.FcmResponse
import com.google.papaia.response.IdentificationStatsResponse
import com.google.papaia.response.PredictionHistoryResponse
import com.google.papaia.response.ScanResult
import com.google.papaia.response.SummaryResponse
import com.google.papaia.response.TipResponse
import com.google.papaia.response.TodaysPredictionResponse
import com.google.papaia.response.UploadResponse
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

    @GET("/api/user/{idNumber}")
    fun getUserById(
        @Header("Authorization") token: String,
        @Path("idNumber") userId: String
    ): Call<UserResponse>

    @GET("/api/farmer/weekly-analytics")
    fun getWeeklyAnalytics(
        @Header("Authorization") token: String
    ): Call<DailyAnalyticsResponse>

    @GET("/api/farmer/daily-analytics")
    fun getDailyAnalytics(
        @Header("Authorization") token: String
    ): Call<DailyAnalyticsResponse>

//    @GET("/api/farmer/daily-tip")
//    fun getDailyTip(
//        @Header("Authorization") token: String
//    ): Call<DailyTipResponse>

    @GET("/api/farmer/predict-history")
    fun getPredictionHistory(
        @Header("Authorization") bearerToken: String
    ): Call<List<PredictionHistoryResponse>>

    @GET("/api/farmer/prediction/{id}")
    fun getPredictionById(
        @Path("id") predictionId: String,
        @Header("Authorization") token: String
    ): Call<ScanResult>

    @GET("/api/farmer/farmer-farm")
    fun getFarmDetails(
        @Header("Authorization") token: String
    ): Call<FarmDetailsResponse>

    @GET("/api/farmer/all-predictions")
    fun getTodaysPredictionsCount(
        @Header("Authorization") token: String
    ): Call<TodaysPredictionResponse>

    @GET("/api/farmer/daily-tip")
    fun getDailyTip(
        @Header("Authorization") token: String
    ): Call<TipResponse>

    @GET("/api/farmer/stats")
    fun getFarmerIdentificationStats(
        @Header("Authorization") token: String
    ): Call<IdentificationStatsResponse>

    @GET("/api/farmer/last-five-days-summary")
    fun getFiveDaysSummary(
        @Header("Authorization") token: String
    ): Call<SummaryResponse>

    @POST("/api/farmer/daily-tip")
    fun generateDailyTip(
        @Header("Authorization") bearerToken: String,
        @Body body: LatLonRequest
    ): Call<TipResponse>

    @POST("api/farmer/update-fcm-token")
    fun updateFcmToken(
        @Header("Authorization") auth: String,
        @Body body: UpdateFcmRequest
    ): Call<FcmResponse>
    // fallback if you cannot supply Authorization header (backend accepts farmerId in body)
    @POST("api/farmer/update-fcm-token")
    fun updateFcmTokenNoAuth(
        @Body body: UpdateFcmRequest
    ): Call<Void>

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
    @POST("/api/farmer/predict")
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

    @Multipart
    @PUT("profile-picture")
    fun updateProfilePicture(
        @Header("Authorization") token: String,
        @Part profilePicture: MultipartBody.Part
    ): Call<UploadResponse>
}