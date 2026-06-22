package com.danceflow.app.data.dto

// 学习资源相关 DTO
data class VideoCategory(
    val id: Long,
    val name: String,
    val description: String?,
    val coverUrl: String?
)

data class VideoResource(
    val id: Long,
    val title: String,
    val description: String?,
    val coverUrl: String?,
    val videoUrl: String,
    val category: VideoCategory,
    val duration: Int, // 秒
    val views: Int,
    val likesCount: Int,
    val isFavorited: Boolean,
    val createdAt: String
)

data class VideoListResponse(
    val videos: List<VideoResource>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)
