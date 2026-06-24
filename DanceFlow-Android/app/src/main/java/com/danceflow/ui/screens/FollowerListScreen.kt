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
import coil.compose.AsyncImage
import com.danceflow.app.data.dto.FollowResponse
import com.danceflow.app.follow.FollowListState
import com.danceflow.app.follow.FollowViewModel
import com.danceflow.app.ui.theme.PrimaryColor
import com.danceflow.app.ui.theme.TextSecondary
import com.danceflow.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowerListScreen(
    userId: Long,
    initialTab: Int = 0,
    navController: NavHostController,
    viewModel: FollowViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    LaunchedEffect(userId, selectedTab) {
        if (selectedTab == 0) viewModel.loadFollowing(userId)
        else viewModel.loadFollowers(userId)
    }

    val followingState by viewModel.followingState.collectAsState()
    val followersState by viewModel.followersState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedTab == 0) "Following" else "Followers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("Following") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("Followers") })
            }

            val stateList = if (selectedTab == 0) followingState else followersState
            when (val state = stateList) {
                is FollowListState.Loading -> LoadingState()
                is FollowListState.Success -> {
                    if (state.list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No users", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(state.list) { user ->
                                FollowUserRow(
                                    user = user,
                                    currentUserId = userId,
                                    onUserClick = { navController.navigate("user_profile/${user.userId}") },
                                    onToggleFollow = {
                                        viewModel.toggleFollow(user.userId, user.isMutual) {
                                            if (selectedTab == 0) viewModel.loadFollowing(userId)
                                            else viewModel.loadFollowers(userId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is FollowListState.Error -> ErrorState(
                    message = state.message,
                    onRetry = {
                        if (selectedTab == 0) viewModel.loadFollowing(userId)
                        else viewModel.loadFollowers(userId)
                    }
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun FollowUserRow(
    user: FollowResponse,
    currentUserId: Long,
    onUserClick: () -> Unit,
    onToggleFollow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onUserClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape)) {
                if (user.avatarUrl != null) {
                    AsyncImage(
                        model = fullUrl(user.avatarUrl),
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
                Text(text = user.username ?: "Unknown", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (user.userId != currentUserId) {
                OutlinedButton(
                    onClick = onToggleFollow,
                    colors = if (user.isMutual) ButtonDefaults.outlinedButtonColors()
                        else ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text(
                        if (user.isMutual) "Following" else "Follow",
                        fontSize = 13.sp,
                        color = if (user.isMutual) PrimaryColor else androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    }
}
