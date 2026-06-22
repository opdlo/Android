package com.danceflow.app.data.dto

import com.google.gson.annotations.SerializedName

// 社区相关 DTO
data class PostResponse(
    val id: Long,
    val author: AuthorInfo,
    val content: String,
    val images: List<String>?,
    val videoUrl: String?,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean,
    val isFavorited: Boolean,
    val createdAt: String
)

data class AuthorInfo(
    val id: Long,
    val username: String,
    val avatarUrl: String?
)

data class CreatePostRequest(
    val content: String,
    val images: List<String>?,
    val videoUrl: String?
)

data class CommentResponse(
    val id: Long,
    val postId: Long,
    val author: AuthorInfo,
    val content: String,
    val createdAt: String
)

data class CreateCommentRequest(
    val content: String
)

data class PostListResponse(
    val posts: List<PostResponse>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)
