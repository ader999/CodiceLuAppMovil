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

    @POST("api/auth/google/")
    suspend fun autenticarConGoogle(@Body solicitud: SolicitudAuthGoogle): Response<RespuestaAutenticacion>

    @GET("api/auth/me/")
    suspend fun obtenerPerfil(
        @Header("Authorization") token: String
    ): Response<Usuario>

    @PATCH("api/auth/me/")
    suspend fun actualizarPerfil(
        @Header("Authorization") token: String,
        @Body usuario: Usuario
    ): Response<Usuario>

    @Multipart
    @PATCH("api/auth/me/")
    suspend fun actualizarFotoPerfil(
        @Header("Authorization") token: String,
        @Part foto_perfil: MultipartBody.Part
    ): Response<Usuario>

    @Multipart
    @PATCH("api/auth/me/")
    suspend fun actualizarPerfilMultipart(
        @Header("Authorization") token: String,
        @Part("first_name") nombre: RequestBody? = null,
        @Part("last_name") apellido: RequestBody? = null,
        @Part("username") nombreUsuario: RequestBody? = null,
        @Part("email") correoElectronico: RequestBody? = null,
        @Part("telefono") telefono: RequestBody? = null,
        @Part foto_perfil: MultipartBody.Part? = null
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

    @Multipart
    @POST("api/eventos/")
    suspend fun crearEventoMultipart(
        @Header("Authorization") token: String,
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("ciudad") ciudad: RequestBody,
        @Part("empresa") empresa: RequestBody? = null,
        @Part("fecha_inicio") fechaInicio: RequestBody,
        @Part("fecha_fin") fechaFin: RequestBody,
        @Part("ubicacion") ubicacion: RequestBody,
        @Part("precio_entrada") precioEntrada: RequestBody,
        @Part("es_gratuito") esGratuito: RequestBody,
        @Part("cupo_maximo") cupoMaximo: RequestBody? = null,
        @Part("latitud") latitud: RequestBody? = null,
        @Part("longitud") longitud: RequestBody? = null,
        @Part("esta_activo") estaActivo: RequestBody? = null,
        @Part imagen: MultipartBody.Part? = null
    ): Response<Evento>

    @POST("api/eventos/{id}/asistir/")
    suspend fun registrarAsistencia(
        @Header("Authorization") token: String,
        @Path("id") idEvento: Int
    ): Response<RespuestaAsistencia>

    @GET("api/empresas/")
    suspend fun obtenerEmpresas(
        @Query("usuario") idUsuario: Int? = null
    ): Response<List<Empresa>>

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

    @GET("api/publicaciones/{id}/comentarios/")
    suspend fun obtenerComentarios(
        @Path("id") idPublicacion: Int
    ): Response<List<ComentarioPublicacion>>

    @POST("api/publicaciones/{id}/comentarios/")
    suspend fun agregarComentario(
        @Header("Authorization") token: String,
        @Path("id") idPublicacion: Int,
        @Body solicitud: SolicitudComentario
    ): Response<ComentarioPublicacion>

    @POST("api/asistente/chat/")
    suspend fun enviarMensajeAsistente(
        @Header("Authorization") token: String? = null,
        @Body solicitud: SolicitudAsistente
    ): Response<RespuestaAsistente>

    companion object {
        const val URL_BASE = "https://codisecore-production.up.railway.app/"
        private var instancia: ServicioApi? = null
        private var cacheOkHttp: Cache? = null

        fun limpiarCache() {
            try {
                cacheOkHttp?.evictAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun obtenerInstancia(contexto: Context): ServicioApi {
            return instancia ?: synchronized(this) {
                val tamanoCache = (5 * 1024 * 1024).toLong() // 5 MB
                val cache = Cache(contexto.cacheDir, tamanoCache)
                cacheOkHttp = cache

                val okHttpClient = OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(InterceptorIdioma(contexto))
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

data class SolicitudAuthGoogle(
    @com.google.gson.annotations.SerializedName("id_token") val idToken: String,
    @com.google.gson.annotations.SerializedName("credential") val credencial: String = idToken
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

data class UbicacionGps(
    val latitud: Double,
    val longitud: Double
)

data class ChatHistoryItem(
    val role: String,
    val parts: List<String>
)

data class SolicitudAsistente(
    val mensaje: String,
    val idioma: String = "es",
    val ubicacion: UbicacionGps? = null,
    val historial: List<ChatHistoryItem>? = null
)

data class HerramientaUtilizada(
    val nombre: String? = null,
    val argumentos: Map<String, Any?>? = null
)

data class RespuestaAsistente(
    val nombre_asistente: String? = null,
    val respuesta: String,
    val herramientas_utilizadas: List<HerramientaUtilizada>? = null,
    val modelo_utilizado: String? = null,
    val idioma: String? = null,
    val puntos_interes_ids: List<Int>? = null,
    val puntos_ids: List<Int>? = null
) {
    fun obtenerPuntosIds(): List<Int> {
        val ids = mutableListOf<Int>()
        puntos_interes_ids?.let { ids.addAll(it) }
        puntos_ids?.let { ids.addAll(it) }
        return ids.distinct()
    }
}
