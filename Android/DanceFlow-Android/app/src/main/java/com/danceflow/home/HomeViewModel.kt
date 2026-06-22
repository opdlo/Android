package com.danceflow.app.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.dto.PracticeHistory
import com.danceflow.app.data.dto.UserProfileResponse
import com.danceflow.app.data.dto.VideoResource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
data class HomeUiState(
    val profile: UserProfileResponse? = null,
    val totalPractices: Int = 0,
    val recentPractices: List<PracticeHistory> = emptyList(),
    val recommendedVideos: List<VideoResource> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val profileDeferred = async { loadProfile() }
                val practicesDeferred = async { loadPractices() }
                val videosDeferred = async { loadVideos() }

                val profile = profileDeferred.await()
                val (totalPractices, recentPractices) = practicesDeferred.await()
                val recommendedVideos = videosDeferred.await()

                _uiState.value = HomeUiState(
                    profile = profile,
                    totalPractices = totalPractices,
                    recentPractices = recentPractices,
                    recommendedVideos = recommendedVideos,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Load error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadProfile(): UserProfileResponse? {
        return try {
            val response = RetrofitInstance.getAuthApi().getCurrentUser()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Profile load error", e)
            null
        }
    }

    private suspend fun loadPractices(): Pair<Int, List<PracticeHistory>> {
        return try {
            val response = RetrofitInstance.getAnalysisApi().getPracticeHistory()
            if (response.isSuccessful && response.body() != null) {
                val all = response.body()!!.practices.sortedByDescending { it.id }
                Pair(all.size, all.take(5))
            } else {
                Pair(0, emptyList())
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Practices load error", e)
            Pair(0, emptyList())
        }
    }

    private suspend fun loadVideos(): List<VideoResource> {
        return try {
            val response = RetrofitInstance.getLearningApi().getVideos(page = 0, size = 10)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.videos
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Videos load error", e)
            emptyList()
        }
    }
}

