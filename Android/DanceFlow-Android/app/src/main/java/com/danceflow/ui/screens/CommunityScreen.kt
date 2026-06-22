package com.danceflow.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.danceflow.app.community.CommunityState
import com.danceflow.app.community.CommunityViewModel
import com.danceflow.app.community.PostActionState
import com.danceflow.app.data.dto.PostResponse
import com.danceflow.app.ui.components.*
import com.danceflow.app.ui.theme.CardBackground
import com.danceflow.app.ui.theme.CardBackgroundLight
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    navController: NavController,
    viewModel: CommunityViewModel = viewModel()
) {
    var showCreatePost by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val communityState by viewModel.communityState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPosts(refresh = true)
    }

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
                    placeholder = "搜索帖子..."
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePost = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "发布",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = communityState) {
                is CommunityState.Loading -> {
                    if (state is CommunityState.Success && state.posts.isEmpty()) {
                        LoadingState()
                    } else {
                        PostsList(
                            posts = (state as? CommunityState.Success)?.posts ?: emptyList(),
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                }
                is CommunityState.Success -> {
                    PostsList(
                        posts = state.posts,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
                is CommunityState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadPosts(refresh = true) }
                    )
                }
                else -> {}
            }
        }
    }

    if (showCreatePost) {
        CreatePostDialog(
            onDismiss = { showCreatePost = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun PostsList(
    posts: List<PostResponse>,
    viewModel: CommunityViewModel,
    navController: NavController
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts) { post ->
            PostCard(
                post = post,
                onLike = { viewModel.likePost(post.id) },
                onFavorite = { viewModel.favoritePost(post.id) },
                onClick = {navController.navigate("post_detail/${post.id}") },
                onComment = { text -> viewModel.addComment(post.id, text) }
            )
        }
    }
}

@Composable
private fun PostCard(
    post: PostResponse,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onClick: () -> Unit,
    onComment: (String) -> Unit = {},
) {
    val showCommentInput = remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 用户信息
            UserInfoRow(
                author = post.author,
                time = post.createdAt
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 内容
            Text(
                text = post.content,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )

            // 图片展示
            if (!post.images.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(post.images.take(3)) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // 视频展示
            if (post.videoUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = post.videoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "视频",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 交互按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                InteractionButton(
                    icon = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    count = post.likesCount,
                    isSelected = post.isLiked,
                    onClick = onLike
                )
                InteractionButton(
                    icon = Icons.Default.ModeComment,
                    count = post.commentsCount,
                    isSelected = false,
                    onClick = { showCommentInput.value = !showCommentInput.value }
                )
            }
            if (showCommentInput.value) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Write a comment...", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { onComment(commentText); commentText = ""; showCommentInput.value = false }) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatePostDialog(
    onDismiss: () -> Unit,
    viewModel: CommunityViewModel
) {
    val context = LocalContext.current
    var content by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // 监听发布结果
    val actionState by viewModel.actionState.collectAsState()
    LaunchedEffect(actionState) {
        when (actionState) {
            is PostActionState.Success -> {
                isUploading = false
                viewModel.resetActionState()
                onDismiss()
            }
            is PostActionState.Error -> {
                isUploading = false
            }
            else -> {}
        }
    }

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImageUris = selectedImageUris + uris
    }

    // 视频选择器
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedVideoUri = it }
    }

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { if (!isUploading) onDismiss() }) {
                        Text("取消")
                    }
                    Text(
                        "发布帖子",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = {
                            if (content.isNotBlank() && !isUploading) {
                                isUploading = true
                                // 将 Uri 转换为 MultipartBody.Part
                                val parts = mutableListOf<MultipartBody.Part>()

                                // 添加图片
                                selectedImageUris.forEach { uri ->
                                    uriToPart(context, uri)?.let { parts.add(it) }
                                }
                                // 添加视频
                                selectedVideoUri?.let { uri ->
                                    uriToPart(context, uri)?.let { parts.add(it) }
                                }

                                viewModel.createPostWithFiles(content, parts)
                            }
                        },
                        enabled = content.isNotBlank() && !isUploading
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("发布")
                        }
                    }
                }

                Divider()

                // 内容输入
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("分享你的想法...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        maxLines = 10,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground
                        )
                    )

                    // 图片预览区
                    if (selectedImageUris.isNotEmpty()) {
                        Text("已选图片 (${selectedImageUris.size})", style = MaterialTheme.typography.labelMedium)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedImageUris) { uri ->
                                Box(
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedImageUris = selectedImageUris - uri
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(20.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.6f),
                                                RoundedCornerShape(bottomStart = 8.dp)
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "移除",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 视频预览区
                    if (selectedVideoUri != null) {
                        Text("已选视频", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardBackground)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "视频文件已选择",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = { selectedVideoUri = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "移除",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // 错误提示
                    if (actionState is PostActionState.Error) {
                        Text(
                            text = (actionState as PostActionState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // 底部操作栏
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 图片按钮
                        FilledTonalButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("图片")
                        }

                        // 视频按钮
                        FilledTonalButton(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            modifier = Modifier.weight(1f),
                            enabled = selectedVideoUri == null
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (selectedVideoUri != null) "已选" else "视频")
                        }
                    }
                }
            }
        }
    }
}

// 将 Uri 转换为 MultipartBody.Part
private fun uriToPart(context: android.content.Context, uri: Uri): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/*"
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()

        val fileName = "${System.currentTimeMillis()}_${bytes.hashCode()}"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        MultipartBody.Part.createFormData("files", fileName, requestBody)
    } catch (e: Exception) {
        null
    }
}
