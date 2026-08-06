package com.example.codise.data

import com.google.gson.annotations.SerializedName

data class City(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    @SerializedName("imagen_portada") val imagenPortada: String?,
    @SerializedName("latitud_centro") val latitudCentro: Double,
    @SerializedName("longitud_centro") val longitudCentro: Double,
    val circuitos: List<Circuit>,
    @SerializedName("datos_historicos") val datosHistoricos: List<HistoricalData>,
    val galeria: List<GalleryItem>
)

data class Circuit(
    val id: Int,
    val ciudad: Int,
    @SerializedName("ciudad_nombre") val ciudadNombre: String,
    val nombre: String,
    val descripcion: String,
    @SerializedName("distancia_km") val distanciaKm: String,
    @SerializedName("duracion_estimada") val duracionEstimada: String,
    val dificultad: String,
    @SerializedName("imagen_mapa") val imagenMapa: String?,
    @SerializedName("puntos_interes") val puntosInteres: List<PointOfInterest>
)

data class PointOfInterest(
    val id: Int,
    val circuito: Int,
    @SerializedName("circuito_nombre") val circuitoNombre: String,
    val nombre: String,
    val descripcion: String,
    val tipo: String,
    val orden: Int,
    val latitud: Double,
    val longitud: Double,
    @SerializedName("datos_historicos") val datosHistoricos: List<HistoricalData>,
    val galeria: List<GalleryItem>
)

data class HistoricalData(
    val id: Int,
    val ciudad: Int?,
    @SerializedName("punto_interes") val puntoInteres: Int?,
    val titulo: String,
    val tipo: String,
    val contenido: String,
    @SerializedName("epoca_o_ano") val epocaOAno: String
)

data class GalleryItem(
    val id: Int,
    val ciudad: Int?,
    @SerializedName("punto_interes") val puntoInteres: Int?,
    val titulo: String,
    val tipo: String,
    val imagen: String?,
    @SerializedName("video_url") val videoUrl: String?
)
