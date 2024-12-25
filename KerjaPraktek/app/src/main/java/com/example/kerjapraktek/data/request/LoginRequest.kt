package com.example.kerjapraktek.data.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)
data class LogoutRequest(
    @SerializedName("refreshToken") val refreshToken: String
)