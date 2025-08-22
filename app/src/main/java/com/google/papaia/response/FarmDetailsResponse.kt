package com.google.papaia.response

data class FarmDetailsResponse(
    val farmName: String,
    val farmOwnerName: String,
    val farmId: String,
    val ownerId: String
)