package com.danceflow.app.data.dto

// 收藏相关 DTO
data class FavoriteItem(
    val id: Long,
    val type: String, // post 或 video
    val postId: Long?,
    val videoId: Long?,
    val title: String?,
    val coverUrl: String?,
    val createdAt: String
)

data class FavoriteListResponse(
    val favorites: List<FavoriteItem>,
    val total: Int
)
