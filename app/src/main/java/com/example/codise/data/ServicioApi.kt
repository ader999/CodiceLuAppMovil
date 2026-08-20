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
import java.util.concurrent.TimeUnit

interface ServicioApi {
    @POST("api/auth/register/")
    suspend fun registrarUsuario(@Body usuario: Usuario): Response<RespuestaAutenticacion>

    @POST("api/auth/login/")
    suspend fun iniciarSesion(@Body credenciales: SolicitudLogin): Response<RespuestaAutenticacion>

    @PATCH("api/auth/profile/update/")
    suspend fun actualizarPerfil(
        @Header("Authorization") token: String,
        @Body usuario: Usuario
    ): Response<Usuario>

    @GET("api/ciudades/")
    suspend fun obtenerCiudades(): Response<List<Ciudad>>

    @POST("api/visitas/")
    suspend fun registrarVisita(
        @Header("Authorization") token: String,
        @Body visita: SolicitudVisita
    ): Response<RespuestaVisita>

    @GET("api/visitas/ids/")
    suspend fun obtenerIdsVisitados(
        @Header("Authorization") token: String
    ): Response<List<Int>>

    @GET("api/visitas/")
    suspend fun obtenerVisitas(
        @Header("Authorization") token: String
    ): Response<List<RespuestaVisita>>

    @GET("api/eventos/")
    suspend fun obtenerEventos(): Response<List<Evento>>

    @POST("api/eventos/")
    suspend fun crearEvento(
        @Header("Authorization") token: String,
        @Body evento: SolicitudEvento
    ): Response<Evento>

    @POST("api/eventos/{id}/asistir/")
    suspend fun registrarAsistencia(
        @Header("Authorization") token: String,
        @Path("id") idEvento: Int
    ): Response<RespuestaAsistencia>

    @POST("api/empresas/")
    suspend fun registrarEmpresa(
        @Header("Authorization") token: String,
        @Body empresa: Empresa
    ): Response<Empresa>

    @GET("api/publicaciones/")
    suspend fun obtenerPublicaciones(
        @Header("Authorization") token: String? = null,
        @Query("evento") idEvento: Int? = null,
        @Query("ciudad") idCiudad: Int? = null,
        @Query("empresa") idEmpresa: Int? = null,
        @Query("autor") idAutor: Int? = null
    ): Response<List<Publicacion>>

    @Multipart
    @POST("api/publicaciones/")
    suspend fun crearPublicacion(
        @Header("Authorization") token: String,
        @Part("descripcion") descripcion: RequestBody,
        @Part("ciudad") idCiudad: Int? = null,
        @Part("empresa") idEmpresa: Int? = null,
        @Part("evento") idEvento: Int? = null,
        @Part("esta_activa") estaActiva: RequestBody? = null,
        @Part imagen_principal: MultipartBody.Part? = null,
        @Part imagenes: List<MultipartBody.Part>? = null
    ): Response<Publicacion>

    @POST("api/publicaciones/{id}/like/")
    suspend fun alternarLike(
        @Header("Authorization") token: String,
        @Path("id") idPublicacion: Int
    ): Response<RespuestaLike>

    companion object {
        const val URL_BASE = "https://codisecore-production.up.railway.app/"
        private var instancia: ServicioApi? = null

        fun obtenerInstancia(contexto: Context): ServicioApi {
            return instancia ?: synchronized(this) {
                val tamanoCache = (5 * 1024 * 1024).toLong() // 5 MB
                val cache = Cache(contexto.cacheDir, tamanoCache)

                val okHttpClient = OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(InterceptorAutenticacion(contexto))
                    .protocols(listOf(Protocol.HTTP_1_1))
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()

                Retrofit.Builder()
                    .baseUrl(URL_BASE)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ServicioApi::class.java)
                    .also { instancia = it }
            }
        }
    }
}

data class SolicitudLogin(
    @com.google.gson.annotations.SerializedName("username") val nombreUsuario: String,
    @com.google.gson.annotations.SerializedName("password") val contrasena: String
)

data class RespuestaAsistencia(
    val message: String,
    val va_a_asistir: Boolean,
    val total_asistentes: Int
)

data class RespuestaLike(
    val message: String,
    val ha_dado_like: Boolean,
    val total_likes: Int
)
