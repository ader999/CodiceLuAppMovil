package com.example.codise.data

import com.google.gson.annotations.SerializedName

data class Usuario(
    val id: Int? = null,
    @SerializedName("username") val nombreUsuario: String,
    @SerializedName("email") val correoElectronico: String,
    @SerializedName("first_name") val nombre: String,
    @SerializedName("last_name") val apellido: String,
    @SerializedName("es_protagonista") val esProtagonista: Boolean,
    @SerializedName("es_turista") val esTurista: Boolean,
    val telefono: String,
    @SerializedName("foto_perfil") val fotoPerfil: String? = null,
    @SerializedName("is_staff") val esStaff: Boolean = false,
    @SerializedName("is_active") val estaActivo: Boolean = true,
    @SerializedName("password") val contrasena: String? = null,
    @SerializedName("password_confirm") val confirmarContrasena: String? = null
)

data class RespuestaAutenticacion(
    @SerializedName("message") val mensaje: String,
    @SerializedName("user") val usuario: Usuario,
    @SerializedName("tokens") val tokens: TokensAutenticacion
)

data class TokensAutenticacion(
    val refresh: String,
    val access: String
)

data class RespuestaRefrescoToken(
    @SerializedName("access") val acceso: String
)
