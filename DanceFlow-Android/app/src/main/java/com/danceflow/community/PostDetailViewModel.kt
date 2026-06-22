package com.danceflow.app.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.dto.CommentResponse
import com.danceflow.app.data.dto.CreateCommentRequest
import com.danceflow.app.data.dto.PostResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DetailState {
    object Loading : DetailState()
    data class Success(
        val post: PostResponse,
        val comments: List<CommentResponse>
    ) : DetailState()
    data class Error(val message: String) : DetailState()
}

class PostDetailViewModel : ViewModel() {

    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state: StateFlow<DetailState> = _state.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    fun load(postId: Long) {
        viewModelScope.launch {
            _state.value = DetailState.Loading
            try {
                val postResp = RetrofitInstance.getCommunityApi().getPost(postId)
                val commentsResp = RetrofitInstance.getCommunityApi().getComments(postId)
                if (postResp.isSuccessful && postResp.body() != null) {
                    val comments = if (commentsResp.isSuccessful) commentsResp.body() ?: emptyList() else emptyList()
                    _state.value = DetailState.Success(postResp.body()!!, comments)
                } else {
                    _state.value = DetailState.Error("Post not found")
                }
            } catch (e: Exception) {
                Log.e("PostDetailVM", "load error", e)
                _state.value = DetailState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun likePost(postId: Long) {
        viewModelScope.launch {
            try {
                val current = _state.value
                if (current is DetailState.Success) {
                    val wasLiked = current.post.isLiked
                    val api = RetrofitInstance.getCommunityApi()
                    val resp = if (wasLiked) api.unlikePost(postId) else api.likePost(postId)
                    if (resp.isSuccessful) {
                        val updatedPost = current.post.copy(
                            isLiked = !wasLiked,
                            likesCount = if (wasLiked) current.post.likesCount - 1 else current.post.likesCount + 1
                        )
                        _state.value = current.copy(post = updatedPost)
                    }
                }
            } catch (e: Exception) {
                Log.e("PostDetailVM", "like error", e)
            }
        }
    }

    fun updateCommentText(text: String) {
        _commentText.value = text
    }

    fun sendComment(postId: Long) {
        val text = _commentText.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            try {
                val resp = RetrofitInstance.getCommunityApi().createComment(postId, CreateCommentRequest(text))
                if (resp.isSuccessful && resp.body() != null) {
                    val current = _state.value
                    if (current is DetailState.Success) {
                        _state.value = current.copy(comments = current.comments + resp.body()!!)
                    }
                    _commentText.value = ""
                }
            } catch (e: Exception) {
                Log.e("PostDetailVM", "comment error", e)
            }
        }
    }
}
