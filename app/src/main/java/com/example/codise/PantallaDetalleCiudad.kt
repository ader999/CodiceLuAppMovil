package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.codise.data.Ciudad
import com.example.codise.data.Circuito
import com.example.codise.data.ItemGaleria
import com.example.codise.ui.theme.AzulPetroleo
import com.example.codise.ui.theme.Codice路Theme
import com.example.codise.ui.theme.GoldColor
import com.example.codise.ui.theme.NegroPuro
import com.example.codise.utils.extraerIdVideoYoutube
import com.example.codise.utils.aUrlCompleta
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun PantallaDetalleCiudad(
    ciudad: Ciudad,
    alRegresar: () -> Unit
) {
    var idVideoSeleccionado by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado con Botón de Regresar e Imagen (PORTADA)
        Box(modifier = Modifier.height(250.dp)) {
            if (ciudad.imagenPortada != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ciudad.imagenPortada.aUrlCompleta(),
                        contentDescription = "Imagen de portada de ${ciudad.nombre}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Degradado superpuesto para mejor contraste
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AzulPetroleo.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = AzulPetroleo.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Imagen de portada no disponible", color = AzulPetroleo, fontSize = 14.sp)
                    }
                }
            }

            // Botón de Regresar
            IconButton(
                onClick = alRegresar,
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp)
                    .statusBarsPadding()
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = ciudad.nombre,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AzulPetroleo
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Descripción",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = AzulPetroleo
            )
            Text(
                text = ciudad.descripcion,
                fontSize = 16.sp,
                color = NegroPuro,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (ciudad.galeria.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Galería Multimedia",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulPetroleo
                )

                if (idVideoSeleccionado != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            key(idVideoSeleccionado) {
                                ReproductorYouTube(
                                    idVideo = idVideoSeleccionado!!,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            IconButton(
                                onClick = { idVideoSeleccionado = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                    .size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cerrar video",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                val galeriaMezclada = remember(ciudad.galeria) { ciudad.galeria.shuffled() }
                CarruselGaleria(galeria = galeriaMezclada) { elemento ->
                    elemento.videoUrl?.let { videoUrl ->
                        extraerIdVideoYoutube(videoUrl)?.let { videoId ->
                            idVideoSeleccionado = videoId
                        }
                    }
                }
            }

            if (ciudad.datosHistoricos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Datos Históricos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulPetroleo
                )
                ciudad.datosHistoricos.forEach { datoHistorico ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = datoHistorico.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Época: ${datoHistorico.epocaOAno}", fontSize = 12.sp, color = GoldColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = datoHistorico.contenido, fontSize = 14.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ReproductorYouTube(
    idVideo: String,
    modifier: Modifier = Modifier
) {
    val propietarioCicloVida = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            YouTubePlayerView(ctx).apply {
                propietarioCicloVida.lifecycle.addObserver(this)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(idVideo, 0f)
                    }
                })
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaDetalleCiudad() {
    Codice路Theme {
        PantallaDetalleCiudad(
            ciudad = Ciudad(
                id = 1,
                nombre = "León",
                descripcion = "León es una ciudad de Nicaragua conocida por su arquitectura colonial, su vibrante escena universitaria y sus iglesias históricas.",
                imagenPortada = "https://example.com/image.jpg",
                latitudCentro = 0.0,
                longitudCentro = 0.0,
                circuitos = listOf(
                    Circuito(
                        id = 1,
                        ciudad = 1,
                        ciudadNombre = "León",
                        nombre = "Circuito Colonial",
                        descripcion = "Recorrido por las principales iglesias y edificios coloniales del centro histórico.",
                        distanciaKm = "5.2",
                        duracionEstimada = "2 horas",
                        dificultad = "Baja",
                        imagenMapa = null,
                        puntosInteres = emptyList()
                    )
                ),
                datosHistoricos = emptyList(),
                galeria = listOf(
                    ItemGaleria(1, 1, null, "Catedral de León", "Imagen", "https://example.com/cat.jpg", null),
                    ItemGaleria(2, 1, null, "Documental León", "Video", null, "https://youtube.com/watch?v=123")
                )
            ),
            alRegresar = {}
        )
    }
}
