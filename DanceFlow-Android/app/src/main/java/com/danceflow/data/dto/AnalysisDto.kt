package com.danceflow.app.data.dto

// 动作分析相关 DTO
data class PracticeHistory(
    val id: Long,
    val videoUrl: String,
    val score: Float?,
    val analysisStatus: String, // pending, analyzing, completed, failed
    val feedback: String?,
    val suggestions: List<String>?,
    val danceStyle: String?,
    val createdAt: String
)

data class CreatePracticeRequest(
    val videoUrl: String,
    val danceStyle: String?
)

data class AnalysisResult(
    val score: Float,
    val feedback: String,
    val suggestions: List<String>,
    val keyFrames: List<KeyFrameAnalysis>,
    val bodyMetrics: BodyMetrics?
)

data class KeyFrameAnalysis(
    val timestamp: Float,
    val score: Float,
    val issues: List<String>
)

data class BodyMetrics(
    val accuracy: Float,
    val rhythm: Float,
    val expression: Float
)

data class PracticeHistoryListResponse(
    val practices: List<PracticeHistory>,
    val total: Int,
    val danceStyle: String?
)

