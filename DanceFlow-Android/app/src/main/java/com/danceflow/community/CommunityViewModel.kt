package com.danceflow.app.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

sealed class CommunityState {
    object Idle : CommunityState()
    object Loading : CommunityState()
    data class Success(val posts: List<PostResponse>) : CommunityState()
    data class Error(val message: String) : CommunityState()
}

sealed class PostActionState {
    object Idle : PostActionState()
    object Loading : PostActionState()
    object Success : PostActionState()
    data class Error(val message: String) : PostActionState()
}

class CommunityViewModel : ViewModel() {

    private val _communityState = MutableStateFlow<CommunityState>(CommunityState.Idle)
    val communityState: StateFlow<CommunityState> = _communityState.asStateFlow()

    private val _actionState = MutableStateFlow<PostActionState>(PostActionState.Idle)
    val actionState: StateFlow<PostActionState> = _actionState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private var allPosts = listOf<PostResponse>()

    fun loadPosts(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 0
            allPosts = emptyList()
        }

        viewModelScope.launch {
            _communityState.value = CommunityState.Loading
            try {
                val response = RetrofitInstance.getCommunityApi().getPosts(
                    page = currentPage,
                    size = pageSize,
                    search = _searchQuery.value.takeIf { it.isNotEmpty() }
                )

                if (response.isSuccessful && response.body() != null) {
                    val newPosts = response.body()!!.posts
                    if (refresh) {
                        allPosts = newPosts
                    } else {
                        allPosts = allPosts + newPosts
                    }
                    _communityState.value = CommunityState.Success(allPosts)
                    currentPage++
                } else {
                    _communityState.value = CommunityState.Error("加载失败")
                }
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Load posts error", e)
                _communityState.value = CommunityState.Error("网络错误: ${e.message}")
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        loadPosts(refresh = true)
    }

    fun likePost(postId: Long) {
        viewModelScope.launch {
            _actionState.value = PostActionState.Loading
            try {
                val post = allPosts.find { it.id == postId }
                val api = RetrofitInstance.getCommunityApi()

                val response = if (post?.isLiked == true) {
                    api.unlikePost(postId)
                } else {
                    api.likePost(postId)
                }

                if (response.isSuccessful) {
                    // 更新本地状态
                    allPosts = allPosts.map { p ->
                        if (p.id == postId) {
                            p.copy(
                                isLiked = !p.isLiked,
                                likesCount = if (p.isLiked) p.likesCount - 1 else p.likesCount + 1
                            )
                        } else p
                    }
                    _communityState.value = CommunityState.Success(allPosts)
                    _actionState.value = PostActionState.Success
                } else {
                    _actionState.value = PostActionState.Error("操作失败")
                }
            } catch (e: Exception) {
                _actionState.value = PostActionState.Error("网络错误")
            }
        }
    }

    fun favoritePost(postId: Long) {
        viewModelScope.launch {
            _actionState.value = PostActionState.Loading
            try {
                val post = allPosts.find { it.id == postId }
                val api = RetrofitInstance.getCommunityApi()

                val response = if (post?.isFavorited == true) {
                    api.unfavoritePost(postId)
                } else {
                    api.favoritePost(postId)
                }

                if (response.isSuccessful) {
                    // 更新本地状态
                    allPosts = allPosts.map { p ->
                        if (p.id == postId) {
                            p.copy(isFavorited = !p.isFavorited)
                        } else p
                    }
                    _communityState.value = CommunityState.Success(allPosts)
                    _actionState.value = PostActionState.Success
                } else {
                    _actionState.value = PostActionState.Error("操作失败")
                }
            } catch (e: Exception) {
                _actionState.value = PostActionState.Error("网络错误")
            }
        }
    }

    fun createPost(content: String, images: List<String>?, videoUrl: String?) {
        viewModelScope.launch {
            _actionState.value = PostActionState.Loading
            try {
                val response = RetrofitInstance.getCommunityApi().createPost(
                    CreatePostRequest(content, images, videoUrl)
                )

                if (response.isSuccessful && response.body() != null) {
                    _actionState.value = PostActionState.Success
                    loadPosts(refresh = true)
                } else {
                    _actionState.value = PostActionState.Error("发布失败")
                }
            } catch (e: Exception) {
                _actionState.value = PostActionState.Error("网络错误")
            }
        }
    }

    fun createPostWithFiles(content: String, fileParts: List<MultipartBody.Part>) {
        viewModelScope.launch {
            _actionState.value = PostActionState.Loading
            try {
                // 如果没有文件，直接创建纯文本帖子
                if (fileParts.isEmpty()) {
                    createPost(content, null, null)
                    return@launch
                }

                // 1. 上传所有文件
                val uploadResponse = RetrofitInstance.getUploadApi().uploadFiles(fileParts)
                if (!uploadResponse.isSuccessful || uploadResponse.body() == null) {
                    _actionState.value = PostActionState.Error("文件上传失败")
                    return@launch
                }

                val allUrls = uploadResponse.body()!!.urls

                // 2. 区分图片和视频
                val imageUrls = allUrls.filter { url ->
                    url.endsWith(".jpg", ignoreCase = true) ||
                    url.endsWith(".jpeg", ignoreCase = true) ||
                    url.endsWith(".png", ignoreCase = true) ||
                    url.endsWith(".gif", ignoreCase = true) ||
                    url.endsWith(".webp", ignoreCase = true)
                }
                val videoUrl = allUrls.firstOrNull { url ->
                    url.endsWith(".mp4", ignoreCase = true) ||
                    url.endsWith(".mov", ignoreCase = true) ||
                    url.endsWith(".avi", ignoreCase = true) ||
                    url.endsWith(".mkv", ignoreCase = true)
                }

                // 3. 创建帖子
                createPost(
                    content = content,
                    images = imageUrls.ifEmpty { null },
                    videoUrl = videoUrl
                )
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Create post with files error", e)
                _actionState.value = PostActionState.Error("发布失败: ${e.message}")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = PostActionState.Idle
    }
    
    fun addComment(postId: Long, content: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.getCommunityApi().createComment(
                    postId = postId,
                    request = CreateCommentRequest(content = content)
                )
                if (response.isSuccessful) {
                    allPosts = allPosts.map { p ->
                        if (p.id == postId) {
                            p.copy(commentsCount = p.commentsCount + 1)
                        } else p
                    }
                    _communityState.value = CommunityState.Success(allPosts)
                }
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Add comment error", e)
            }
        }
    }
}
