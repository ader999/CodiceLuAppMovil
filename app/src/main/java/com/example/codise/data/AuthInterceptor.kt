package com.example.codise.data

import android.content.Context
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.net.HttpURLConnection

class AuthInterceptor(context: Context) : Interceptor {
    private val sessionManager = SessionManager.getInstance(context)
    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            synchronized(this) {
                val currentSession = sessionManager.getSession()
                val refreshToken = currentSession?.tokens?.refresh

                if (refreshToken != null) {
                    val newAccessToken = refreshAccessToken(refreshToken)
                    if (newAccessToken != null) {
                        // Save new token
                        val updatedSession = currentSession.copy(
                            tokens = currentSession.tokens.copy(access = newAccessToken)
                        )
                        sessionManager.saveSession(updatedSession)

                        // Close previous response and retry
                        response.close()
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()
                        return chain.proceed(newRequest)
                    }
                }
                
                // If we reach here, refresh failed or no token
                sessionManager.clearSession()
            }
        }

        return response
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        val client = OkHttpClient()
        val bodyJson = gson.toJson(mapOf("refresh" to refreshToken))
        val body = bodyJson.toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("${ApiService.BASE_URL}api/auth/login/refresh/") // Adjust based on backend
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val refreshResponse = gson.fromJson(responseBody, TokenRefreshResponse::class.java)
                refreshResponse.access
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
