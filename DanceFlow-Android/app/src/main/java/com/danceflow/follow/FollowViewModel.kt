package com.danceflow.app.follow

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.dto.FollowResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FollowListState {
    object Idle : FollowListState()
    object Loading : FollowListState()
    data class Success(val list: List<FollowResponse>) : FollowListState()
    data class Error(val message: String) : FollowListState()
}

class FollowViewModel : ViewModel() {
    private val _followersState = MutableStateFlow<FollowListState>(FollowListState.Idle)
    val followersState: StateFlow<FollowListState> = _followersState.asStateFlow()

    private val _followingState = MutableStateFlow<FollowListState>(FollowListState.Idle)
    val followingState: StateFlow<FollowListState> = _followingState.asStateFlow()

    fun loadFollowers(userId: Long) {
        viewModelScope.launch {
            _followersState.value = FollowListState.Loading
            try {
                val resp = RetrofitInstance.getFollowApi().getFollowers(userId)
                if (resp.isSuccessful && resp.body() != null)
                    _followersState.value = FollowListState.Success(resp.body()!!)
                else
                    _followersState.value = FollowListState.Error("加载失败")
            } catch (e: Exception) {
                _followersState.value = FollowListState.Error(e.message ?: "网络错误")
            }
        }
    }

    fun loadFollowing(userId: Long) {
        viewModelScope.launch {
            _followingState.value = FollowListState.Loading
            try {
                val resp = RetrofitInstance.getFollowApi().getFollowing(userId)
                if (resp.isSuccessful && resp.body() != null)
                    _followingState.value = FollowListState.Success(resp.body()!!)
                else
                    _followingState.value = FollowListState.Error("加载失败")
            } catch (e: Exception) {
                _followingState.value = FollowListState.Error(e.message ?: "网络错误")
            }
        }
    }

    fun toggleFollow(userId: Long, currentlyFollowing: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                if (currentlyFollowing)
                    RetrofitInstance.getFollowApi().unfollow(userId)
                else
                    RetrofitInstance.getFollowApi().follow(userId)
                loadFollowing(userId)
            } catch (e: Exception) {
                Log.e("FollowViewModel", "toggle error", e)
            } finally {
                onComplete()
            }
        }
    }
}
