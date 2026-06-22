package com.danceflow.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.danceflow.app.analysis.AnalysisState
import com.danceflow.app.analysis.AnalysisViewModel
import com.danceflow.app.analysis.UploadState
import com.danceflow.app.data.dto.PracticeHistory
import com.danceflow.app.ui.components.*
import com.danceflow.app.ui.theme.CardBackground
import com.danceflow.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    navController: NavController,
    viewModel: AnalysisViewModel = viewModel(),
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val tabs = listOf("历史记录", "新练习")

    val historyState by viewModel.historyState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 标签页
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0F3460),
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        when (selectedTab) {
            0 -> HistoryContent(
                historyState = historyState,
                onRetry = { viewModel.loadHistory() },
                onHistoryClick = { practiceId ->
                    navController.navigate("analysis_result/$practiceId")
                }
            )
            1 -> NewPracticeContent(
                uploadState = uploadState,
                onUpload = { uri, style ->
                    viewModel.uploadVideo(navController.context, uri, style)
                },
                onResetState = { viewModel.resetUploadState() }
            )
        }
    }
}

@Composable
private fun HistoryContent(
    historyState: AnalysisState,
    onRetry: () -> Unit,
    onHistoryClick: (Long) -> Unit
) {
    when (val state = historyState) {
        is AnalysisState.Loading -> LoadingState()
        is AnalysisState.Success -> {
            if (state.history.isEmpty()) {
                EmptyState("还没有练习记录")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.history) { practice ->
                        PracticeHistoryCard(
                            practice = practice,
                            onClick = { onHistoryClick(practice.id) }
                        )
                    }
                }
            }
        }
        is AnalysisState.Error -> ErrorState(state.message, onRetry)
        else -> LoadingState()
    }
}

@Composable
private fun PracticeHistoryCard(
    practice: PracticeHistory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 视频预览
            Box(
                modifier = Modifier
                    .size(80.dp, 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2D2D44))
            ) {
                AsyncImage(
                    model = practice.videoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (practice.analysisStatus) {
                            "pending" -> "等待分析"
                            "analyzing" -> "分析中"
                            "completed" -> "已完成"
                            "failed" -> "分析失败"
                            else -> "未知状态"
                        },
                        fontSize = 14.sp,
                        color = when (practice.analysisStatus) {
                            "completed" -> Color(0xFF4CAF50)
                            "analyzing" -> Color(0xFFFFA726)
                            "failed" -> Color(0xFFF44336)
                            else -> Color.Gray
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTime(practice.createdAt),
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                if (practice.score != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "评分: ${practice.score}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun NewPracticeContent(
    uploadState: UploadState,
    onUpload: (Uri, String?) -> Unit,
    onResetState: () -> Unit
) {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var danceStyle by remember { mutableStateOf("") }
    var showVideoPicker by remember { mutableStateOf(false) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
    }

    LaunchedEffect(uploadState) {
        if (uploadState is UploadState.Success) {
            selectedVideoUri = null
            danceStyle = ""
            onResetState()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uploadState) {
            is UploadState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("正在上传视频...", color = TextSecondary)
                }
            }
            is UploadState.Error -> {
                ErrorState(
                    message = (uploadState as UploadState.Error).message,
                    onRetry = { selectedVideoUri?.let { onUpload(it, danceStyle) } }
                )
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    // 视频选择区
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .clickable { videoPickerLauncher.launch("video/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedVideoUri != null) {
                            // TODO: 显示视频预览
                            Text("视频已选择", color = MaterialTheme.colorScheme.primary)
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoCall,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("点击选择视频", color = TextSecondary)
                            }
                        }
                    }

                    // 舞蹈风格输入
                    OutlinedTextField(
                        value = danceStyle,
                        onValueChange = { danceStyle = it },
                        label = { Text("舞蹈风格（可选）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultOutlinedTextFieldColors()
                    )

                    // 提示信息
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                        text = "• 上传后将自动进行动作分析\n• 分析结果将显示在历史记录中\n• 支持 MP4 格式视频",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                    // 上传按钮
                    Button(
                        onClick = {
                            selectedVideoUri?.let { uri ->
                                onUpload(uri, danceStyle.ifBlank { null })
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = selectedVideoUri != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始分析", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun defaultOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    focusedContainerColor = CardBackground,
    unfocusedContainerColor = CardBackground,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)
