package com.danceflow.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.danceflow.app.data.dto.ConversationResponse
import com.danceflow.app.message.ConversationListState
import com.danceflow.app.message.MessageViewModel
import com.danceflow.app.ui.theme.PrimaryColor
import com.danceflow.app.ui.theme.TextSecondary
import com.danceflow.app.ui.components.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    navController: NavHostController,
    viewModel: MessageViewModel = viewModel()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(backStackEntry?.destination?.route) {
        if (backStackEntry?.destination?.route == "conversations") {
            viewModel.loadConversations()
        }
    }
    val convState by viewModel.conversationsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = convState) {
            is ConversationListState.Loading -> LoadingState()
            is ConversationListState.Success -> {
                if (state.conversations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No conversations yet", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.conversations) { conv ->
                            ConversationItem(
                                conv = conv,
                                onClick = {
                                    navController.navigate("chat/${conv.userId}/${conv.username ?: "User"}")
                                }
                            )
                        }
                    }
                }
            }
            is ConversationListState.Error -> ErrorState(
                message = state.message,
                onRetry = { viewModel.loadConversations() }
            )
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationItem(conv: ConversationResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(modifier = Modifier.size(48.dp).clip(CircleShape)) {
                if (conv.avatarUrl != null) {
                    AsyncImage(
                        model = fullUrl(conv.avatarUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AccountCircle, contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(4.dp), tint = TextSecondary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = conv.username ?: "Unknown", fontSize = 16.sp, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!conv.lastMessage.isNullOrBlank()) {
                    Text(text = conv.lastMessage, fontSize = 13.sp, color = TextSecondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (conv.lastMessageTime != null) {
                    Text(text = formatTime(conv.lastMessageTime), fontSize = 11.sp, color = TextSecondary)
                }
                if (conv.unreadCount > 0) {
                    Badge(containerColor = PrimaryColor) {
                        Text(text = if (conv.unreadCount > 99) "99+" else "${conv.unreadCount}",
                            fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun formatTime(timeStr: String?): String {
    if (timeStr == null) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(timeStr.take(19))
        if (date != null) {
            val now = Calendar.getInstance()
            val msgCal = Calendar.getInstance().apply { time = date }
            val fmt = if (now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR))
                SimpleDateFormat("HH:mm", Locale.getDefault())
            else if (now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR))
                SimpleDateFormat("MM/dd", Locale.getDefault())
            else
                SimpleDateFormat("yy/MM/dd", Locale.getDefault())
            fmt.format(date)
        } else ""
    } catch (e: Exception) { "" }
}
