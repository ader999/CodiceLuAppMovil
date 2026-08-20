package com.example.codise.data

import com.google.gson.annotations.SerializedName

data class Empresa(
    val id: Int? = null,
    val usuario: Int? = null,
    @SerializedName("usuario_username") val usuarioNombreUsuario: String? = null,
    val ciudad: Int,
    @SerializedName("ciudad_nombre") val ciudadNombre: String? = null,
    @SerializedName("punto_interes") val puntoInteres: Int? = null,
    @SerializedName("punto_interes_nombre") val puntoInteresNombre: String? = null,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val direccion: String,
    @SerializedName("telefono_contacto") val telefonoContacto: String,
    @SerializedName("email_contacto") val emailContacto: String,
    @SerializedName("sitio_web") val sitioWeb: String?,
    @SerializedName("imagen_portada") val imagenPortada: String? = null,
    val latitud: Double,
    val longitud: Double,
    @SerializedName("acepta_inversiones") val aceptaInversiones: Boolean,
    @SerializedName("fecha_creacion") val fechaCreacion: String? = null
)
