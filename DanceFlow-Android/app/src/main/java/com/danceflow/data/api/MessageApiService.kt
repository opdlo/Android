package com.danceflow.app.data.api

import com.danceflow.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MessageApiService {

    @POST("api/messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageResponse>

    @GET("api/messages/conversations")
    suspend fun getConversations(): Response<List<ConversationResponse>>

    @GET("api/messages/{userId}")
    suspend fun getConversation(@Path("userId") userId: Long): Response<List<MessageResponse>>

    @PUT("api/messages/{id}/read")
    suspend fun markAsRead(@Path("id") messageId: Long): Response<Unit>

    @PUT("api/messages/read-all/{senderId}")
    suspend fun markAllAsRead(@Path("senderId") senderId: Long): Response<Unit>

    @GET("api/messages/unread/count")
    suspend fun getUnreadCount(): Response<Map<String, Long>>
}
