package com.danceflow.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.danceflow.app.ui.components.BottomNavBar
import com.danceflow.app.ui.screens.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            val showBottomBar = currentRoute?.let { route ->
                listOf(
                    NavBarScreen.Feed.route,
                    NavBarScreen.Practice.route,
                    NavBarScreen.Learning.route,
                    NavBarScreen.Profile.route,
                    NavBarScreen.Settings.route
                ).any { route == it || route.startsWith("$it?") }
            } == true
            if (showBottomBar) {
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = NavBarScreen.Feed.route
            ) {
                // Feed - shows community posts
                composable(NavBarScreen.Feed.route) {
                    CommunityScreen(navController = navController)
                }

                // Practice History (with optional tab param)
                composable(
                    route = "${NavBarScreen.Practice.route}?tab={tab}",
                    arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 1 })
                ) { backStackEntry ->
                    val tab = backStackEntry.arguments?.getInt("tab") ?: 0
                    AnalysisScreen(navController = navController, initialTab = tab)
                }

                // Learning page (no longer a bottom tab)
                composable("learning") {
                    LearningScreen(navController = navController)
                }

                // Community (no longer a bottom tab, accessible via string route)
                composable("community") {
                    CommunityScreen(navController = navController)
                }

                // Profile page
                composable(NavBarScreen.Profile.route) {
                    ProfileScreen(onLogout = onLogout, navController = navController)
                }

                // Analysis sub-route (not a tab), optional tab parameter
                composable(
                    route = "analysis_list?tab={tab}",
                    arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
                ) { backStackEntry ->
                    val tab = backStackEntry.arguments?.getInt("tab") ?: 0
                    AnalysisScreen(navController = navController, initialTab = tab)
                }
                // 分析结果页面
                composable("analysis_result/{practiceId}") { backStackEntry ->
                    val practiceId = backStackEntry.arguments?.getString("practiceId")?.toLongOrNull() ?: 0L
                    AnalysisResultScreen(
                        practiceId = practiceId,
                        navController = navController
                    )
                }

                // 视频详情页面
                composable("video_detail/{videoId}") { backStackEntry ->
                    val videoId = backStackEntry.arguments?.getString("videoId")?.toLongOrNull() ?: 0L
                    // TODO: 创建视频详情页面
                    VideoDetailScreen(videoId = videoId, navController = navController)
                }

                composable("post_detail/{postId}") { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId")?.toLongOrNull() ?: 0L
                    PostDetailScreen(postId = postId, navController = navController)
                }
                // Post detail sub-route
                // Settings page
                composable(NavBarScreen.Settings.route) {
                    SettingsScreen(
                        onLogout = onLogout,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }


}

// 临时占位符 - 视频详情页面
@Composable
private fun VideoDetailScreen(videoId: Long, navController: NavHostController) {
    // TODO: 实现视频详情页面
}
