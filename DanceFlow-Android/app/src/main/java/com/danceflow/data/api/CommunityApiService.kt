package com.danceflow.app.data.api

import com.danceflow.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CommunityApiService {

    @GET("api/community/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null
    ): Response<PostListResponse>

    @GET("api/community/posts/{id}")
    suspend fun getPost(@Path("id") postId: Long): Response<PostResponse>

    @POST("api/community/posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<PostResponse>

    @POST("api/community/posts/{id}/like")
    suspend fun likePost(@Path("id") postId: Long): Response<Unit>

    @DELETE("api/community/posts/{id}/like")
    suspend fun unlikePost(@Path("id") postId: Long): Response<Unit>

    @POST("api/community/posts/{id}/favorite")
    suspend fun favoritePost(@Path("id") postId: Long): Response<Unit>

    @DELETE("api/community/posts/{id}/favorite")
    suspend fun unfavoritePost(@Path("id") postId: Long): Response<Unit>

    @GET("api/community/posts/{id}/comments")
    suspend fun getComments(@Path("id") postId: Long): Response<List<CommentResponse>>

    @POST("api/community/posts/{id}/comments")
    suspend fun createComment(
        @Path("id") postId: Long,
        @Body request: CreateCommentRequest
    ): Response<CommentResponse>

    @GET("api/community/posts/user/{userId}")
    suspend fun getUserPosts(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PostListResponse>
}

