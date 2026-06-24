package com.danceflow.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.SessionManager
import com.danceflow.app.data.dto.PostResponse
import com.danceflow.app.ui.components.*
import com.danceflow.app.ui.theme.TextSecondary
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsScreen(navController: NavController) {
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf<PostResponse?>(null) }


    LaunchedEffect(Unit) {
        try {
            val userId = SessionManager.getUserIdSync()
            if (userId != null) {
                val resp = RetrofitInstance.getCommunityApi().getPosts(page = 0, size = 50, userId = userId)
                if (resp.isSuccessful && resp.body() != null) {
                    posts = resp.body()!!.posts
                } else {
                    errorMsg = "Failed to load posts"
                }
            } else {
                errorMsg = "User not logged in"
            }
        } catch (e: Exception) {
            errorMsg = "Error: ${e.message}"
        }
        loaded = true
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Posts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMsg.isNotEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMsg, color = MaterialTheme.colorScheme.error)
                    }
                }
                posts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No posts yet", color = TextSecondary)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(posts) { post ->
                            MyPostCard(
                                post = post,
                                onDelete = { showDeleteConfirm = it },
                                onClick = { navController.navigate("post_detail/${post.id}") }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteConfirm?.let { post ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = null
                    deletePost(post.id) { success ->
                        if (success) {
                            posts = posts.filter { it.id != post.id }
                        }
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun deletePost(postId: Long, onResult: (Boolean) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val resp = RetrofitInstance.getCommunityApi().deletePost(postId)
            if (resp.isSuccessful) {
                withContext(Dispatchers.Main) { onResult(true) }
            } else {
                withContext(Dispatchers.Main) { onResult(false) }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult(false) }
        }
    }
}

@Composable
private fun MyPostCard(
    post: PostResponse,
    onDelete: (PostResponse) -> Unit,
    onClick: () -> Unit
) {

    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var fullscreenVideoUrl by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            // Header row with author info and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author avatar
                if (post.author != null) {
                    AsyncImage(
                        model = fullUrl(post.author.avatarUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.author.username ?: "Unknown",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = post.createdAt?.take(10) ?: "",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                // Delete button
                IconButton(
                    onClick = { onDelete(post) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content text
            Box(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            ) {
                Text(
                    text = post.content,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Images
            if (!post.images.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    post.images.take(3).forEach { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { fullscreenImageUrl = imageUrl },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Video
            if (post.videoUrl != null) {
                Spacer(modifier = Modifier.height(10.dp))
                if (fullUrl(post.videoUrl) != null) {
                    Box {
                        VideoPlayer(
                            videoUrl = fullUrl(post.videoUrl) ?: "",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        IconButton(
                            onClick = { fullscreenVideoUrl = post.videoUrl },
                            modifier = Modifier.align(Alignment.BottomEnd).size(36.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen",
                                tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Like
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) MaterialTheme.colorScheme.primary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(post.likesCount.toString(), fontSize = 12.sp, color = TextSecondary)
                }
                // Comment
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ModeComment,
                        contentDescription = "Comment",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(post.commentsCount.toString(), fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(Modifier.weight(1f))
                // Favorite
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (post.isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (post.isFavorited) MaterialTheme.colorScheme.primary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    fullscreenImageUrl?.let { url ->
        val fu = fullUrl(url) ?: url
        Dialog(
            onDismissRequest = { fullscreenImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black).clickable { fullscreenImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(model = fu, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().padding(16.dp), contentScale = ContentScale.Fit)
            }
        }
    }
    fullscreenVideoUrl?.let { url ->
        val fu = fullUrl(url) ?: url
        Dialog(
            onDismissRequest = { fullscreenVideoUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                VideoPlayer(videoUrl = fu, modifier = Modifier.fillMaxSize())
                IconButton(onClick = { fullscreenVideoUrl = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}
