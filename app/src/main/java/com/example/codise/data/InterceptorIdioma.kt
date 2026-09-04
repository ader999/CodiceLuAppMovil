package com.example.codise.data

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class InterceptorIdioma(contexto: Context) : Interceptor {
    private val gestorIdioma = GestorIdioma.obtenerInstancia(contexto)

    override fun intercept(chain: Interceptor.Chain): Response {
        val peticionOriginal = chain.request()
        val cabeceraIdioma = gestorIdioma.obtenerCabeceraAcceptLanguage()

        val peticionConIdioma = peticionOriginal.newBuilder()
            .header("Accept-Language", cabeceraIdioma)
            .build()

        return chain.proceed(peticionConIdioma)
    }
}
