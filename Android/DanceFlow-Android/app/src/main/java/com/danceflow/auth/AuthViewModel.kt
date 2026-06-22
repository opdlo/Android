package com.danceflow.app.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.SessionManager
import com.danceflow.app.data.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String, val user: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            try {
                val response = RetrofitInstance.getAuthApi()
                    .login(LoginRequest(username, password))

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    // 保存认证信息
                    SessionManager.saveAuthToken(authResponse.token)
                    SessionManager.saveLoginStatus(true)
                    SessionManager.saveUserInfo(
                        authResponse.userId.toString(),
                        authResponse.username
                    )
                    _loginState.value = AuthState.Success(authResponse.token, authResponse)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "登录失败"
                    _loginState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error", e)
                _loginState.value = AuthState.Error("网络错误: ${e.message}")
            }
        }
    }

    fun register(username: String, password: String, email: String) {
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            try {
                val response = RetrofitInstance.getAuthApi()
                    .register(RegisterRequest(username, password, email))

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    // 保存认证信息
                    SessionManager.saveAuthToken(authResponse.token)
                    SessionManager.saveLoginStatus(true)
                    SessionManager.saveUserInfo(
                        authResponse.userId.toString(),
                        authResponse.username
                    )
                    _registerState.value = AuthState.Success(authResponse.token, authResponse)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "注册失败"
                    _registerState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Register error", e)
                _registerState.value = AuthState.Error("网络错误: ${e.message}")
            }
        }
    }

    fun resetStates() {
        _loginState.value = AuthState.Idle
        _registerState.value = AuthState.Idle
    }
}
