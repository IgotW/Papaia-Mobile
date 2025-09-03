package com.google.papaia.response

import com.google.gson.annotations.SerializedName

data class ScanResult(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("confidence")
    val confidence: Double,

    @SerializedName("farmId")
    val farmId: String?,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("prediction")
    val prediction: String,

    @SerializedName("suggestion")
    val suggestion: String? = null,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("userId")
    val userId: String
)