package com.example.codise

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Publicacion
import com.example.codise.ui.theme.*
import com.example.codise.utils.aUrlCompleta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPublicaciones(
    viewModel: ViewModelPublicaciones,
    alHacerClicEnSubir: () -> Unit
) {
    val estadoUi by viewModel.estadoUi
    var imagenesVistaPrevia by remember { mutableStateOf<List<String>?>(null) }
    var paginaInicialVistaPrevia by remember { mutableIntStateOf(0) }
    val estaRefrescando = estadoUi is EstadoUiPublicaciones.Cargando

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = alHacerClicEnSubir,
                containerColor = GoldColor,
                contentColor = AzulPetroleo
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Nueva Publicación")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = estaRefrescando,
            onRefresh = { viewModel.obtenerPublicaciones() },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (estadoUi) {
                    is EstadoUiPublicaciones.Cargando -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldColor)
                    }
                    is EstadoUiPublicaciones.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = (estadoUi as EstadoUiPublicaciones.Error).mensaje, color = Color.Red)
                            Button(onClick = { viewModel.obtenerPublicaciones() }, colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo)) {
                                Text("Reintentar")
                            }
                        }
                    }
                    is EstadoUiPublicaciones.Exito -> {
                        val publicaciones = (estadoUi as EstadoUiPublicaciones.Exito).publicaciones
                        if (publicaciones.isEmpty()) {
                            EstadoPublicacionesVacio()
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(publicaciones) { publicacion ->
                                    TarjetaPublicacion(
                                        publicacion = publicacion,
                                        alHacerClicEnLike = { viewModel.alternarLike(publicacion.id) },
                                        alHacerClicEnImagen = { imagenes, pagina ->
                                            imagenesVistaPrevia = imagenes
                                            paginaInicialVistaPrevia = pagina
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
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
}

@Composable
fun EstadoPublicacionesVacio() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
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
            text = "Aún no hay publicaciones",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AzulPetroleo
        )
        Text(
            text = "¡Sé el primero en compartir tu experiencia!",
            fontSize = 14.sp,
            color = GrisClaro
        )
    }
}

@Composable
fun TarjetaPublicacion(
    publicacion: Publicacion,
    alHacerClicEnLike: () -> Unit,
    alHacerClicEnImagen: (List<String>, Int) -> Unit
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
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (publicacion.autorFotoPerfil != null) {
                    AsyncImage(
                        model = publicacion.autorFotoPerfil.aUrlCompleta(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GrisClaro.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.graphics.painter.ColorPainter(AzulPetroleo.copy(alpha = 0.2f))
                    )
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
                    Text(
                        text = publicacion.autorNombreUsuario,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo,
                        fontSize = 15.sp
                    )
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

            // Acciones
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = alHacerClicEnLike) {
                    Icon(
                        imageVector = if (publicacion.usuarioHaDadoLike) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Me gusta",
                        tint = if (publicacion.usuarioHaDadoLike) Color.Red else AzulPetroleo
                    )
                }
                Text(
                    text = "${publicacion.totalLikes}",
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
                    color = NegroPuro.copy(alpha = 0.8f)
                )
            }
        }
    }
}
