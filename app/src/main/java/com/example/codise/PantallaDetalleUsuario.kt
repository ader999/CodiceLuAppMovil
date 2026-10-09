package com.example.codise

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.*
import com.example.codise.ui.theme.*
import com.example.codise.utils.LocalCadenas
import com.example.codise.utils.UtilidadesContacto
import com.example.codise.utils.aUrlCompleta

data class PerfilPublicoParametros(
    val autorId: Int,
    val autorUsername: String,
    val autorNombre: String? = null,
    val autorFoto: String? = null,
    val esEmpresa: Boolean = false,
    val empresaId: Int? = null,
    val empresaNombre: String? = null,
    val empresaImagen: String? = null,
    val ciudadNombre: String? = null,
    val esProtagonista: Boolean = false,
    val tipoAutor: String? = null
)

fun Publicacion.aPerfilParametros(): PerfilPublicoParametros {
    return PerfilPublicoParametros(
        autorId = autor,
        autorUsername = autorNombreUsuario,
        autorNombre = autorNombre,
        autorFoto = autorFotoPerfil,
        esEmpresa = esPublicacionEmpresa,
        empresaId = empresa ?: empresaId,
        empresaNombre = if (esPublicacionEmpresa) empresaNombre ?: nombreAutorAMostrar else empresaNombre,
        empresaImagen = if (esPublicacionEmpresa) empresaImagen ?: fotoAutorAMostrar else empresaImagen,
        ciudadNombre = ciudadNombre,
        esProtagonista = esProtagonista,
        tipoAutor = tipoAutor
    )
}

fun ComentarioPublicacion.aPerfilParametros(): PerfilPublicoParametros {
    return PerfilPublicoParametros(
        autorId = autor,
        autorUsername = autorNombreUsuario,
        autorNombre = autorNombre,
        autorFoto = autorFotoPerfil,
        esEmpresa = esComentarioEmpresa,
        empresaId = empresa ?: empresaId,
        empresaNombre = empresaNombre,
        empresaImagen = fotoAutorAMostrar,
        ciudadNombre = null,
        esProtagonista = false,
        tipoAutor = tipoAutor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleUsuario(
    parametros: PerfilPublicoParametros,
    viewModelPublicaciones: ViewModelPublicaciones,
    idEmpresaActiva: Int? = null,
    empresasUsuario: List<Empresa> = emptyList(),
    alRegresar: () -> Unit,
    paddingSuperior: Dp = 0.dp
) {
    BackHandler(onBack = alRegresar)

    val contexto = LocalContext.current
    val cadenas = LocalCadenas.current
    val servicioApi = remember { ServicioApi.obtenerInstancia(contexto) }
    val administradorSesion = remember { AdministradorSesion.obtenerInstancia(contexto) }
    val sesion by administradorSesion.sesion.collectAsState()
    val usuarioActualId = sesion?.usuario?.id

    var empresaDetalle by remember { mutableStateOf<Empresa?>(null) }
    var estaCargandoEmpresa by remember { mutableStateOf(parametros.esEmpresa) }

    var publicaciones by remember { mutableStateOf<List<Publicacion>>(emptyList()) }
    var estaCargandoPublicaciones by remember { mutableStateOf(true) }

    var publicacionParaEditar by remember { mutableStateOf<Publicacion?>(null) }
    var publicacionParaEliminar by remember { mutableStateOf<Publicacion?>(null) }
    var estaGuardandoEdicion by remember { mutableStateOf(false) }
    var estaEliminando by remember { mutableStateOf(false) }

    var imagenesVistaPrevia by remember { mutableStateOf<List<String>?>(null) }
    var paginaInicialVistaPrevia by remember { mutableIntStateOf(0) }
    var publicacionParaComentarios by remember { mutableStateOf<Publicacion?>(null) }

    LaunchedEffect(parametros) {
        val sesion = administradorSesion.obtenerSesion()
        val token = sesion?.let { "Bearer ${it.tokens.access}" }

        // 1. Cargar detalle de la empresa si es perfil empresarial
        if (parametros.esEmpresa) {
            estaCargandoEmpresa = true
            try {
                var empEncontrada: Empresa? = null
                val idEmpresa = parametros.empresaId
                if (idEmpresa != null) {
                    val resp = servicioApi.obtenerDetalleEmpresa(idEmpresa)
                    if (resp.isSuccessful && resp.body() != null) {
                        empEncontrada = resp.body()
                    }
                }
                if (empEncontrada == null) {
                    val respLista = servicioApi.obtenerEmpresas(parametros.autorId)
                    if (respLista.isSuccessful && !respLista.body().isNullOrEmpty()) {
                        empEncontrada = respLista.body()!!.firstOrNull { it.id == parametros.empresaId }
                            ?: respLista.body()!!.first()
                    }
                }
                empresaDetalle = empEncontrada
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                estaCargandoEmpresa = false
            }
        }

        // 2. Cargar publicaciones del usuario o de la empresa
        estaCargandoPublicaciones = true
        try {
            val respPub = servicioApi.obtenerPublicaciones(
                token = token,
                idEmpresaHeader = idEmpresaActiva?.toString(),
                idEmpresa = if (parametros.esEmpresa) parametros.empresaId else null,
                idAutor = if (!parametros.esEmpresa) parametros.autorId else null
            )
            if (respPub.isSuccessful && respPub.body() != null) {
                val todas = respPub.body()!!
                val filtradas = todas.filter { pub ->
                    if (parametros.esEmpresa && parametros.empresaId != null) {
                        pub.empresa == parametros.empresaId || pub.empresaId == parametros.empresaId || (pub.esPublicacionEmpresa && pub.autor == parametros.autorId)
                    } else {
                        pub.autor == parametros.autorId && !pub.esPublicacionEmpresa
                    }
                }
                publicaciones = if (filtradas.isNotEmpty()) filtradas else todas
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            estaCargandoPublicaciones = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Celeste)
    ) {
        // Barra superior con título
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = paddingSuperior + 10.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val titulo = if (parametros.esEmpresa) {
                    empresaDetalle?.nombre ?: parametros.empresaNombre ?: "Perfil de Empresa"
                } else {
                    parametros.autorNombre ?: parametros.autorUsername
                }
                Text(
                    text = titulo,
                    style = TitularPrincipal.copy(fontSize = 20.sp, lineHeight = 26.sp),
                    color = AzulPetroleo,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (parametros.esEmpresa) "Perfil de Empresa" else "@${parametros.autorUsername}",
                    fontSize = 12.sp,
                    color = AzulPetroleo.copy(alpha = 0.7f)
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // TARJETA DE PERFIL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Fila principal: Avatar y datos básicos
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val fotoAMostrar = if (parametros.esEmpresa) {
                                empresaDetalle?.imagenPortada ?: parametros.empresaImagen ?: parametros.autorFoto
                            } else {
                                parametros.autorFoto
                            }

                            if (fotoAMostrar != null) {
                                AsyncImage(
                                    model = fotoAMostrar.aUrlCompleta(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .border(2.5.dp, GoldColor, CircleShape)
                                        .background(GrisClaro.copy(alpha = 0.2f)),
                                    contentScale = ContentScale.Crop,
                                    error = androidx.compose.ui.graphics.painter.ColorPainter(AzulPetroleo.copy(alpha = 0.2f))
                                )
                            } else if (parametros.esEmpresa) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(AzulPetroleo.copy(alpha = 0.1f))
                                        .border(2.5.dp, GoldColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = "Empresa",
                                        modifier = Modifier.size(40.dp),
                                        tint = AzulPetroleo
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(AzulPetroleo.copy(alpha = 0.08f))
                                        .border(2.5.dp, GoldColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(68.dp),
                                        tint = AzulPetroleo.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                val nombreMostrar = if (parametros.esEmpresa) {
                                    empresaDetalle?.nombre ?: parametros.empresaNombre ?: parametros.autorUsername
                                } else {
                                    parametros.autorNombre ?: parametros.autorUsername
                                }
                                Text(
                                    text = nombreMostrar,
                                    style = SubtituloH2,
                                    color = AzulPetroleo
                                )

                                Text(
                                    text = "@${parametros.autorUsername}",
                                    fontSize = 13.sp,
                                    color = NegroPuro.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Etiquetas / Badges
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (parametros.esEmpresa) {
                                        Surface(
                                            color = GoldColor.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Empresa",
                                                fontSize = 11.sp,
                                                color = AzulPetroleo,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }

                                        val cat = empresaDetalle?.categoria
                                        if (!cat.isNullOrBlank()) {
                                            Surface(
                                                color = AzulPetroleo.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = cat,
                                                    fontSize = 11.sp,
                                                    color = AzulPetroleo,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        val esProt = parametros.esProtagonista
                                        Surface(
                                            color = if (esProt) GoldColor.copy(alpha = 0.2f) else Celeste.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (esProt) "Protagonista" else "Turista",
                                                fontSize = 11.sp,
                                                color = AzulPetroleo,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                val ciudadTexto = empresaDetalle?.ciudadNombre ?: parametros.ciudadNombre
                                if (!ciudadTexto.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = GoldColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = ciudadTexto,
                                            fontSize = 12.sp,
                                            color = NegroPuro.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }

                        // Descripción de la empresa o usuario
                        val descripcionTexto = empresaDetalle?.descripcion
                        if (!descripcionTexto.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = AzulPetroleo.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = descripcionTexto,
                                    style = CuerpoTexto,
                                    color = NegroPuro.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Dirección si es empresa
                        val direccionTexto = empresaDetalle?.direccion
                        if (!direccionTexto.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = AzulPetroleo.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = direccionTexto,
                                    fontSize = 12.sp,
                                    color = NegroPuro.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // SECCIÓN DE WHATSAPP PARA EMPRESAS
                        if (parametros.esEmpresa) {
                            Spacer(modifier = Modifier.height(14.dp))
                            val whatsappNumero = empresaDetalle?.whatsappEfectivo

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF25D366)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                                contentDescription = "WhatsApp",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Contacto WhatsApp",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF128C7E)
                                            )
                                            val textoNumero = if (!whatsappNumero.isNullOrBlank()) {
                                                whatsappNumero
                                            } else if (estaCargandoEmpresa) {
                                                "Cargando..."
                                            } else {
                                                "No disponible"
                                            }
                                            Text(
                                                text = textoNumero,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulPetroleo
                                            )
                                        }
                                    }

                                    if (!whatsappNumero.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                val nombreEmp = empresaDetalle?.nombre ?: parametros.empresaNombre ?: "su empresa"
                                                val mensaje = "Hola, vi su empresa $nombreEmp en Códice y me gustaría comunicarme con ustedes."
                                                val enlaceOpcional = empresaDetalle?.linkWhatsapp?.takeIf { it.isNotBlank() } ?: whatsappNumero
                                                UtilidadesContacto.abrirWhatsApp(contexto, enlaceOpcional, mensaje)
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF25D366),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Abrir WhatsApp",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Otros canales de contacto (Web y Correo)
                            val sitioWeb = empresaDetalle?.sitioWeb
                            val email = empresaDetalle?.emailContacto
                            if (!sitioWeb.isNullOrBlank() || !email.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!sitioWeb.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                try {
                                                    val url = if (!sitioWeb.startsWith("http://") && !sitioWeb.startsWith("https://")) "https://$sitioWeb" else sitioWeb
                                                    contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                                } catch (_: Exception) {}
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, AzulPetroleo.copy(alpha = 0.4f)),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp), tint = AzulPetroleo)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Sitio Web", fontSize = 12.sp, color = AzulPetroleo, maxLines = 1)
                                        }
                                    }
                                    if (!email.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                try {
                                                    contexto.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
                                                } catch (_: Exception) {}
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, AzulPetroleo.copy(alpha = 0.4f)),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = AzulPetroleo)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Correo", fontSize = 12.sp, color = AzulPetroleo, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TÍTULO SECCIÓN DE PUBLICACIONES
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Publicaciones",
                        style = SubtituloH2,
                        color = AzulPetroleo
                    )
                    Surface(
                        color = AzulPetroleo.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${publicaciones.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulPetroleo,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // LISTA O ESTADOS DE PUBLICACIONES
            if (estaCargandoPublicaciones) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GoldColor)
                    }
                }
            } else if (publicaciones.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = AzulPetroleo.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Sin publicaciones aún",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzulPetroleo
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Este usuario no ha compartido publicaciones recientemente.",
                                fontSize = 13.sp,
                                color = GrisClaro,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(publicaciones, key = { it.id }) { publicacion ->
                    val puedeEditarOEliminar = if (usuarioActualId == null) {
                        false
                    } else if (publicacion.esPublicacionEmpresa) {
                        val pubEmpresaId = publicacion.empresa ?: publicacion.empresaId
                        (idEmpresaActiva != null && idEmpresaActiva == pubEmpresaId) ||
                        (pubEmpresaId != null && empresasUsuario.any { it.id == pubEmpresaId }) ||
                        publicacion.autor == usuarioActualId
                    } else {
                        publicacion.autor == usuarioActualId
                    }

                    TarjetaPublicacion(
                        publicacion = publicacion,
                        alHacerClicEnLike = {
                            viewModelPublicaciones.alternarLike(publicacion.id, idEmpresaActiva)
                            publicaciones = publicaciones.map { item ->
                                if (item.id == publicacion.id) {
                                    val nuevoLike = !item.usuarioHaDadoLike
                                    item.copy(
                                        usuarioHaDadoLike = nuevoLike,
                                        totalLikes = if (nuevoLike) item.totalLikes + 1 else (item.totalLikes - 1).coerceAtLeast(0)
                                    )
                                } else item
                            }
                        },
                        alHacerClicEnComentar = {
                            publicacionParaComentarios = publicacion
                        },
                        alHacerClicEnImagen = { imagenes, index ->
                            imagenesVistaPrevia = imagenes
                            paginaInicialVistaPrevia = index
                        },
                        alHacerClicEnAutor = null,
                        puedeEditarOEliminar = puedeEditarOEliminar,
                        alActualizar = { pub -> publicacionParaEditar = pub },
                        alEliminar = { pub -> publicacionParaEliminar = pub }
                    )
                }
            }
        }
    }

    // Modal de vista previa de imagen en pantalla completa
    if (imagenesVistaPrevia != null) {
        DialogoVistaPreviaImagen(
            imagenes = imagenesVistaPrevia!!,
            paginaInicial = paginaInicialVistaPrevia,
            alCerrar = { imagenesVistaPrevia = null }
        )
    }

    // Modal Bottom Sheet para comentarios
    if (publicacionParaComentarios != null) {
        HojaComentariosPublicacion(
            publicacion = publicacionParaComentarios!!,
            viewModel = viewModelPublicaciones,
            idEmpresaActiva = idEmpresaActiva,
            alHacerClicEnAutor = null,
            alCerrar = { publicacionParaComentarios = null }
        )
    }

    if (publicacionParaEditar != null) {
        DialogoEditarPublicacion(
            publicacion = publicacionParaEditar!!,
            alCerrar = { publicacionParaEditar = null },
            alConfirmar = { nuevaDescripcion, nuevasImagenes ->
                val pub = publicacionParaEditar ?: return@DialogoEditarPublicacion
                estaGuardandoEdicion = true
                val ctxEmpresa = if (pub.esPublicacionEmpresa) (pub.empresa ?: pub.empresaId ?: idEmpresaActiva) else null
                viewModelPublicaciones.actualizarPublicacion(
                    idPublicacion = pub.id,
                    descripcion = nuevaDescripcion,
                    idCiudad = pub.ciudad,
                    urisImagenes = nuevasImagenes,
                    idEmpresaContexto = ctxEmpresa
                ) { exito, error, pubActualizada ->
                    estaGuardandoEdicion = false
                    if (exito && pubActualizada != null) {
                        publicaciones = publicaciones.map { if (it.id == pub.id) pubActualizada else it }
                        Toast.makeText(contexto, cadenas.publicacionActualizada, Toast.LENGTH_SHORT).show()
                        publicacionParaEditar = null
                    } else {
                        Toast.makeText(contexto, error ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            estaGuardando = estaGuardandoEdicion
        )
    }

    if (publicacionParaEliminar != null) {
        DialogoConfirmarEliminarPublicacion(
            publicacion = publicacionParaEliminar!!,
            alCerrar = { publicacionParaEliminar = null },
            alConfirmar = {
                val pub = publicacionParaEliminar ?: return@DialogoConfirmarEliminarPublicacion
                estaEliminando = true
                val ctxEmpresa = if (pub.esPublicacionEmpresa) (pub.empresa ?: pub.empresaId ?: idEmpresaActiva) else null
                viewModelPublicaciones.eliminarPublicacion(
                    idPublicacion = pub.id,
                    idEmpresaContexto = ctxEmpresa
                ) { exito, error ->
                    estaEliminando = false
                    if (exito) {
                        publicaciones = publicaciones.filter { it.id != pub.id }
                        Toast.makeText(contexto, cadenas.publicacionEliminada, Toast.LENGTH_SHORT).show()
                        publicacionParaEliminar = null
                    } else {
                        Toast.makeText(contexto, error ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            estaEliminando = estaEliminando
        )
    }
}
