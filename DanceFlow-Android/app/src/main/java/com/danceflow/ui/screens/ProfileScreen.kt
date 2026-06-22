package com.danceflow.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.danceflow.app.data.SessionManager
import com.danceflow.app.profile.FavoritesState
import com.danceflow.app.profile.ProfileState
import com.danceflow.app.profile.ProfileViewModel
import com.danceflow.app.ui.components.*
import com.danceflow.app.ui.navigation.NavBarScreen
import com.danceflow.app.ui.theme.CardBackground
import com.danceflow.app.ui.theme.TextSecondary
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.danceflow.app.ui.theme.ThemeState
import androidx.compose.material.icons.filled.DarkMode
import com.danceflow.app.ui.theme.PrimaryColor
@Composable
fun FavoriteItem(item: com.danceflow.app.data.dto.FavoriteItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp, 40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title ?: "Unnamed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = if (item.type == "post") "Post" else "Video",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun defaultOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
)

// Convert Uri to MultipartBody.Part (for avatar upload)
fun uriToMultipartPart(context: android.content.Context, uri: Uri): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/*"
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()

        val fileName = "avatar_${System.currentTimeMillis()}"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        MultipartBody.Part.createFormData("file", fileName, requestBody)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun DarkModeToggle(
    darkMode: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DarkMode, contentDescription = "Dark mode",
                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Dark Mode", fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)
            )
            Switch(
                checked = darkMode, onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = tint,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun MenuSection(
    onFavoritesClick: () -> Unit,
    onPracticeHistoryClick: () -> Unit,
    viewModel: ProfileViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            MenuItem(
                icon = Icons.Default.Favorite,
                title = "My Favorites",
                onClick = {
                    viewModel.loadFavorites()
                    onFavoritesClick()
                }
            )
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFF2D2D44)
            )
            MenuItem(
                icon = Icons.Default.History,
                title = "Practice History",
                onClick = onPracticeHistoryClick
            )
        }
    }
}

@Composable
fun OtherSection(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            MenuItem(
                icon = Icons.Default.SwapHoriz,
                title = "Switch Account",
//                onClick = { /* TODO: Switch Account */ },
                onClick = { onLogout() }
            )
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFF2D2D44)
            )
            MenuItem(
                icon = Icons.Default.ExitToApp,
                title = "Logout",
                onClick = {
                    viewModel.loadProfile()
                    onLogout()
                },
                tint = Color(0xFFF44336)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    user: com.danceflow.app.data.dto.UserProfileResponse,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String?, String?) -> Unit
) {
    var gender by remember { mutableStateOf(user.gender ?: "") }
    var birthday by remember { mutableStateOf(user.birthday ?: "") }
    var signature by remember { mutableStateOf(user.signature ?: "") }
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gender
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = !genderExpanded }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = defaultOutlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                // Birthday
                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    label = { Text("Birthday (yyyy-MM-dd)") },
                    placeholder = { Text("2000-01-01") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultOutlinedTextFieldColors()
                )

                // Signature
                OutlinedTextField(
                    value = signature,
                    onValueChange = { signature = it },
                    label = { Text("Signature") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultOutlinedTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        gender.takeIf { it.isNotBlank() },
                        birthday.takeIf { it.isNotBlank() },
                        signature.takeIf { it.isNotBlank() },
                        user.avatarUrl
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FavoritesDialog(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel
) {
    val favoritesState by viewModel.favoritesState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Favorites") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                when (val state = favoritesState) {
                    is FavoritesState.Loading -> LoadingState()
                    is FavoritesState.Success -> {
                        if (state.favorites.isEmpty()) {
                            EmptyState("No favorites yet")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.favorites) { item ->
                                    FavoriteItem(item = item)
                                }
                            }
                        }
                    }

                    is FavoritesState.Error -> ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadFavorites() }
                    )

                    else -> {
                        LaunchedEffect(Unit) {
                            viewModel.loadFavorites()
                        }
                        LoadingState()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}



@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    var showEditProfile by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    val darkMode by ThemeState.isDarkMode.collectAsState()
    val context = LocalContext.current

    val profileState by viewModel.profileState.collectAsState()
    val favoritesState by viewModel.favoritesState.collectAsState()

    // Avatar picker
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uriToMultipartPart(context, it)?.let { part ->
                isUploadingAvatar = true
                viewModel.uploadAvatar(part) {
                    isUploadingAvatar = false
                    viewModel.loadProfile()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    when (val state = profileState) {
        is ProfileState.Loading -> LoadingState()
        is ProfileState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // User Info header
                ProfileHeader(
                    user = state.user,
                    onEditClick = { showEditProfile = true },
                    onAvatarClick = { avatarPickerLauncher.launch("image/*") },
                    onSettingsClick = { navController.navigate(NavBarScreen.Settings.route) },
                    isUploadingAvatar = isUploadingAvatar
                )


                // Function menu
                Spacer(modifier = Modifier.height(16.dp))
                MenuSection(
                    onFavoritesClick = { showFavorites = true },
                    onPracticeHistoryClick = {
                        navController.navigate("analysis_list") {
                            popUpTo(NavBarScreen.Profile.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    viewModel = viewModel
                )


                // Other options
                Spacer(modifier = Modifier.height(16.dp))

                DarkModeToggle(
                    darkMode = darkMode,
                    onToggle = { ThemeState.toggle() }
                )

                OtherSection(
                    onLogout = onLogout,
                    viewModel = viewModel
                )
            }
        }
        is ProfileState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.loadProfile() }
        )
        else -> LoadingState()
    }

    // Edit Profile dialog
    if (showEditProfile) {
        val currentProfile = profileState
        if (currentProfile is ProfileState.Success) {
            EditProfileDialog(
                user = currentProfile.user,
                onDismiss = { showEditProfile = false },
                onSave = { gender, birthday, signature, avatarUrl ->
                    viewModel.updateProfile(gender, birthday, signature, avatarUrl)
                    showEditProfile = false
                }
            )
        }
    }

    // Favorites dialog
    if (showFavorites) {
        FavoritesDialog(
            onDismiss = { showFavorites = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun ProfileHeader(
    user: com.danceflow.app.data.dto.UserProfileResponse,
    onEditClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isUploadingAvatar: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Background image
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            if (user.backgroundUrl != null) {
                AsyncImage(
                    model = user.backgroundUrl, contentDescription = null,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(PrimaryColor.copy(alpha = 0.15f)))
            }
        }
        // Settings button
        IconButton(onClick = onSettingsClick, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(
                Icons.Default.Settings, contentDescription = "Settings",
                tint = Color.White
            )
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(top = 50.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar (clickable to change)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(enabled = !isUploadingAvatar) { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = rememberVectorPainter(Icons.Default.AccountCircle),
                    error = rememberVectorPainter(Icons.Default.AccountCircle)
                )
                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingAvatar) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Username
            Text(
                text = user.username,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Signature
            if (!user.signature.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.signature,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }


            // Edit button
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile", fontSize = 15.sp)
            }
        }
    }
}

