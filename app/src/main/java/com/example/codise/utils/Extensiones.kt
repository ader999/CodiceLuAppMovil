package com.example.codise.utils

import com.example.codise.data.ServicioApi

fun String.aUrlCompleta(): String {
    return if (this.startsWith("/")) {
        ServicioApi.URL_BASE.removeSuffix("/") + this
    } else {
        this
    }
}

fun extraerIdVideoYoutube(url: String): String? {
    val patron = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
    val patronCompilado = java.util.regex.Pattern.compile(patron)
    val emparejador = patronCompilado.matcher(url)
    return if (emparejador.find()) {
        emparejador.group()
    } else {
        null
    }
}

fun obtenerUrlMiniaturaYoutube(idVideo: String): String {
    return "https://img.youtube.com/vi/$idVideo/hqdefault.jpg"
}
