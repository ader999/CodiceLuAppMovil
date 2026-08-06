package com.example.codise.data

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int? = null,
    val username: String,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("es_protagonista") val esProtagonista: Boolean,
    @SerializedName("es_turista") val esTurista: Boolean,
    val telefono: String,
    @SerializedName("foto_perfil") val fotoPerfil: String? = null,
    @SerializedName("is_staff") val isStaff: Boolean = false,
    @SerializedName("is_active") val isActive: Boolean = true,
    val password: String? = null,
    @SerializedName("password_confirm") val passwordConfirm: String? = null
)

data class AuthResponse(
    val message: String,
    val user: User,
    val tokens: Tokens
)

data class Tokens(
    val refresh: String,
    val access: String
)
