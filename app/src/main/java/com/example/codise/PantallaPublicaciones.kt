package com.example.codise

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.ComentarioPublicacion
import com.example.codise.data.Publicacion
import com.example.codise.ui.theme.*
import com.example.codise.utils.LocalCadenas
import com.example.codise.utils.aUrlCompleta
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPublicaciones(
    viewModel: ViewModelPublicaciones,
    idEmpresaActiva: Int? = null,
    alHacerClicEnSubir: () -> Unit = {},
    alHacerClicEnAutor: (Publicacion) -> Unit = {},
    alHacerClicEnAutorComentario: (ComentarioPublicacion) -> Unit = {},
    paddingSuperior: Dp = 0.dp
) {
    val cadenas = LocalCadenas.current
    val estadoUi by viewModel.estadoUi
    var imagenesVistaPrevia by remember { mutableStateOf<List<String>?>(null) }
    var paginaInicialVistaPrevia by remember { mutableIntStateOf(0) }
    var publicacionSeleccionadaParaComentarios by remember { mutableStateOf<Publicacion?>(null) }
    val estaRefrescando = estadoUi is EstadoUiPublicaciones.Cargando

    PullToRefreshBox(
        isRefreshing = estaRefrescando,
        onRefresh = { viewModel.obtenerPublicaciones(idEmpresaContexto = idEmpresaActiva) },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (estadoUi) {
                is EstadoUiPublicaciones.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldColor)
                }
                is EstadoUiPublicaciones.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = paddingSuperior)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = (estadoUi as EstadoUiPublicaciones.Error).mensaje,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.obtenerPublicaciones(idEmpresaContexto = idEmpresaActiva) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cadenas.reintentar, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                is EstadoUiPublicaciones.Exito -> {
                    val publicaciones = (estadoUi as EstadoUiPublicaciones.Exito).publicaciones
                    if (publicaciones.isEmpty()) {
                        EstadoPublicacionesVacio(modifier = Modifier.padding(top = paddingSuperior))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, top = paddingSuperior + 8.dp, end = 16.dp, bottom = 76.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(publicaciones) { publicacion ->
                                TarjetaPublicacion(
                                    publicacion = publicacion,
                                    alHacerClicEnLike = { viewModel.alternarLike(publicacion.id, idEmpresaActiva) },
                                    alHacerClicEnComentar = {
                                        publicacionSeleccionadaParaComentarios = publicacion
                                    },
                                    alHacerClicEnImagen = { imagenes, pagina ->
                                        imagenesVistaPrevia = imagenes
                                        paginaInicialVistaPrevia = pagina
                                    },
                                    alHacerClicEnAutor = alHacerClicEnAutor
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    if (imagenesVistaPrevia != null) {
        DialogoVistaPreviaImagen(
            imagenes = imagenesVistaPrevia!!,
            paginaInicial = paginaInicialVistaPrevia,
            alCerrar = { imagenesVistaPrevia = null }
        )
    }

    // Modal BottomSheet de Comentarios
    if (publicacionSeleccionadaParaComentarios != null) {
        val pubActual = (estadoUi as? EstadoUiPublicaciones.Exito)?.publicaciones?.find {
            it.id == publicacionSeleccionadaParaComentarios?.id
        } ?: publicacionSeleccionadaParaComentarios!!

        HojaComentariosPublicacion(
            publicacion = pubActual,
            viewModel = viewModel,
            idEmpresaActiva = idEmpresaActiva,
            alHacerClicEnAutor = alHacerClicEnAutorComentario,
            alCerrar = { publicacionSeleccionadaParaComentarios = null }
        )
    }
}

@Composable
fun EstadoPublicacionesVacio(modifier: Modifier = Modifier) {
    val cadenas = LocalCadenas.current
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = AzulPetroleo.copy(alpha = 0.3f),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = cadenas.sinPublicaciones,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AzulPetroleo
        )
        Text(
            text = cadenas.seElPrimeroPublicar,
            fontSize = 14.sp,
            color = GrisClaro
        )
    }
}

@Composable
fun TarjetaPublicacion(
    publicacion: Publicacion,
    alHacerClicEnLike: () -> Unit,
    alHacerClicEnComentar: () -> Unit,
    alHacerClicEnImagen: (List<String>, Int) -> Unit,
    alHacerClicEnAutor: ((Publicacion) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Encabezado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .then(
                        if (alHacerClicEnAutor != null) {
                            Modifier.clickable { alHacerClicEnAutor(publicacion) }
                        } else Modifier
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fotoAMostrar = publicacion.fotoAutorAMostrar
                if (fotoAMostrar != null) {
                    AsyncImage(
                        model = fotoAMostrar.aUrlCompleta(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GrisClaro.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.graphics.painter.ColorPainter(AzulPetroleo.copy(alpha = 0.2f))
                    )
                } else if (publicacion.esPublicacionEmpresa) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AzulPetroleo.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = "Empresa",
                            modifier = Modifier.size(24.dp),
                            tint = AzulPetroleo
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        null,
                        modifier = Modifier.size(40.dp),
                        tint = AzulPetroleo.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = publicacion.nombreAutorAMostrar,
                            fontWeight = FontWeight.Bold,
                            color = AzulPetroleo,
                            fontSize = 15.sp
                        )
                        if (publicacion.esPublicacionEmpresa) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = GoldColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Empresa",
                                    fontSize = 10.sp,
                                    color = AzulPetroleo,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (publicacion.ciudadNombre != null) {
                        Text(
                            text = publicacion.ciudadNombre,
                            fontSize = 12.sp,
                            color = GrisClaro
                        )
                    }
                }
                if (publicacion.eventoTitulo != null) {
                    Surface(
                        color = GoldColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Event, null, tint = GoldColor, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(publicacion.eventoTitulo, fontSize = 10.sp, color = GoldColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Carrusel de Imágenes
            val todasLasImagenes = remember(publicacion) {
                val lista = mutableListOf<String>()
                publicacion.imagenPrincipal?.let { lista.add(it.aUrlCompleta()) }
                publicacion.imagenes.forEach { lista.add(it.imagen.aUrlCompleta()) }
                lista
            }

            if (todasLasImagenes.isNotEmpty()) {
                val estadoPaginador = rememberPagerState(pageCount = { todasLasImagenes.size })
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    HorizontalPager(state = estadoPaginador, modifier = Modifier.fillMaxSize()) { pagina ->
                        AsyncImage(
                            model = todasLasImagenes[pagina],
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { alHacerClicEnImagen(todasLasImagenes, pagina) },
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    if (todasLasImagenes.size > 1) {
                        Row(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(todasLasImagenes.size) { iteracion ->
                                val color = if (estadoPaginador.currentPage == iteracion) Color.White else Color.White.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Acciones (Like y Comentarios)
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = alHacerClicEnLike) {
                    AnimatedContent(
                        targetState = publicacion.usuarioHaDadoLike,
                        transitionSpec = {
                            (scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn())
                                .togetherWith(scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeOut())
                        },
                        label = "AnimacionLike"
                    ) { dioLike ->
                        Icon(
                            painter = painterResource(
                                id = if (dioLike) R.drawable.ic_like_filled else R.drawable.ic_custom_like
                            ),
                            contentDescription = "Me gusta",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(31.dp)
                        )
                    }
                }
                Text(
                    text = "${publicacion.totalLikes}",
                    fontSize = 14.sp,
                    color = AzulPetroleo,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = alHacerClicEnComentar) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comentarios",
                        tint = AzulPetroleo
                    )
                }
                Text(
                    text = "${publicacion.totalComentarios}",
                    fontSize = 14.sp,
                    color = AzulPetroleo,
                    fontWeight = FontWeight.Medium
                )
            }

            // Descripción
            if (publicacion.descripcion.isNotEmpty()) {
                Text(
                    text = publicacion.descripcion,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    fontSize = 14.sp,
                    color = NegroPuro.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaComentariosPublicacion(
    publicacion: Publicacion,
    viewModel: ViewModelPublicaciones,
    idEmpresaActiva: Int? = null,
    alHacerClicEnAutor: ((ComentarioPublicacion) -> Unit)? = null,
    alCerrar: () -> Unit
) {
    val cadenas = LocalCadenas.current
    val contexto = LocalContext.current
    var textoComentario by remember { mutableStateOf("") }
    var estaEnviando by remember { mutableStateOf(false) }
    val estaAutenticado = viewModel.estaUsuarioAutenticado()

    LaunchedEffect(publicacion.id) {
        viewModel.cargarComentarios(publicacion.id)
    }

    ModalBottomSheet(
        onDismissRequest = alCerrar,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GrisClaro.copy(alpha = 0.6f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Título de la sección
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${cadenas.comentarios} (${publicacion.comentarios.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulPetroleo
                )
                IconButton(onClick = alCerrar) {
                    Icon(Icons.Default.Close, contentDescription = cadenas.cerrar, tint = AzulPetroleo)
                }
            }

            HorizontalDivider(color = GrisClaro.copy(alpha = 0.3f))

            // Lista de comentarios
            if (publicacion.comentarios.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = AzulPetroleo.copy(alpha = 0.3f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = cadenas.comentarios,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AzulPetroleo
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cadenas.seElPrimeroPublicar,
                            fontSize = 13.sp,
                            color = GrisClaro
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(publicacion.comentarios, key = { it.id }) { comentario ->
                        ElementoComentario(
                            comentario = comentario,
                            alHacerClicEnAutor = { com ->
                                alCerrar()
                                alHacerClicEnAutor?.invoke(com)
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = GrisClaro.copy(alpha = 0.3f))

            // Barra para escribir comentario
            if (estaAutenticado) {
                if (idEmpresaActiva != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = GoldColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Comentando como empresa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulPetroleo
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textoComentario,
                        onValueChange = { textoComentario = it },
                        placeholder = { Text(cadenas.escribeComentario, fontSize = 14.sp, color = GrisClaro) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = NegroPuro),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NegroPuro,
                            unfocusedTextColor = NegroPuro,
                            cursorColor = AzulPetroleo,
                            focusedBorderColor = AzulPetroleo,
                            unfocusedBorderColor = GrisClaro.copy(alpha = 0.5f),
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedPlaceholderColor = GrisClaro,
                            unfocusedPlaceholderColor = GrisClaro
                        ),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            if (textoComentario.isNotBlank() && !estaEnviando) {
                                estaEnviando = true
                                val textoAEnviar = textoComentario
                                viewModel.agregarComentario(publicacion.id, textoAEnviar, idEmpresaActiva) { exito, error ->
                                    estaEnviando = false
                                    if (exito) {
                                        textoComentario = ""
                                    } else {
                                        Toast.makeText(contexto, error ?: "Error al publicar comentario", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = textoComentario.isNotBlank() && !estaEnviando,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (textoComentario.isNotBlank()) AzulPetroleo else GrisClaro.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    ) {
                        if (estaEnviando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = GoldColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar comentario",
                                tint = if (textoComentario.isNotBlank()) GoldColor else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    color = AzulPetroleo.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = AzulPetroleo,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Inicia sesión para dejar un comentario en esta publicación.",
                            fontSize = 13.sp,
                            color = AzulPetroleo,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElementoComentario(
    comentario: ComentarioPublicacion,
    alHacerClicEnAutor: ((ComentarioPublicacion) -> Unit)? = null
) {
    val modifierClic = if (alHacerClicEnAutor != null) Modifier.clickable { alHacerClicEnAutor(comentario) } else Modifier

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        val fotoAMostrar = comentario.fotoAutorAMostrar
        if (fotoAMostrar != null) {
            AsyncImage(
                model = fotoAMostrar.aUrlCompleta(),
                contentDescription = null,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .then(modifierClic)
                    .background(GrisClaro.copy(alpha = 0.2f)),
                contentScale = ContentScale.Crop,
                error = androidx.compose.ui.graphics.painter.ColorPainter(AzulPetroleo.copy(alpha = 0.2f))
            )
        } else if (comentario.esComentarioEmpresa) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .then(modifierClic)
                    .background(AzulPetroleo.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = "Empresa",
                    modifier = Modifier.size(20.dp),
                    tint = AzulPetroleo
                )
            }
        } else {
            Icon(
                Icons.Default.AccountCircle,
                null,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .then(modifierClic),
                tint = AzulPetroleo.copy(alpha = 0.5f)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF6F8FA), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifierClic
                ) {
                    Text(
                        text = comentario.nombreAutorAMostrar,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AzulPetroleo
                    )
                    if (comentario.esComentarioEmpresa) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = GoldColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Empresa",
                                fontSize = 9.sp,
                                color = AzulPetroleo,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = formatearFechaComentario(comentario.fechaCreacion),
                    fontSize = 11.sp,
                    color = GrisClaro
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comentario.contenido,
                fontSize = 13.sp,
                color = NegroPuro.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )
        }
    }
}

private fun formatearFechaComentario(fechaIso: String): String {
    return try {
        val instant = Instant.parse(fechaIso)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val formateador = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("es", "ES"))
        zonedDateTime.format(formateador)
    } catch (e: Exception) {
        try {
            val offsetDateTime = OffsetDateTime.parse(fechaIso)
            val formateador = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("es", "ES"))
            offsetDateTime.format(formateador)
        } catch (e2: Exception) {
            fechaIso.take(16).replace("T", " ")
        }
    }
}
