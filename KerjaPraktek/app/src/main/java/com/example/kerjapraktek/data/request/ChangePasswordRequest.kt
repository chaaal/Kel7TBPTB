package com.example.kerjapraktek.data.request

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)