package com.danceflow.app.data.dto

data class FollowResponse(
    val id: Long,
    val userId: Long,
    val username: String?,
    val avatarUrl: String?,
    val createdAt: String?,
    val isMutual: Boolean = false
)

data class MessageResponse(
    val id: Long,
    val senderId: Long,
    val senderUsername: String?,
    val senderAvatarUrl: String?,
    val receiverId: Long,
    val receiverUsername: String?,
    val receiverAvatarUrl: String?,
    val content: String,
    val isRead: Boolean = false,
    val messageType: String? = "text",
    val mediaUrl: String? = null,
    val referenceType: String? = null,
    val referenceId: Long? = null,
    val referenceContent: String? = null,
    val createdAt: String?
)

data class ConversationResponse(
    val userId: Long,
    val username: String?,
    val avatarUrl: String?,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val unreadCount: Long = 0,
    val lastMessageType: String? = "text",
    val lastMessageMediaUrl: String? = null
)

data class SendMessageRequest(
    val receiverId: Long,
    val content: String,
    val messageType: String? = "text",
    val mediaUrl: String? = null,
    val referenceType: String? = null,
    val referenceId: Long? = null
)

data class FollowCountResponse(
    val followingCount: Long,
    val followersCount: Long
)
