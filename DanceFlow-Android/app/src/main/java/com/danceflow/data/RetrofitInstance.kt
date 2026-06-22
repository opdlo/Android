package com.danceflow.app.data

import android.content.Context
import com.danceflow.app.data.api.AuthApiService
import com.danceflow.app.data.api.CommunityApiService
import com.danceflow.app.data.api.LearningApiService
import com.danceflow.app.data.api.AnalysisApiService
import com.danceflow.app.data.api.ProfileApiService
import com.danceflow.app.data.api.UploadApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
object RetrofitInstance {
    private const val BASE_URL = "http://localhost:8080/" // adb reverse 转发

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var gson: Gson
    private lateinit var retrofit: Retrofit

    private lateinit var authApiService: AuthApiService
    private lateinit var communityApiService: CommunityApiService
    private lateinit var learningApiService: LearningApiService
    private lateinit var analysisApiService: AnalysisApiService
    private lateinit var profileApiService: ProfileApiService
    private lateinit var uploadApiService: UploadApiService

    fun init(context: Context) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                // 从 SessionManager 获取 token 并添加到请求头
        SessionManager.getAuthTokenSync()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }
    .addInterceptor { chain ->
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            runBlocking { SessionManager.notifyAuthExpired() }
        }
        response
    }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        gson = GsonBuilder()
            .setLenient()
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        // 初始化各个 API 服务
        authApiService = retrofit.create(AuthApiService::class.java)
        communityApiService = retrofit.create(CommunityApiService::class.java)
        learningApiService = retrofit.create(LearningApiService::class.java)
        analysisApiService = retrofit.create(AnalysisApiService::class.java)
        profileApiService = retrofit.create(ProfileApiService::class.java)
        uploadApiService = retrofit.create(UploadApiService::class.java)
    }

    fun getAuthApi(): AuthApiService = authApiService
    fun getCommunityApi(): CommunityApiService = communityApiService
    fun getLearningApi(): LearningApiService = learningApiService
    fun getAnalysisApi(): AnalysisApiService = analysisApiService
    fun getProfileApi(): ProfileApiService = profileApiService
    fun getUploadApi(): UploadApiService = uploadApiService


}

