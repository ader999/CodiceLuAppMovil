package com.example.codise

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.codise.data.ItemGaleria
import com.example.codise.utils.extraerIdVideoYoutube
import com.example.codise.utils.obtenerUrlMiniaturaYoutube
import com.example.codise.utils.aUrlCompleta

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
    val contexto = LocalContext.current

    val imagenesUrls = remember(galeria) {
        galeria.filter { it.tipo != "Video" || it.imagen != null }
            .mapNotNull { it.imagen?.aUrlCompleta() }
    }
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
                ?: if (elemento.tipo == "Video" && elemento.videoUrl != null) {
                    extraerIdVideoYoutube(elemento.videoUrl)?.let { obtenerUrlMiniaturaYoutube(it) }
                } else {
                    null
                }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (elemento.tipo == "Video" && elemento.videoUrl != null) {
                            if (alHacerClicEnVideo != null) {
                                alHacerClicEnVideo(elemento)
                            } else if (alHacerClicEnElemento != null) {
                                alHacerClicEnElemento(elemento)
                            } else {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(elemento.videoUrl))
                                contexto.startActivity(intent)
                            }
                        } else {
                            val urlActual = elemento.imagen?.aUrlCompleta()
                            if (urlActual != null) {
                                val indice = imagenesUrls.indexOf(urlActual).coerceAtLeast(0)
                                if (alHacerClicEnImagen != null) {
                                    alHacerClicEnImagen(imagenesUrls, indice)
                                } else if (alHacerClicEnElemento != null) {
                                    alHacerClicEnElemento(elemento)
                                } else {
                                    indiceInicialVistaPrevia = indice
                                    mostrarVistaPrevia = true
                                }
                            } else {
                                alHacerClicEnElemento?.invoke(elemento)
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
                    // Marcador de posición si no hay imagen ni miniatura
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                if (elemento.tipo == "Video") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(48.dp)
                        )
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

    if (mostrarVistaPrevia && imagenesUrls.isNotEmpty()) {
        DialogoVistaPreviaImagen(
            imagenes = imagenesUrls,
            paginaInicial = indiceInicialVistaPrevia,
            alCerrar = { mostrarVistaPrevia = false }
        )
    }
}
