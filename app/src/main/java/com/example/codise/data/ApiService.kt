package com.example.codise.data

import android.content.Context
import okhttp3.Cache
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

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

    @POST("api/visitas/")
    suspend fun registerVisit(
        @Header("Authorization") token: String,
        @Body visit: VisitRequest
    ): Response<VisitResponse>

    @GET("api/visitas/ids/")
    suspend fun getVisitedIds(
        @Header("Authorization") token: String
    ): Response<List<Int>>

    @GET("api/visitas/")
    suspend fun getVisits(
        @Header("Authorization") token: String
    ): Response<List<VisitResponse>>

    @GET("api/eventos/")
    suspend fun getEvents(): Response<List<Event>>

    @POST("api/eventos/")
    suspend fun createEvent(
        @Header("Authorization") token: String,
        @Body event: EventRequest
    ): Response<Event>

    @POST("api/eventos/{id}/asistir/")
    suspend fun registerAttendance(
        @Header("Authorization") token: String,
        @Path("id") eventId: Int
    ): Response<AttendanceResponse>

    @POST("api/empresas/")
    suspend fun registerBusiness(
        @Header("Authorization") token: String,
        @Body business: Business
    ): Response<Business>

    @GET("api/publicaciones/")
    suspend fun getPublications(
        @Header("Authorization") token: String? = null,
        @Query("evento") eventId: Int? = null,
        @Query("ciudad") cityId: Int? = null,
        @Query("empresa") businessId: Int? = null,
        @Query("autor") authorId: Int? = null
    ): Response<List<Publication>>

    @Multipart
    @POST("api/publicaciones/")
    suspend fun createPublication(
        @Header("Authorization") token: String,
        @Part("descripcion") description: RequestBody,
        @Part("ciudad") cityId: Int? = null,
        @Part("empresa") businessId: Int? = null,
        @Part("evento") eventId: Int? = null,
        @Part("esta_activa") estaActiva: RequestBody? = null,
        @Part imagen_principal: MultipartBody.Part? = null,
        @Part imagenes: List<MultipartBody.Part>? = null
    ): Response<Publication>

    @POST("api/publicaciones/{id}/like/")
    suspend fun toggleLike(
        @Header("Authorization") token: String,
        @Path("id") publicationId: Int
    ): Response<LikeResponse>

    companion object {
        const val BASE_URL = "https://codisecore-production.up.railway.app/"
        private var instance: ApiService? = null

        fun getInstance(context: Context): ApiService {
            return instance ?: synchronized(this) {
                val cacheSize = (5 * 1024 * 1024).toLong() // 5 MB
                val cache = Cache(context.cacheDir, cacheSize)

                val okHttpClient = OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(AuthInterceptor(context))
                    .protocols(listOf(Protocol.HTTP_1_1))
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
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

data class AttendanceResponse(
    val message: String,
    val va_a_asistir: Boolean,
    val total_asistentes: Int
)

data class LikeResponse(
    val message: String,
    val ha_dado_like: Boolean,
    val total_likes: Int
)
