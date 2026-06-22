package com.danceflow.app.data.api

import com.danceflow.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface LearningApiService {

    @GET("api/learning/categories")
    suspend fun getCategories(): Response<List<VideoCategory>>

    @GET("api/learning/videos")
    suspend fun getVideos(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("category") categoryId: Long? = null,
        @Query("search") search: String? = null
    ): Response<VideoListResponse>

    @GET("api/learning/videos/{id}")
    suspend fun getVideo(@Path("id") videoId: Long): Response<VideoResource>

    @POST("api/learning/videos/{id}/favorite")
    suspend fun favoriteVideo(@Path("id") videoId: Long): Response<Unit>

    @DELETE("api/learning/videos/{id}/favorite")
    suspend fun unfavoriteVideo(@Path("id") videoId: Long): Response<Unit>
}
