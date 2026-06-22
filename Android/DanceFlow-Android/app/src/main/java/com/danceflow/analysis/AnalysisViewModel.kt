package com.danceflow.app.analysis

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(val history: List<PracticeHistory>) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val practice: PracticeHistory) : UploadState()
    data class Error(val message: String) : UploadState()
}

class AnalysisViewModel : ViewModel() {

    private val _historyState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val historyState: StateFlow<AnalysisState> = _historyState.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = AnalysisState.Loading
            try {
                val response = RetrofitInstance.getAnalysisApi().getPracticeHistory()
                if (response.isSuccessful && response.body() != null) {
                    _historyState.value = AnalysisState.Success(
                        response.body()!!.practices.sortedByDescending { it.id }
                    )
                } else {
                    _historyState.value = AnalysisState.Error("加载历史失败")
                }
            } catch (e: Exception) {
                Log.e("AnalysisViewModel", "Load history error", e)
                _historyState.value = AnalysisState.Error("网络错误")
            }
        }
    }

    fun uploadVideo(context: Context, uri: Uri, danceStyle: String?) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            try {
                // 将 Uri 转换为 File
                val file = uriToFile(context, uri)
                if (file == null) {
                    _uploadState.value = UploadState.Error("文件处理失败")
                    return@launch
                }

                val requestBody = RequestBody.create(
                    "video/*".toMediaType(),
                    file
                )
                val videoPart = MultipartBody.Part.createFormData("video", file.name, requestBody)

                val stylePart = if (danceStyle != null) {
                    RequestBody.create("text/plain".toMediaType(), danceStyle)
                        .let { MultipartBody.Part.createFormData("danceStyle", null, it) }
                } else {
                    null
                }

                val response = RetrofitInstance.getAnalysisApi()
                    .createPractice(videoPart, stylePart)

                if (response.isSuccessful && response.body() != null) {
                    _uploadState.value = UploadState.Success(response.body()!!)
                    loadHistory() // 刷新历史列表
                } else {
                    _uploadState.value = UploadState.Error("上传失败")
                }
            } catch (e: Exception) {
                Log.e("AnalysisViewModel", "Upload error", e)
                _uploadState.value = UploadState.Error("网络错误: ${e.message}")
            }
        }
    }

    fun getAnalysisResult(practiceId: Long): AnalysisResult? {
        // 暂时返回 null，实际应该从 API 获取
        return null
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.mp4")
            val outputStream = FileOutputStream(tempFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("AnalysisViewModel", "URI to file error", e)
            null
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}
