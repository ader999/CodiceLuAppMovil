package com.example.codise.utils

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

object UtilidadesUbicacion {

    fun validarCoordenadas(lat: Double, lng: Double): Boolean {
        return lat in -90.0..90.0 && lng in -180.0..180.0
    }

    /**
     * Extrae latitud y longitud directamente de enlaces largos de Google Maps,
     * coordenadas directas en formato decimal o en grados minutos segundos (DMS).
     */
    fun extraerCoordenadasDirectas(texto: String): Pair<Double, Double>? {
        val entrada = texto.trim()
        if (entrada.isEmpty()) return null

        // 1. Patrón @lat,lng común en URLs de Google Maps (@12.136389,-86.251389)
        val patronArroba = Pattern.compile("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
        val matcherArroba = patronArroba.matcher(entrada)
        if (matcherArroba.find()) {
            val lat = matcherArroba.group(1)?.toDoubleOrNull()
            val lng = matcherArroba.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null && validarCoordenadas(lat, lng)) {
                return Pair(lat, lng)
            }
        }

        // 2. Patrón de parámetros q=, query=, ll=, destination=, daddr=, center= o dir//
        val patronParametros = Pattern.compile(
            "(?:[?&](?:q|query|ll|destination|daddr|center)=|dir//)(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"
        )
        val matcherParametros = patronParametros.matcher(entrada)
        if (matcherParametros.find()) {
            val lat = matcherParametros.group(1)?.toDoubleOrNull()
            val lng = matcherParametros.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null && validarCoordenadas(lat, lng)) {
                return Pair(lat, lng)
            }
        }

        // 3. Coordenadas decimales directas: "12.136389, -86.251389" o "geo:12.136389,-86.251389"
        val patronDecimal = Pattern.compile(
            "(?:geo:)?(-?\\d{1,2}(?:\\.\\d+)?)[,\\s]+(-?\\d{1,3}(?:\\.\\d+)?)"
        )
        val matcherDecimal = patronDecimal.matcher(entrada)
        if (matcherDecimal.find()) {
            val lat = matcherDecimal.group(1)?.toDoubleOrNull()
            val lng = matcherDecimal.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null && validarCoordenadas(lat, lng)) {
                return Pair(lat, lng)
            }
        }

        // 4. Formato DMS (Grados, Minutos, Segundos): ej. 12°08'11.0"N 86°15'05.0"W
        val patronDms = Pattern.compile(
            "(\\d{1,2})°(?:(\\d{1,2})')?(?:([\\d.]+)\")?\\s*([NSns])[\\s,]+(\\d{1,3})°(?:(\\d{1,2})')?(?:([\\d.]+)\")?\\s*([EOWeow])"
        )
        val matcherDms = patronDms.matcher(entrada)
        if (matcherDms.find()) {
            try {
                val dLat = matcherDms.group(1)!!.toDouble()
                val mLat = matcherDms.group(2)?.toDouble() ?: 0.0
                val sLat = matcherDms.group(3)?.toDouble() ?: 0.0
                val dirLat = matcherDms.group(4)!!
                var lat = dLat + (mLat / 60.0) + (sLat / 3600.0)
                if (dirLat.equals("S", ignoreCase = true)) lat = -lat

                val dLng = matcherDms.group(5)!!.toDouble()
                val mLng = matcherDms.group(6)?.toDouble() ?: 0.0
                val sLng = matcherDms.group(7)?.toDouble() ?: 0.0
                val dirLng = matcherDms.group(8)!!
                var lng = dLng + (mLng / 60.0) + (sLng / 3600.0)
                if (dirLng.equals("W", ignoreCase = true) || dirLng.equals("O", ignoreCase = true)) lng = -lng

                if (validarCoordenadas(lat, lng)) {
                    return Pair(lat, lng)
                }
            } catch (_: Exception) {}
        }

        return null
    }

    /**
     * Resuelve enlaces acortados de Google Maps (como maps.app.goo.gl o goo.gl/maps)
     * siguiendo las redirecciones HTTP para obtener la URL completa con coordenadas.
     */
    suspend fun resolverUrlCorta(urlString: String): String = withContext(Dispatchers.IO) {
        try {
            var urlActual = urlString
            var redirecciones = 0
            while (redirecciones < 6) {
                val url = URL(urlActual)
                val conexion = url.openConnection() as HttpURLConnection
                conexion.instanceFollowRedirects = false
                conexion.connectTimeout = 6000
                conexion.readTimeout = 6000
                conexion.requestMethod = "GET"
                conexion.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10)")
                conexion.connect()

                val codigo = conexion.responseCode
                if (codigo in 300..399) {
                    val nuevaUbicacion = conexion.getHeaderField("Location") ?: break
                    urlActual = if (nuevaUbicacion.startsWith("/")) {
                        val u = URL(urlActual)
                        "${u.protocol}://${u.host}$nuevaUbicacion"
                    } else {
                        nuevaUbicacion
                    }
                    redirecciones++
                } else {
                    break
                }
            }
            urlActual
        } catch (_: Exception) {
            urlString
        }
    }

    /**
     * Geocodifica una dirección o nombre de lugar a coordenadas latitud/longitud
     * usando Android Geocoder.
     */
    suspend fun geocodificarDireccion(
        context: Context,
        direccion: String,
        nombreCiudad: String? = null
    ): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context)
            val intentos = mutableListOf(direccion)

            if (!nombreCiudad.isNullOrBlank() && !direccion.contains(nombreCiudad, ignoreCase = true)) {
                intentos.add("$direccion, $nombreCiudad, Nicaragua")
            }
            if (!direccion.contains("Nicaragua", ignoreCase = true)) {
                intentos.add("$direccion, Nicaragua")
            }

            for (consulta in intentos) {
                @Suppress("DEPRECATION")
                val resultados = geocoder.getFromLocationName(consulta, 1)
                if (!resultados.isNullOrEmpty()) {
                    val lugar = resultados[0]
                    if (validarCoordenadas(lugar.latitude, lugar.longitude)) {
                        return@withContext Pair(lugar.latitude, lugar.longitude)
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Función unificada: intenta primero extracción directa (coordenadas o link largo),
     * si es enlace acortado lo resuelve, y si no, recurre a geocodificación de dirección.
     */
    suspend fun obtenerCoordenadas(
        context: Context,
        entrada: String,
        nombreCiudad: String? = null
    ): Pair<Double, Double>? {
        val textoLimpio = entrada.trim()
        if (textoLimpio.isEmpty()) return null

        // 1. Extracción directa rápida
        extraerCoordenadasDirectas(textoLimpio)?.let { return it }

        // 2. Si es URL acortada o cualquier URL
        if (textoLimpio.contains("maps.app.goo.gl") ||
            textoLimpio.contains("goo.gl/maps") ||
            textoLimpio.startsWith("http://") ||
            textoLimpio.startsWith("https://")
        ) {
            val urlResuelta = resolverUrlCorta(textoLimpio)
            extraerCoordenadasDirectas(urlResuelta)?.let { return it }
        }

        // 3. Si parece una dirección textual o nombre de lugar, geocodificar
        return geocodificarDireccion(context, textoLimpio, nombreCiudad)
    }
}
