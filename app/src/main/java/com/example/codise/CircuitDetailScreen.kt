package com.example.codise

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Circuit
import com.example.codise.data.PointOfInterest
import com.example.codise.ui.theme.*
import com.example.codise.utils.toFullUrl

@Composable
fun CircuitDetailScreen(
    circuit: Circuit,
    visitedPoiIds: Set<Int>,
    onToggleVisited: (Int) -> Unit
) {
    val sortedPois = remember(circuit.puntosInteres) {
        circuit.puntosInteres.sortedBy { it.orden }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Celeste.copy(alpha = 0.1f))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Circuit Info Header
            item {
                CircuitHeader(circuit)
            }

            // POIs Header
            item {
                Text(
                    "Puntos del Recorrido",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulPetroleo,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Animated POI items
            itemsIndexed(sortedPois) { index, poi ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 100L)
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    PoiDetailCard(
                        poi = poi,
                        isVisited = visitedPoiIds.contains(poi.id),
                        onToggleVisited = { onToggleVisited(poi.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CircuitHeader(circuit: Circuit) {
    Column {
        if (circuit.imagenMapa != null) {
            AsyncImage(
                model = circuit.imagenMapa.toFullUrl(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = circuit.nombre,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AzulPetroleo
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = circuit.descripcion,
                fontSize = 16.sp,
                color = NegroPuro,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconText(Icons.Default.DirectionsWalk, "${circuit.distanciaKm} km", AzulPetroleo)
                IconText(Icons.Default.Timer, circuit.duracionEstimada, AzulPetroleo)
            }
        }
    }
}

@Composable
fun PoiDetailCard(
    poi: PointOfInterest,
    isVisited: Boolean,
    onToggleVisited: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Order Number
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isVisited) Color(0xFF4CAF50) else GoldColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVisited) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = poi.orden.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = poi.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulPetroleo,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = poi.descripcion,
                fontSize = 14.sp,
                color = NegroPuro.copy(alpha = 0.8f)
            )

            if (poi.galeria.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                GalleryCarousel(gallery = poi.galeria)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mark as Visited Button
                Button(
                    onClick = onToggleVisited,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVisited) Color(0xFF4CAF50).copy(alpha = 0.1f) else AzulPetroleo,
                        contentColor = if (isVisited) Color(0xFF4CAF50) else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = if (isVisited) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)) else null
                ) {
                    Icon(
                        if (isVisited) Icons.Default.CheckCircle else Icons.Default.Done,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isVisited) "Visitado" else "Ya lo visité", fontSize = 12.sp)
                }

                // How to get there Button
                Button(
                    onClick = {
                        val uri = Uri.parse("google.navigation:q=${poi.latitud},${poi.longitud}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${poi.latitud},${poi.longitud}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Directions, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cómo llegar", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun IconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
