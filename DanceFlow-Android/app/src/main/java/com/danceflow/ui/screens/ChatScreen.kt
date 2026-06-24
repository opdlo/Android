package com.danceflow.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.danceflow.app.data.dto.MessageResponse
import com.danceflow.app.message.MessageListState
import com.danceflow.app.message.MessageViewModel
import com.danceflow.app.ui.components.ErrorState
import com.danceflow.app.ui.components.LoadingState
import com.danceflow.app.ui.components.fullUrl
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import com.danceflow.app.ui.theme.PrimaryColor
import com.danceflow.app.ui.theme.TextSecondary
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.dto.SendMessageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    otherUserId: Long,
    otherUsername: String,
    navController: NavHostController,
    viewModel: MessageViewModel = viewModel()
) {
    var inputText by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val msgState by viewModel.messagesState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(otherUserId) {
            viewModel.loadConversation(otherUserId)
            viewModel.markAllAsRead(otherUserId)
        }
    LaunchedEffect(msgState) {
        if (msgState is MessageListState.Success) {
            val count = (msgState as MessageListState.Success).messages.size
            if (count > 0) listState.animateScrollToItem(count - 1)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val part = uriToPart(context, uri)
                    if (part != null) {
                        val resp = RetrofitInstance.getUploadApi().uploadFiles(listOf(part))
                        if (resp.isSuccessful && resp.body() != null) {
                            val url = resp.body()!!.urls.firstOrNull()
                            if (url != null) {
                                val msgReq = SendMessageRequest(
                                    receiverId = otherUserId,
                                    content = "[Image]",
                                    messageType = "image",
                                    mediaUrl = url
                                )
                                RetrofitInstance.getMessageApi().sendMessage(msgReq)
                                viewModel.loadConversation(otherUserId)
                            }
                        }
                    }
                } catch (e: Exception) { }
                isUploading = false
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val part = uriToPart(context, uri)
                    if (part != null) {
                        val resp = RetrofitInstance.getUploadApi().uploadFiles(listOf(part))
                        if (resp.isSuccessful && resp.body() != null) {
                            val url = resp.body()!!.urls.firstOrNull()
                            if (url != null) {
                                val msgReq = SendMessageRequest(
                                    receiverId = otherUserId,
                                    content = "[Video]",
                                    messageType = "video",
                                    mediaUrl = url
                                )
                                RetrofitInstance.getMessageApi().sendMessage(msgReq)
                                viewModel.loadConversation(otherUserId)
                            }
                        }
                    }
                } catch (e: Exception) { }
                isUploading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherUsername, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp, 4.dp, 8.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        enabled = !isUploading,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Image", tint = PrimaryColor)
                    }
                    IconButton(
                        onClick = { videoPickerLauncher.launch("video/*") },
                        enabled = !isUploading,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video", tint = PrimaryColor)
                    }
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(otherUserId, inputText) { inputText = "" }
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        when (val state = msgState) {
            is MessageListState.Loading -> LoadingState()
            is MessageListState.Success -> {
                LazyColumn(
                    state = listState,
                modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.messages) { msg ->
                        MessageBubble(msg = msg, isMine = msg.senderId != otherUserId, navController = navController)
                    }
                }
            }
            is MessageListState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadConversation(otherUserId) })
            else -> {}
        }
    }
}

@Composable
private fun MessageBubble(msg: MessageResponse, isMine: Boolean, navController: NavHostController? = null) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMine) PrimaryColor else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp, 8.dp).widthIn(max = 280.dp)) {
                // Different content based on message type
                when (msg.messageType) {
                    "image" -> {
                        if (msg.mediaUrl != null) {
                            AsyncImage(
                                model = fullUrl(msg.mediaUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(200.dp, 150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                    "video" -> {
                        Box(
                            modifier = Modifier.size(200.dp, 150.dp).clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Video",
                                modifier = Modifier.size(48.dp),
                                tint = Color.White
                            )
                        }
                    }
                    "post_share" -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (msg.referenceId != null && navController != null) {
                                        navController.navigate("post_detail/${msg.referenceId}")
                                    }
                                },
                            color = if (isMine) PrimaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isMine) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Shared a post",
                                        fontSize = 12.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = if (isMine) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (msg.referenceContent != null) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = msg.referenceContent,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        color = if (isMine) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Tap to view",
                                    fontSize = 10.sp,
                                    color = if (isMine) Color.White.copy(alpha = 0.6f) else com.danceflow.app.ui.theme.TextSecondary
                                )
                            }
                        }
                    }
                    else -> {
                        // text message - show content
                        if (msg.content.isNotBlank()) {
                            Text(
                                text = msg.content,
                                fontSize = 15.sp,
                                color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Text(
                    text = formatMsgTime(msg.createdAt),
                    fontSize = 10.sp,
                    color = if (isMine) Color.White.copy(alpha = 0.7f) else TextSecondary,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }
    }
}

private fun formatMsgTime(timeStr: String?): String {
    if (timeStr == null) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(timeStr.take(19))
        if (date != null) SimpleDateFormat("HH:mm", Locale.getDefault()).format(date) else ""
    } catch (e: Exception) { "" }
}
