package com.example.codise.data

import com.google.gson.annotations.SerializedName

data class Publication(
    val id: Int,
    val autor: Int,
    @SerializedName("autor_username") val autorUsername: String,
    @SerializedName("autor_foto_perfil") val autorFotoPerfil: String?,
    @SerializedName("es_protagonista") val esProtagonista: Boolean,
    val empresa: Int?,
    @SerializedName("empresa_nombre") val empresaNombre: String?,
    val ciudad: Int?,
    @SerializedName("ciudad_nombre") val ciudadNombre: String?,
    val evento: Int?,
    @SerializedName("evento_titulo") val eventoTitulo: String?,
    val descripcion: String,
    @SerializedName("imagen_principal") val imagenPrincipal: String?,
    @SerializedName("video_url") val videoUrl: String?,
    val imagenes: List<PublicationImage> = emptyList(),
    @SerializedName("total_likes") val totalLikes: Int,
    @SerializedName("user_ha_dado_like") val userHaDadoLike: Boolean,
    @SerializedName("esta_activa") val estaActiva: Boolean,
    @SerializedName("fecha_creacion") val fechaCreacion: String
)

data class PublicationImage(
    val id: Int,
    val imagen: String,
    @SerializedName("fecha_creacion") val fechaCreacion: String
)
