package com.example.codise.data

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Header
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

interface ApiService {
    @POST("api/auth/register/")
    suspend fun register(@Body user: User): Response<AuthResponse>

    @POST("api/auth/login/")
    suspend fun login(@Body credentials: LoginRequest): Response<AuthResponse>

    @PATCH("api/auth/profile/update/")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body user: User
    ): Response<User>

    @GET("api/ciudades/")
    suspend fun getCities(): Response<List<City>>

    companion object {
        const val BASE_URL = "https://codisecore-production.up.railway.app/"
        private var instance: ApiService? = null

        fun getInstance(context: Context): ApiService {
            return instance ?: synchronized(this) {
                val cacheSize = (5 * 1024 * 1024).toLong() // 5 MB
                val cache = Cache(context.cacheDir, cacheSize)

                val okHttpClient = OkHttpClient.Builder()
                    .cache(cache)
                    .build()

                Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
                    .also { instance = it }
            }
        }
    }
}

data class LoginRequest(
    val username: String, // Can be username or email
    val password: String
)
