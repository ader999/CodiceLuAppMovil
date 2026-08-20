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

class InterceptorAutenticacion(contexto: Context) : Interceptor {
    private val administradorSesion = AdministradorSesion.obtenerInstancia(contexto)
    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val peticionOriginal = chain.request()
        val respuesta = chain.proceed(peticionOriginal)

        if (respuesta.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            synchronized(this) {
                val sesionActual = administradorSesion.obtenerSesion()
                val tokenRefresco = sesionActual?.tokens?.refresh

                if (tokenRefresco != null) {
                    val nuevoTokenAcceso = refrescarTokenAcceso(tokenRefresco)
                    if (nuevoTokenAcceso != null) {
                        // Guardar nueva sesión con el token actualizado
                        val sesionActualizada = sesionActual.copy(
                            tokens = sesionActual.tokens.copy(access = nuevoTokenAcceso)
                        )
                        administradorSesion.guardarSesion(sesionActualizada)

                        // Cerrar respuesta anterior y reintentar
                        respuesta.close()
                        val nuevaPeticion = peticionOriginal.newBuilder()
                            .header("Authorization", "Bearer $nuevoTokenAcceso")
                            .build()
                        return chain.proceed(nuevaPeticion)
                    }
                }
                
                // Si falla el refresco o no hay token, limpiar sesión
                administradorSesion.cerrarSesion()
            }
        }

        return respuesta
    }

    private fun refrescarTokenAcceso(tokenRefresco: String): String? {
        val cliente = OkHttpClient()
        val cuerpoJson = gson.toJson(mapOf("refresh" to tokenRefresco))
        val cuerpo = cuerpoJson.toRequestBody("application/json".toMediaType())
        
        val peticion = Request.Builder()
            .url("${ServicioApi.URL_BASE}api/auth/login/refresh/")
            .post(cuerpo)
            .build()

        return try {
            val respuesta = cliente.newCall(peticion).execute()
            if (respuesta.isSuccessful) {
                val cuerpoRespuesta = respuesta.body?.string()
                val respuestaRefresco = gson.fromJson(cuerpoRespuesta, RespuestaRefrescoToken::class.java)
                respuestaRefresco.acceso
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
