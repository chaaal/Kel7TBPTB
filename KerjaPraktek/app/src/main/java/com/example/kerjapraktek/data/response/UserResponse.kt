package com.example.kerjapraktek.data.response

data class UserResponse(
	val data: DataUser,
	val success: Boolean,
	val message: String
)

data class DataUser(
	val createdAt: String,
	val password: String,
	val jabatan: String,
	val name: String,
	val noHp: String,
	val id: Int,
	val email: String,
	val updatedAt: String
)

