package com.danceflow.app.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// 扩展属性创建 DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

object SessionManager {
    private const val TAG = "SessionManager"

    private lateinit var context: Context
    private lateinit var dataStore: DataStore<Preferences>

    // 用于通知 token 过期的事件通道
    val authExpiredEvents = Channel<Unit>(Channel.BUFFERED)

    // Preferences Keys
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    private val LOGIN_STATUS_KEY = stringPreferencesKey("login_status")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USERNAME_KEY = stringPreferencesKey("username")

    fun init(context: Context) {
        this.context = context.applicationContext
        this.dataStore = context.dataStore
    }

    // 保存认证 Token
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
        Log.d(TAG, "Token saved")
    }

    // 获取认证 Token
    suspend fun getAuthToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }.first()
    }

    // 同步获取 Token（用于在拦截器中）
    fun getAuthTokenSync(): String? {
        var token: String? = null
        // 使用 runBlocking 来同步获取
        try {
            kotlinx.coroutines.runBlocking {
                token = dataStore.data.map { preferences ->
                    preferences[AUTH_TOKEN_KEY]
                }.first()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting token", e)
        }
        return token
    }

    // 保存登录状态
    suspend fun saveLoginStatus(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[LOGIN_STATUS_KEY] = isLoggedIn.toString()
        }
        Log.d(TAG, "Login status saved: $isLoggedIn")
    }

    // 获取登录状态
    suspend fun getLoginStatus(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[LOGIN_STATUS_KEY]?.toBoolean() ?: false
        }.first()
    }

    // 同步获取登录状态
    fun isLoggedIn(): Boolean {
        var status = false
        try {
            kotlinx.coroutines.runBlocking {
                status = getLoginStatus()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting login status", e)
        }
        return status
    }

    // 保存用户信息
    suspend fun saveUserInfo(userId: String, username: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
        }
    }

    // 获取用户信息
    suspend fun getUserInfo(): Pair<String?, String?> {
        return dataStore.data.map { preferences ->
            Pair(
                preferences[USER_ID_KEY],
                preferences[USERNAME_KEY]
            )
        }.first()
    }

    // 清除会话信息
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        Log.d(TAG, "Session cleared")
    }

    // 通知 token 过期
    suspend fun notifyAuthExpired() {
        authExpiredEvents.send(Unit)
        clearSession()
    }
}
