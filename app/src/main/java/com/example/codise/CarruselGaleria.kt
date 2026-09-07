package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.codise.data.ItemGaleria
import com.example.codise.utils.aUrlCompleta
import com.example.codise.utils.extraerIdVideoYoutube
import com.example.codise.utils.obtenerUrlMiniaturaYoutube

@Composable
fun CarruselGaleria(
    galeria: List<ItemGaleria>,
    modifier: Modifier = Modifier,
    alHacerClicEnElemento: ((ItemGaleria) -> Unit)? = null,
    alHacerClicEnVideo: ((ItemGaleria) -> Unit)? = null,
    alHacerClicEnImagen: ((List<String>, Int) -> Unit)? = null
) {
    if (galeria.isEmpty()) return

    val estadoPaginador = rememberPagerState(pageCount = { galeria.size })
    var mostrarVistaPrevia by remember { mutableStateOf(false) }
    var indiceInicialVistaPrevia by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        HorizontalPager(
            state = estadoPaginador,
            modifier = Modifier.fillMaxSize()
        ) { pagina ->
            val elemento = galeria[pagina]
            val imagenAMostrar = elemento.imagen?.aUrlCompleta()
                ?: if (elemento.esVideo && elemento.videoUrl != null) {
                    extraerIdVideoYoutube(elemento.videoUrl)?.let { obtenerUrlMiniaturaYoutube(it) }
                        ?: elemento.urlVideo
                } else if (elemento.esVideo) {
                    elemento.urlVideo
                } else {
                    null
                }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (elemento.esVideo) {
                            if (alHacerClicEnVideo != null) {
                                alHacerClicEnVideo(elemento)
                            } else if (alHacerClicEnElemento != null) {
                                alHacerClicEnElemento(elemento)
                            } else {
                                indiceInicialVistaPrevia = pagina
                                mostrarVistaPrevia = true
                            }
                        } else {
                            if (alHacerClicEnImagen != null) {
                                val imagenesUrls = galeria.filter { !it.esVideo || it.imagen != null }
                                    .mapNotNull { it.imagen?.aUrlCompleta() }
                                val urlActual = elemento.imagen?.aUrlCompleta()
                                val indice = if (urlActual != null) imagenesUrls.indexOf(urlActual).coerceAtLeast(0) else 0
                                alHacerClicEnImagen(imagenesUrls, indice)
                            } else if (alHacerClicEnElemento != null) {
                                alHacerClicEnElemento(elemento)
                            } else {
                                indiceInicialVistaPrevia = pagina
                                mostrarVistaPrevia = true
                            }
                        }
                    }
            ) {
                if (imagenAMostrar != null) {
                    AsyncImage(
                        model = imagenAMostrar,
                        contentDescription = elemento.titulo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Marcador de posición estilizado
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (elemento.esVideo) {
                                    Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                                } else {
                                    Brush.verticalGradient(listOf(Color(0xFF374151), Color(0xFF1F2937)))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (elemento.esVideo) Icons.Default.Videocam else Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                if (elemento.esVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir video",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                } else if (imagenAMostrar != null) {
                    // Indicador sutil de que la imagen se puede ampliar
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .padding(6.dp)
                    ) {
                        Icon(
                            Icons.Default.ZoomIn,
                            contentDescription = "Ampliar imagen",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Indicadores
        if (galeria.size > 1) {
            Row(
                Modifier
                    .height(32.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(galeria.size) { indice ->
                    val color =
                        if (estadoPaginador.currentPage == indice) Color.White else Color.White.copy(
                            alpha = 0.5f
                        )
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

    if (mostrarVistaPrevia) {
        DialogoVistaPreviaGaleria(
            galeria = galeria,
            paginaInicial = indiceInicialVistaPrevia,
            alCerrar = { mostrarVistaPrevia = false }
        )
    }
}
