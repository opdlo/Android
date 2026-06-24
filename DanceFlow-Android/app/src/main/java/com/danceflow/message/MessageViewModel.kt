package com.danceflow.app.message

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.dto.ConversationResponse
import com.danceflow.app.data.dto.MessageResponse
import com.danceflow.app.data.dto.SendMessageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MessageListState {
    object Idle : MessageListState()
    object Loading : MessageListState()
    data class Success(val messages: List<MessageResponse>) : MessageListState()
    data class Error(val message: String) : MessageListState()
}

sealed class ConversationListState {
    object Idle : ConversationListState()
    object Loading : ConversationListState()
    data class Success(val conversations: List<ConversationResponse>) : ConversationListState()
    data class Error(val message: String) : ConversationListState()
}

class MessageViewModel : ViewModel() {
    private val _messagesState = MutableStateFlow<MessageListState>(MessageListState.Idle)
    val messagesState: StateFlow<MessageListState> = _messagesState.asStateFlow()

    private val _conversationsState = MutableStateFlow<ConversationListState>(ConversationListState.Idle)
    val conversationsState: StateFlow<ConversationListState> = _conversationsState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    fun loadConversations() {
        viewModelScope.launch {
            _conversationsState.value = ConversationListState.Loading
            try {
                val resp = RetrofitInstance.getMessageApi().getConversations()
                if (resp.isSuccessful && resp.body() != null)
                    _conversationsState.value = ConversationListState.Success(resp.body()!!)
                else
                    _conversationsState.value = ConversationListState.Error("加载失败")
            } catch (e: Exception) {
                _conversationsState.value = ConversationListState.Error(e.message ?: "网络错误")
            }
        }
    }

    fun loadConversation(userId: Long) {
        viewModelScope.launch {
            _messagesState.value = MessageListState.Loading
            try {
                val resp = RetrofitInstance.getMessageApi().getConversation(userId)
                if (resp.isSuccessful && resp.body() != null)
                    _messagesState.value = MessageListState.Success(resp.body()!!)
                else
                    _messagesState.value = MessageListState.Error("加载失败")
            } catch (e: Exception) {
                _messagesState.value = MessageListState.Error(e.message ?: "网络错误")
            }
        }
    }

    fun sendMessage(receiverId: Long, content: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                RetrofitInstance.getMessageApi().sendMessage(SendMessageRequest(receiverId, content))
                loadConversation(receiverId)
            } catch (e: Exception) {
                Log.e("MessageViewModel", "send error", e)
            } finally {
                onComplete()
            }
        }
    }

    fun sharePost(receiverId: Long, postId: Long, content: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val req = com.danceflow.app.data.dto.SendMessageRequest(
                    receiverId = receiverId,
                    content = content,
                    messageType = "post_share",
                    referenceType = "post",
                    referenceId = postId
                )
                RetrofitInstance.getMessageApi().sendMessage(req)
                onComplete()
            } catch (e: Exception) {
                android.util.Log.e("MessageViewModel", "sharePost error", e)
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val resp = RetrofitInstance.getMessageApi().getUnreadCount()
                if (resp.isSuccessful && resp.body() != null)
                    _unreadCount.value = resp.body()?.get("count") ?: 0L
            } catch (e: Exception) {
                Log.e("MessageViewModel", "unread error", e)
            }
        }
    }

    fun markAllAsRead(senderId: Long) {
        viewModelScope.launch {
            try {
                RetrofitInstance.getMessageApi().markAllAsRead(senderId)
            } catch (e: Exception) {
                Log.e("MessageViewModel", "markAllAsRead error", e)
            }
        }
    }

}