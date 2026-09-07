package com.example.codise.data

import com.google.gson.annotations.SerializedName

data class Evento(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val ciudad: Int,
    @SerializedName("ciudad_nombre") val ciudadNombre: String,
    val empresa: Int?,
    @SerializedName("empresa_nombre") val empresaNombre: String?,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
    val ubicacion: String,
    @SerializedName("precio_entrada") val precioEntrada: String,
    @SerializedName("es_gratuito") val esGratuito: Boolean,
    @SerializedName("cupo_maximo") val cupoMaximo: Int?,
    val imagen: String?,
    val latitud: Double?,
    val longitud: Double?,
    @SerializedName("esta_activo") val estaActivo: Boolean = true,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    val galeria: List<ItemGaleria> = emptyList()
)

data class SolicitudEvento(
    val titulo: String,
    val descripcion: String,
    val ciudad: Int,
    val empresa: Int? = null,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
    val ubicacion: String,
    @SerializedName("precio_entrada") val precioEntrada: String = "0.00",
    @SerializedName("es_gratuito") val esGratuito: Boolean = true,
    @SerializedName("cupo_maximo") val cupoMaximo: Int? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    @SerializedName("esta_activo") val estaActivo: Boolean = true
)
