package com.danceflow.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danceflow.app.auth.AuthState
import com.danceflow.app.auth.AuthViewModel
import com.danceflow.app.ui.components.ErrorState
import com.danceflow.app.ui.components.LoadingState

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is AuthState.Success) {
            onRegisterSuccess((registerState as AuthState.Success).token)
            viewModel.resetStates()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (registerState) {
            is AuthState.Loading -> {
                LoadingState()
            }
            is AuthState.Error -> {
                ErrorState(
                    message = (registerState as AuthState.Error).message,
                    onRetry = { viewModel.register(username, password, email) }
                )
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "注册账号",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 用户名
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("用户名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultOutlinedTextFieldColors()
                    )

                    // 邮箱
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("邮箱") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultOutlinedTextFieldColors()
                    )

                    // 密码
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultOutlinedTextFieldColors()
                    )

                    // 确认密码
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (it != password) {
                                errorMessage = "两次输入的密码不一致"
                            } else {
                                errorMessage = ""
                            }
                        },
                        label = { Text("确认密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultOutlinedTextFieldColors(),
                        isError = errorMessage.isNotEmpty()
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 注册按钮
                    Button(
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank() &&
                                password == confirmPassword && email.isNotBlank()) {
                                viewModel.register(username, password, email)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = username.isNotBlank() && password.isNotBlank() &&
                                confirmPassword == password && email.isNotBlank()
                    ) {
                        Text("注册", fontSize = 16.sp)
                    }

                    // 登录链接
                    TextButton(onClick = onNavigateToLogin) {
                        Text("已有账号？去登录")
                    }
                }
            }
        }
    }
}

@Composable
private fun defaultOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)
