package com.danceflow.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danceflow.app.ui.navigation.NavBarScreen
import com.danceflow.app.ui.theme.PrimaryColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.RowScope
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val screens = listOf(
        NavBarScreen.Feed,
        NavBarScreen.Practice,
        NavBarScreen.Profile,
        NavBarScreen.Learning,
        NavBarScreen.Settings
    )

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color(0xFF0F3460),
        contentColor = PrimaryColor
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            NavItem(screen = screens[0], route = currentRoute, onNavigate = onNavigate, modifier = Modifier.weight(1f))
            NavItem(screen = screens[1], route = currentRoute, onNavigate = onNavigate, modifier = Modifier.weight(1f))
            NavItem(screen = screens[2], route = currentRoute, onNavigate = onNavigate, modifier = Modifier.weight(1f))
            NavItem(screen = screens[3], route = currentRoute, onNavigate = onNavigate, modifier = Modifier.weight(1f))
            NavItem(screen = screens[4], route = currentRoute, onNavigate = onNavigate, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RowScope.NavItem(
    screen: NavBarScreen,
    route: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
        label = { Text(screen.title) },
        selected = route == screen.route || (route != null && route.startsWith("${screen.route}?")),
        onClick = { if (route != screen.route) onNavigate(screen.route) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = PrimaryColor,
            selectedTextColor = PrimaryColor,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,
            indicatorColor = Color(0xFF1A1A2E)
        ),
        modifier = modifier
    )
}
