package com.danceflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.danceflow.app.home.HomeViewModel
import com.danceflow.app.home.HomeUiState
import com.danceflow.app.data.dto.PracticeHistory
import com.danceflow.app.data.dto.VideoResource
import com.danceflow.app.ui.components.*
import com.danceflow.app.ui.theme.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> LoadingState()
        state.error != null -> ErrorState(
            message = state.error!!,
            onRetry = { viewModel.loadAll() }
        )
        else -> HomeContent(state = state, navController = navController)
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Region 1: Greeting
        item {
            GreetingHeader(profile = state.profile)
        }

        // Region 2: Stat card
        item {
            StatCard(totalPractices = state.totalPractices)
        }

        // Region 3: Recent practices
        if (state.recentPractices.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recent Practices",
                    showViewAll = true,
                    onViewAll = { navController.navigate("analysis_list") }
                )
            }
            items(state.recentPractices) { practice ->
                PracticeRow(
                    practice = practice,
                    onClick = { navController.navigate("analysis_result/${practice.id}") }
                )
            }
        } else {
            item {
                EmptyPracticeHint(onStartPractice = { navController.navigate("analysis_list") })
            }
        }

        // Region 4: Recommended videos
        if (state.recommendedVideos.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recommended",
                    showViewAll = true,
                    onViewAll = {
                        navController.navigate("learning") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            item {
                VideoRow(
                    videos = state.recommendedVideos.take(10),
                    onVideoClick = { videoId ->
                        navController.navigate("video_detail/$videoId")
                    }
                )
            }
        }
    }
}

// --- Sub-components ---

@Composable
private fun GreetingHeader(profile: com.danceflow.app.data.dto.UserProfileResponse?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, ${profile?.username ?: ""}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Keep practicing",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
        AsyncImage(
            model = profile?.avatarUrl,
            contentDescription = "avatar",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = rememberVectorPainter(Icons.Default.AccountCircle),
            error = rememberVectorPainter(Icons.Default.AccountCircle)
        )
    }
}

@Composable
private fun StatCard(totalPractices: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().let { it /* use theme border */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "$totalPractices",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "practices",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    showViewAll: Boolean = false,
    onViewAll: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        if (showViewAll && onViewAll != null) {
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onViewAll) {
                Text(
                    text = "View all",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PracticeRow(
    practice: PracticeHistory,
    onClick: () -> Unit
) {
    val statusColor = when (practice.analysisStatus) {
        "completed" -> SecondaryColor
        "analyzing" -> AccentColor.copy(alpha = 0.7f)
        "failed" -> AccentColor
        else -> TextSecondary
    }
    val statusText = when (practice.analysisStatus) {
        "completed" -> "Completed"
        "analyzing" -> "Analyzing"
        "failed" -> "Failed"
        "pending" -> "Pending"
        else -> "Unknown"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                   color = TextPrimary
               )
                if (practice.danceStyle != null) {
                    Text(
                        text = practice.danceStyle!!,
                        fontSize = 12.sp,
                        color = SecondaryColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = com.danceflow.app.ui.components.formatTime(practice.createdAt),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            if (practice.score != null) {
                Text(
                    text = "${practice.score}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun VideoRow(
    videos: List<VideoResource>,
    onVideoClick: (Long) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(videos) { video ->
            VideoCard(
                video = video,
                onClick = { onVideoClick(video.id) }
            )
        }
    }
}

@Composable
private fun VideoCard(
    video: VideoResource,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Cover image container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color(0xFF2D2D44)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = video.coverUrl,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = video.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = video.category.name,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun EmptyPracticeHint(onStartPractice: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No practice records yet",
                fontSize = 15.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onStartPractice) {
                Text("Start practicing")
            }
        }
    }
}

