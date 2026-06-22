package com.danceflow.app.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.SessionManager
import com.danceflow.app.data.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val user: UserProfileResponse) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class FavoritesState {
    object Idle : FavoritesState()
    object Loading : FavoritesState()
    data class Success(val favorites: List<FavoriteItem>) : FavoritesState()
    data class Error(val message: String) : FavoritesState()
}

class ProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _favoritesState = MutableStateFlow<FavoritesState>(FavoritesState.Idle)
    val favoritesState: StateFlow<FavoritesState> = _favoritesState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = RetrofitInstance.getAuthApi().getCurrentUser()
                if (response.isSuccessful && response.body() != null) {
                    _profileState.value = ProfileState.Success(response.body()!!)

                    // 更新 SessionManager 中的用户信息
                    val user = response.body()!!
                    SessionManager.saveUserInfo(
                        user.id.toString(),
                        user.username
                    )
                } else {
                    _profileState.value = ProfileState.Error("加载失败")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Load profile error", e)
                _profileState.value = ProfileState.Error("网络错误")
            }
        }
    }

    fun updateProfile(gender: String?, birthday: String?, signature: String?, avatarUrl: String?) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = RetrofitInstance.getAuthApi().updateProfile(
                    UpdateProfileRequest(gender, birthday, signature, avatarUrl)
                )
                if (response.isSuccessful && response.body() != null) {
                    _profileState.value = ProfileState.Success(response.body()!!)
                } else {
                    _profileState.value = ProfileState.Error("更新失败")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Update profile error", e)
                _profileState.value = ProfileState.Error("网络错误")
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _favoritesState.value = FavoritesState.Loading
            try {
                val response = RetrofitInstance.getProfileApi().getFavorites()
                if (response.isSuccessful && response.body() != null) {
                    _favoritesState.value = FavoritesState.Success(response.body()!!.favorites)
                } else {
                    _favoritesState.value = FavoritesState.Error("加载失败")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Load favorites error", e)
                _favoritesState.value = FavoritesState.Error("网络错误")
            }
        }
    }

    fun uploadAvatar(part: MultipartBody.Part, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.getProfileApi().uploadAvatar(part)
                if (!response.isSuccessful) {
                    Log.e("ProfileViewModel", "Upload avatar failed")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Upload avatar error", e)
            } finally {
                onComplete()
            }
        }
    }

    suspend fun logout() {
        SessionManager.clearSession()
    }
}
