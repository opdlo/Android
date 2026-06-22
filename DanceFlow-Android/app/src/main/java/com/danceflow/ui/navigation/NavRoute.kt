package com.danceflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    object Feed : NavBarScreen("feed", "Feed", Icons.Filled.Home)
    object Practice : NavBarScreen("practice", "Practice", Icons.Filled.PlayArrow)
    object Learning : NavBarScreen("learning", "Learning", Icons.Filled.Source)
    object Profile : NavBarScreen("profile", "Profile", Icons.Filled.Person)
    object Settings : NavBarScreen("settings", "Settings", Icons.Filled.Settings)
}
