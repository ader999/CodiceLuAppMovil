package com.example.codise.data

import com.google.gson.annotations.SerializedName

data class Publicacion(
    val id: Int,
    val autor: Int,
    @SerializedName("autor_username") val autorNombreUsuario: String,
    @SerializedName("autor_nombre") val autorNombre: String? = null,
    @SerializedName("tipo_autor") val tipoAutor: String? = null,
    @SerializedName("autor_foto_perfil") val autorFotoPerfil: String?,
    @SerializedName("es_protagonista") val esProtagonista: Boolean,
    val empresa: Int?,
    @SerializedName("empresa_id") val empresaId: Int? = null,
    @SerializedName("empresa_nombre") val empresaNombre: String?,
    @SerializedName("empresa_imagen") val empresaImagen: String? = null,
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
) {
    val esPublicacionEmpresa: Boolean
        get() = tipoAutor.equals("EMPRESA", ignoreCase = true) || empresa != null

    val nombreAutorAMostrar: String
        get() = if (esPublicacionEmpresa && !empresaNombre.isNullOrBlank()) {
            empresaNombre
        } else if (!autorNombre.isNullOrBlank()) {
            autorNombre
        } else if (!empresaNombre.isNullOrBlank()) {
            empresaNombre
        } else {
            autorNombreUsuario
        }

    val fotoAutorAMostrar: String?
        get() = if (esPublicacionEmpresa) {
            empresaImagen ?: if (autorFotoPerfil != null && !autorFotoPerfil.contains("google_avatar") && !autorFotoPerfil.contains("perfil_")) autorFotoPerfil else null
        } else {
            autorFotoPerfil
        }
}

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
    @SerializedName("autor_nombre") val autorNombre: String? = null,
    @SerializedName("autor_foto_perfil") val autorFotoPerfil: String?,
    val empresa: Int? = null,
    @SerializedName("empresa_id") val empresaId: Int? = null,
    @SerializedName("empresa_nombre") val empresaNombre: String? = null,
    @SerializedName("tipo_autor") val tipoAutor: String? = null,
    val contenido: String,
    @SerializedName("esta_activo") val estaActivo: Boolean = true,
    @SerializedName("fecha_creacion") val fechaCreacion: String
) {
    val esComentarioEmpresa: Boolean
        get() = tipoAutor.equals("EMPRESA", ignoreCase = true) || empresa != null || empresaId != null

    val nombreAutorAMostrar: String
        get() = if (esComentarioEmpresa && !empresaNombre.isNullOrBlank()) {
            empresaNombre
        } else if (!autorNombre.isNullOrBlank()) {
            autorNombre
        } else {
            autorNombreUsuario
        }

    val fotoAutorAMostrar: String?
        get() = if (esComentarioEmpresa) {
            if (autorFotoPerfil != null && !autorFotoPerfil.contains("google_avatar") && !autorFotoPerfil.contains("perfil_")) autorFotoPerfil else null
        } else {
            autorFotoPerfil
        }
}

data class SolicitudComentario(
    val contenido: String,
    val empresa: Int? = null
)
