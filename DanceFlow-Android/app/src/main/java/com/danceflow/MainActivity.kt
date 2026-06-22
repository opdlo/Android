package com.danceflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.SessionManager
import com.danceflow.app.ui.navigation.MainScreen
import com.danceflow.app.ui.screens.LoginScreen
import com.danceflow.app.ui.screens.RegisterScreen
import com.danceflow.app.ui.theme.DanceFlowTheme
import com.danceflow.app.ui.theme.ThemeState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitInstance.init(applicationContext)
        SessionManager.init(applicationContext)
        setContent {
            val darkMode by ThemeState.isDarkMode.collectAsState()
            DanceFlowTheme(darkTheme = darkMode) {
                // 鐢ㄤ竴涓彲鍙樼姸鎬佹潵鎺у埗鏄剧ず鍝釜鐣岄潰
                var isLoggedIn by remember { mutableStateOf(SessionManager.isLoggedIn()) }
                var showRegister by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    SessionManager.authExpiredEvents.receiveAsFlow().collect {
                        isLoggedIn = false
                    }
                }

                if (isLoggedIn) {
                    // 濡傛灉宸茬櫥褰曪紝鏄剧ず涓荤晫闈?
                    MainScreen(
                        // 浼犲叆閫€鍑虹櫥褰曠殑閫昏緫
                        onLogout = {
                            scope.launch {
                                SessionManager.clearSession()
                                isLoggedIn = false     // 鏇存柊UI
                            }
                        }
                    )
                } else {
                    if (showRegister) {
                        RegisterScreen(
                            onRegisterSuccess = { token ->
                                scope.launch {
                                    SessionManager.saveAuthToken(token)
                                    SessionManager.saveLoginStatus(true)
                                    isLoggedIn = true
                                }
                            },
                            onNavigateToLogin = {
                                showRegister = false
                            }
                        )
                    } else {
                        // 濡傛灉鏈櫥褰曪紝鏄剧ず鐧诲綍鐣岄潰
                        LoginScreen(
                            onLoginSuccess = { token ->
                                // 鐧诲綍鎴愬姛鍚庯紝鏇存柊鐘舵€佸苟淇濆瓨
                                scope.launch {
                                    SessionManager.saveAuthToken(token)
                                    SessionManager.saveLoginStatus(true)
                                    isLoggedIn = true
                                }
                            },
                            onNavigateToRegister = {
                                showRegister = true
                            }
                        )
                    }
                }
            }
        }
    }
}
