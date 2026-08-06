package com.example.codise.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Header
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}

data class LoginRequest(
    val username: String, // Can be username or email
    val password: String
)
