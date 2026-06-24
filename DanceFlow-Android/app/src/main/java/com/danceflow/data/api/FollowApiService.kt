package com.danceflow.app.data.api

import com.danceflow.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface FollowApiService {

    @POST("api/follow/{userId}")
    suspend fun follow(@Path("userId") userId: Long): Response<FollowResponse>

    @DELETE("api/follow/{userId}")
    suspend fun unfollow(@Path("userId") userId: Long): Response<Unit>

    @GET("api/follow/{userId}/followers")
    suspend fun getFollowers(@Path("userId") userId: Long): Response<List<FollowResponse>>

    @GET("api/follow/{userId}/following")
    suspend fun getFollowing(@Path("userId") userId: Long): Response<List<FollowResponse>>

    @GET("api/follow/{userId}/count")
    suspend fun getFollowCount(@Path("userId") userId: Long): Response<FollowCountResponse>

    @GET("api/follow/check/{followeeId}")
    suspend fun isFollowing(@Path("followeeId") followeeId: Long): Response<Map<String, Boolean>>
}
