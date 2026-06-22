package com.danceflow.app.data.api

import com.danceflow.app.data.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {

    @GET("api/profile/favorites")
    suspend fun getFavorites(): Response<FavoriteListResponse>

    @POST("api/profile/upload-avatar")
    @Multipart
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<String>

    @POST("api/profile/upload-background")
    @Multipart
    suspend fun uploadBackground(@Part file: MultipartBody.Part): Response<String>
}

