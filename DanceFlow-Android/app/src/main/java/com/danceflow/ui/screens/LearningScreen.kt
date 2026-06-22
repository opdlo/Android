package com.danceflow.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.danceflow.app.learning.CategoriesState
import com.danceflow.app.learning.LearningState
import com.danceflow.app.learning.LearningViewModel
import com.danceflow.app.data.dto.VideoCategory
import com.danceflow.app.data.dto.VideoResource
import com.danceflow.app.ui.components.*
import com.danceflow.app.ui.theme.CardBackground
import com.danceflow.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    navController: NavController,
    viewModel: LearningViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }

    val categoriesState by viewModel.categoriesState.collectAsState()
    val learningState by viewModel.learningState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // 监听搜索变化
    LaunchedEffect(searchQuery) {
        if (searchQuery != viewModel.searchQuery.value) {
            viewModel.search(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 0.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "搜索舞蹈视频..."
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 分类标签
            when (val catState = categoriesState) {
                is CategoriesState.Success -> {
                    CategoryFilter(
                        categories = catState.categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                }
                else -> {}
            }

            // 视频列表
            when (val state = learningState) {
                is LearningState.Loading -> {
                    LoadingState()
                }
                is LearningState.Success -> {
                    VideoList(
                        videos = state.videos,
                        onVideoClick = { videoId ->
                            navController.navigate("video_detail/$videoId")
                        },
                        onFavorite = { viewModel.favoriteVideo(it) }
                    )
                }
                is LearningState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadVideos(refresh = true) }
                    )
                }
                else -> {
                    // 初始加载
                    LaunchedEffect(Unit) {
                        viewModel.loadVideos(refresh = true)
                    }
                    LoadingState()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilter(
    categories: List<VideoCategory>,
    selectedCategory: Long?,
    onCategorySelected: (Long?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("全部") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category.id,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun VideoList(
    videos: List<VideoResource>,
    onVideoClick: (Long) -> Unit,
    onFavorite: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(videos) { video ->
            VideoCard(
                video = video,
                onClick = { onVideoClick(video.id) },
                onFavorite = { onFavorite(video.id) }
            )
        }
    }
}

@Composable
private fun VideoCard(
    video: VideoResource,
    onClick: () -> Unit,
    onFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // 视频封面
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = video.coverUrl,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // 时长标签
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = formatDuration(video.duration),
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // 标题
                Text(
                    text = video.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 分类和观看数
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.category.name,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${video.views}次观看",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onFavorite,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (video.isFavorited) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                                contentDescription = "收藏",
                                tint = if (video.isFavorited) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    TextSecondary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}
