package com.danceflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.danceflow.app.analysis.AnalysisViewModel
import com.danceflow.app.ui.theme.AccentColor
import com.danceflow.app.ui.theme.CardBackground
import com.danceflow.app.ui.theme.PrimaryColor
import com.danceflow.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    practiceId: Long,
    navController: NavController,
    viewModel: AnalysisViewModel = viewModel()
) {
    // 这里应该从 ViewModel 获取分析结果
    // 暂时使用模拟数据
    var score by remember { mutableStateOf(85f) }
    var feedback by remember { mutableStateOf("整体表现优秀！节奏感很好，动作标准度较高。") }
    var suggestions by remember { mutableStateOf(listOf("注意手臂伸展幅度", "加强腰部转动", "保持身体平衡")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分析结果") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 评分卡片
            ScoreCard(score = score)

            // 反馈卡片
            FeedbackCard(feedback = feedback)

            // 建议卡片
            SuggestionsCard(suggestions = suggestions)

            // 关键帧分析（预留接口）
            KeyFramesSection()

            // 身体指标（预留接口）
            BodyMetricsSection()
        }
    }
}

@Composable
private fun ScoreCard(score: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "综合评分",
                fontSize = 16.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 评分圆环
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = score / 100f,
                    modifier = Modifier.size(120.dp),
                    color = when {
                        score >= 80f -> Color(0xFF4CAF50)
                        score >= 60f -> Color(0xFFFFA726)
                        else -> Color(0xFFF44336)
                    },
                    strokeWidth = 8.dp,
                    trackColor = Color(0xFF2D2D44)
                )

                Text(
                    text = "${score.toInt()}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        score >= 80f -> Color(0xFF4CAF50)
                        score >= 60f -> Color(0xFFFFA726)
                        else -> Color(0xFFF44336)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    score >= 90f -> "优秀"
                    score >= 80f -> "良好"
                    score >= 60f -> "及格"
                    else -> "需要改进"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FeedbackCard(feedback: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "评价反馈",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = feedback,
                fontSize = 15.sp,
                color = TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun SuggestionsCard(suggestions: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "改进建议",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            suggestions.forEachIndexed { index, suggestion ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(50f))
                            .background(PrimaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = suggestion,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (index < suggestions.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun KeyFramesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "关键帧分析",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "此功能需要配合模型分析接口，将在后续版本中实现。",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun BodyMetricsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "身体指标分析",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "此功能需要配合模型分析接口，将在后续版本中实现。",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}
