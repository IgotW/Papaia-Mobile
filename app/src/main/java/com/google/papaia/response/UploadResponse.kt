package com.google.papaia.response

data class UploadResponse(
    val message: String,
    val profilePicture: String,
    val success: Boolean
)