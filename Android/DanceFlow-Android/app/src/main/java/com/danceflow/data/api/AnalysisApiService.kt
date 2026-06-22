package com.danceflow.app.data.api

import com.danceflow.app.data.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AnalysisApiService {

    @GET("api/analysis/practices")
    suspend fun getPracticeHistory(): Response<PracticeHistoryListResponse>

    @GET("api/analysis/practices/{id}")
    suspend fun getPractice(@Path("id") practiceId: Long): Response<PracticeHistory>

    @Multipart
    @POST("api/analysis/practices")
    suspend fun createPractice(
        @Part video: MultipartBody.Part,
        @Part("danceStyle") danceStyle: MultipartBody.Part?
    ): Response<PracticeHistory>

    @GET("api/analysis/practices/{id}/result")
    suspend fun getAnalysisResult(@Path("id") practiceId: Long): Response<AnalysisResult>
}
