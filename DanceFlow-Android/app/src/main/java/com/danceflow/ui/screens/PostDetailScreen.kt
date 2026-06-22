package com.danceflow.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.danceflow.app.community.DetailState
import com.danceflow.app.community.PostDetailViewModel
import com.danceflow.app.ui.components.*
import com.danceflow.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Long,
    navController: NavController,
    viewModel: PostDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    LaunchedEffect(postId) {
        viewModel.load(postId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { viewModel.updateCommentText(it) },
                        placeholder = { Text("Write a comment...", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { viewModel.sendComment(postId) }) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { padding ->
        when (val s = state) {
            is DetailState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is DetailState.Error -> ErrorState(s.message, onRetry = { viewModel.load(postId) }, modifier = Modifier.padding(padding))
            is DetailState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        PostDetailCard(
                            post = s.post,
                            onLike = { viewModel.likePost(postId) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Comments (${s.comments.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    items(s.comments) { comment ->
                        CommentCard(comment = comment)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (s.comments.isEmpty()) {
                        item {
                            Text(
                                text = "No comments yet",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun PostDetailCard(
    post: com.danceflow.app.data.dto.PostResponse,
    onLike: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.author.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = post.author.username, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = formatTime(post.createdAt), fontSize = 12.sp, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = post.content, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (!post.images.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                post.images.forEach { url ->
                    AsyncImage(model = url, contentDescription = null,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).clip(RoundedCornerShape(8.dp)).padding(bottom = 4.dp),
                        contentScale = ContentScale.Fit)
                }
            }
            if (post.videoUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))) {
                    AsyncImage(model = post.videoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.align(Alignment.Center).size(48.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike) {
                    Icon(
                        if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) MaterialTheme.colorScheme.primary else TextSecondary
                    )
                }
                Text(text = "${post.likesCount}", fontSize = 13.sp, color = TextSecondary)
            }
        }
    }
}
@Composable
private fun CommentCard(comment: com.danceflow.app.data.dto.CommentResponse) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(model = comment.author.avatarUrl, contentDescription = null,
                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(17.dp)),
                contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)) {
                            append("${comment.author.username}: ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                            append(comment.content)
                        }
                    },
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formatTime(comment.createdAt), fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}
