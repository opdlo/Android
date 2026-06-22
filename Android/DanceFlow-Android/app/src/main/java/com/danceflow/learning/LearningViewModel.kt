package com.danceflow.app.learning

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LearningState {
    object Idle : LearningState()
    object Loading : LearningState()
    data class Success(val videos: List<VideoResource>) : LearningState()
    data class Error(val message: String) : LearningState()
}

sealed class CategoriesState {
    object Idle : CategoriesState()
    object Loading : CategoriesState()
    data class Success(val categories: List<VideoCategory>) : CategoriesState()
    data class Error(val message: String) : CategoriesState()
}

class LearningViewModel : ViewModel() {

    private val _learningState = MutableStateFlow<LearningState>(LearningState.Idle)
    val learningState: StateFlow<LearningState> = _learningState.asStateFlow()

    private val _categoriesState = MutableStateFlow<CategoriesState>(CategoriesState.Idle)
    val categoriesState: StateFlow<CategoriesState> = _categoriesState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private var allVideos = listOf<VideoResource>()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = CategoriesState.Loading
            try {
                val response = RetrofitInstance.getLearningApi().getCategories()
                if (response.isSuccessful && response.body() != null) {
                    _categoriesState.value = CategoriesState.Success(response.body()!!)
                } else {
                    _categoriesState.value = CategoriesState.Error("加载分类失败")
                }
            } catch (e: Exception) {
                Log.e("LearningViewModel", "Load categories error", e)
                _categoriesState.value = CategoriesState.Error("网络错误")
            }
        }
    }

    fun loadVideos(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 0
            allVideos = emptyList()
        }

        viewModelScope.launch {
            _learningState.value = LearningState.Loading
            try {
                val response = RetrofitInstance.getLearningApi().getVideos(
                    page = currentPage,
                    size = pageSize,
                    categoryId = _selectedCategory.value,
                    search = _searchQuery.value.takeIf { it.isNotEmpty() }
                )

                if (response.isSuccessful && response.body() != null) {
                    val newVideos = response.body()!!.videos
                    if (refresh) {
                        allVideos = newVideos
                    } else {
                        allVideos = allVideos + newVideos
                    }
                    _learningState.value = LearningState.Success(allVideos)
                    currentPage++
                } else {
                    _learningState.value = LearningState.Error("加载失败")
                }
            } catch (e: Exception) {
                Log.e("LearningViewModel", "Load videos error", e)
                _learningState.value = LearningState.Error("网络错误")
            }
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategory.value = categoryId
        loadVideos(refresh = true)
    }

    fun search(query: String) {
        _searchQuery.value = query
        loadVideos(refresh = true)
    }

    fun favoriteVideo(videoId: Long) {
        viewModelScope.launch {
            try {
                val video = allVideos.find { it.id == videoId }
                val api = RetrofitInstance.getLearningApi()

                val response = if (video?.isFavorited == true) {
                    api.unfavoriteVideo(videoId)
                } else {
                    api.favoriteVideo(videoId)
                }

                if (response.isSuccessful) {
                    allVideos = allVideos.map { v ->
                        if (v.id == videoId) {
                            v.copy(isFavorited = !v.isFavorited)
                        } else v
                    }
                    _learningState.value = LearningState.Success(allVideos)
                }
            } catch (e: Exception) {
                Log.e("LearningViewModel", "Favorite error", e)
            }
        }
    }
}
