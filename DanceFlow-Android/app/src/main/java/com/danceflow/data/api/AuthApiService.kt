package com.danceflow.app.data.api

import com.danceflow.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<UserProfileResponse>

    @PUT("api/auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfileResponse>

    @PUT("api/auth/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>
}

