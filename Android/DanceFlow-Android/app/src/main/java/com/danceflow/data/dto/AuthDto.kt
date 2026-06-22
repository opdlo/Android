package com.danceflow.app.data.dto

// 认证相关 DTO
data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String
)

data class AuthResponse(
    val token: String,
    val userId: Long,
    val username: String
)

data class UpdateProfileRequest(
    val gender: String?,
    val birthday: String?,
    val signature: String?,
    val avatarUrl: String?
)

data class UserProfileResponse(
    val id: Long,
    val username: String,
    val gender: String?,
    val birthday: String?,
    val signature: String?,
    val avatarUrl: String?,
    val backgroundUrl: String?,
    val createdAt: String
)
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
