package com.danceflow.app.data.api

import com.danceflow.app.data.dto.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApiService {

    @Multipart
    @POST("api/upload")
    suspend fun uploadFiles(
        @Part files: List<MultipartBody.Part>
    ): Response<UploadResponse>
}
