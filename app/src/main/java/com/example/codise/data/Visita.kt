package com.example.codise.data

import com.google.gson.annotations.SerializedName

data class SolicitudVisita(
    @SerializedName("punto_interes_id") val puntoInteresId: Int,
    @SerializedName("latitud_usuario") val latitud: Double?,
    @SerializedName("longitud_usuario") val longitud: Double?
)

data class RespuestaVisita(
    val id: Int,
    val usuario: Int,
    @SerializedName("punto_interes") val puntoInteresId: Int,
    @SerializedName("punto_interes_nombre") val nombrePuntoInteres: String,
    @SerializedName("fecha_visita") val fechaVisita: String,
    @SerializedName("es_validada") val esValidada: Boolean,
    @SerializedName("distancia_metros") val distanciaMetros: Double?
)
