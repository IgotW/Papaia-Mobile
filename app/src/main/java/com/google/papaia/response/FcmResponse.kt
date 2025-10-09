package com.google.papaia.response

data class FcmResponse(
    val message: String,
    val updatedData: Map<String, Any>
)