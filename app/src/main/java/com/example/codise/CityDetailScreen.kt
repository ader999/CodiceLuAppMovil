package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.codise.data.City
import com.example.codise.ui.theme.AzulPetroleo
import com.example.codise.ui.theme.Codise路Theme
import com.example.codise.ui.theme.GoldColor
import com.example.codise.ui.theme.NegroPuro
import com.example.codise.data.GalleryItem
import com.example.codise.utils.extractYoutubeVideoId
import com.example.codise.utils.getYoutubeThumbnailUrl
import com.example.codise.utils.toFullUrl
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun CityDetailScreen(
    city: City
) {
    var selectedVideoId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header with Back Button and Image (PORTADA)
        Box(modifier = Modifier.height(250.dp)) {
            if (city.imagenPortada != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = city.imagenPortada.toFullUrl(),
                        contentDescription = "Imagen de portada de ${city.nombre}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay for better text contrast or just aesthetic
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
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = city.nombre,
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
                text = city.descripcion,
                fontSize = 16.sp,
                color = NegroPuro,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (city.galeria.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Galería Multimedia",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulPetroleo
                )

                if (selectedVideoId != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            key(selectedVideoId) {
                                YouTubePlayer(
                                    videoId = selectedVideoId!!,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            IconButton(
                                onClick = { selectedVideoId = null },
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
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    items(city.galeria) { item ->
                        GalleryItemCard(item) { videoUrl ->
                            extractYoutubeVideoId(videoUrl)?.let { videoId ->
                                selectedVideoId = videoId
                            }
                        }
                    }
                }
            }

            if (city.circuitos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Circuitos Turísticos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulPetroleo
                )
                city.circuitos.forEach { circuit ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = circuit.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = circuit.descripcion, fontSize = 14.sp)
                            Text(
                                text = "Distancia: ${circuit.distanciaKm} km | Duración: ${circuit.duracionEstimada}",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp),
                                color = AzulPetroleo
                            )
                        }
                    }
                }
            }

            if (city.datosHistoricos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Datos Históricos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulPetroleo
                )
                city.datosHistoricos.forEach { history ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = history.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Época: ${history.epocaOAno}", fontSize = 12.sp, color = GoldColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = history.contenido, fontSize = 14.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Video Player Dialog (REMOVED - now inline)
}

@Composable
fun YouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            YouTubePlayerView(ctx).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                })
            }
        }
    )
}

@Composable
fun GalleryItemCard(item: GalleryItem, onVideoClick: (String) -> Unit) {
    val displayImage = item.imagen?.toFullUrl() 
        ?: if (item.tipo == "Video" && item.videoUrl != null) {
            extractYoutubeVideoId(item.videoUrl)?.let { getYoutubeThumbnailUrl(it) }
        } else {
            null
        }

    Card(
        modifier = Modifier
            .size(160.dp, 100.dp)
            .clickable(enabled = item.videoUrl != null || item.imagen != null) { 
                item.videoUrl?.let { onVideoClick(it) } 
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (displayImage != null) {
                AsyncImage(
                    model = displayImage,
                    contentDescription = item.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = "Ver Video",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Overlay for videos to show play icon if image is also present
            if (item.tipo == "Video") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = "Ver Video",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Title overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Text(
                    text = item.titulo,
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CityDetailScreenPreview() {
    Codise路Theme {
        CityDetailScreen(
            city = City(
                id = 1,
                nombre = "León",
                descripcion = "León es una ciudad de Nicaragua conocida por su arquitectura colonial, su vibrante escena universitaria y sus iglesias históricas.",
                imagenPortada = "https://example.com/image.jpg",
                latitudCentro = 0.0,
                longitudCentro = 0.0,
                circuitos = listOf(
                    com.example.codise.data.Circuit(
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
                    GalleryItem(1, 1, null, "Catedral de León", "Imagen", "https://example.com/cat.jpg", null),
                    GalleryItem(2, 1, null, "Documental León", "Video", null, "https://youtube.com/watch?v=123")
                )
            )
        )
    }
}
