package com.example.codise.data

import com.google.gson.annotations.SerializedName

data class Publicacion(
    val id: Int,
    val autor: Int,
    @SerializedName("autor_username") val autorNombreUsuario: String,
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
    val imagenes: List<ImagenPublicacion> = emptyList(),
    @SerializedName("total_likes") val totalLikes: Int,
    @SerializedName("user_ha_dado_like") val usuarioHaDadoLike: Boolean,
    @SerializedName("total_comentarios") val totalComentarios: Int = 0,
    val comentarios: List<ComentarioPublicacion> = emptyList(),
    @SerializedName("esta_activa") val estaActiva: Boolean,
    @SerializedName("fecha_creacion") val fechaCreacion: String
)

data class ImagenPublicacion(
    val id: Int,
    val imagen: String,
    @SerializedName("fecha_creacion") val fechaCreacion: String
)

data class ComentarioPublicacion(
    val id: Int,
    val publicacion: Int? = null,
    val autor: Int,
    @SerializedName("autor_username") val autorNombreUsuario: String,
    @SerializedName("autor_foto_perfil") val autorFotoPerfil: String?,
    val contenido: String,
    @SerializedName("esta_activo") val estaActivo: Boolean = true,
    @SerializedName("fecha_creacion") val fechaCreacion: String
)

data class SolicitudComentario(
    val contenido: String
)
