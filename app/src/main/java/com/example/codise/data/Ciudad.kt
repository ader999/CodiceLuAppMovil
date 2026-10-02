package com.example.codise.data

import com.example.codise.utils.aUrlCompleta
import com.google.gson.annotations.SerializedName

data class Ciudad(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    @SerializedName("imagen_portada") val imagenPortada: String?,
    @SerializedName("latitud_centro") val latitudCentro: Double,
    @SerializedName("longitud_centro") val longitudCentro: Double,
    val circuitos: List<Circuito>,
    @SerializedName("datos_historicos") val datosHistoricos: List<DatoHistorico>,
    val galeria: List<ItemGaleria>
)

data class Circuito(
    val id: Int,
    val ciudad: Int,
    @SerializedName("ciudad_nombre") val ciudadNombre: String,
    val nombre: String,
    val descripcion: String,
    @SerializedName("distancia_km") val distanciaKm: String,
    @SerializedName("duracion_estimada") val duracionEstimada: String,
    val dificultad: String,
    @SerializedName("imagen_mapa") val imagenMapa: String?,
    @SerializedName("puntos_interes") val puntosInteres: List<PuntoInteres>,
    @SerializedName("empresas_en_ruta") val empresasEnRuta: List<EmpresaEnCircuito> = emptyList()
)

data class EmpresaEnCircuito(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val direccion: String?,
    @SerializedName("telefono_contacto") val telefonoContacto: String?,
    @SerializedName("numero_whatsapp") val numeroWhatsapp: String?,
    @SerializedName("link_whatsapp") val linkWhatsapp: String?,
    @SerializedName("email_contacto") val emailContacto: String?,
    @SerializedName("sitio_web") val sitioWeb: String?,
    @SerializedName("imagen_portada") val imagenPortada: String?,
    val ciudad: Int,
    @SerializedName("ciudad_nombre") val ciudadNombre: String,
    val latitud: Double?,
    val longitud: Double?,
    @SerializedName("acepta_inversiones") val aceptaInversiones: Boolean,
    @SerializedName("es_patrocinada") val esPatrocinada: Boolean,
    @SerializedName("en_ruta") val enRuta: Boolean,
    @SerializedName("distancia_metros") val distanciaMetros: Double?,
    @SerializedName("punto_cercano_nombre") val puntoCercanoNombre: String?
)

data class PuntoInteres(
    val id: Int,
    val circuito: Int,
    @SerializedName("circuito_nombre") val circuitoNombre: String,
    val nombre: String,
    val descripcion: String,
    val tipo: String,
    val orden: Int,
    val latitud: Double,
    val longitud: Double,
    @SerializedName("datos_historicos") val datosHistoricos: List<DatoHistorico>,
    val galeria: List<ItemGaleria>
)

data class DatoHistorico(
    val id: Int,
    val ciudad: Int?,
    @SerializedName("punto_interes") val puntoInteres: Int?,
    val titulo: String,
    val tipo: String,
    val contenido: String,
    @SerializedName("epoca_o_ano") val epocaOAno: String
)

data class ItemGaleria(
    val id: Int,
    val ciudad: Int?,
    @SerializedName("punto_interes") val puntoInteres: Int?,
    val titulo: String,
    val tipo: String = "Imagen",
    val imagen: String?,
    @SerializedName("video_url") val videoUrl: String? = null,
    val evento: Int? = null,
    @SerializedName("video_archivo") val videoArchivo: String? = null
) {
    val tieneVideo: Boolean
        get() = !videoArchivo.isNullOrBlank() || !videoUrl.isNullOrBlank()

    val esVideo: Boolean
        get() = tipo.equals("Video", ignoreCase = true) || tieneVideo

    val urlVideo: String?
        get() = videoArchivo?.aUrlCompleta() ?: videoUrl?.let { if (it.startsWith("/")) it.aUrlCompleta() else it }
}
