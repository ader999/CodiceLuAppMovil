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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.example.codise.utils.ShareUtils
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Circuit
import com.example.codise.data.PointOfInterest
import com.example.codise.data.local.VisitedPoi
import com.example.codise.ui.theme.*
import com.example.codise.utils.toFullUrl

@Composable
fun CircuitDetailScreen(
    circuit: Circuit,
    visitedPois: List<VisitedPoi>,
    onToggleVisited: (Int) -> Unit
) {
    val sortedPois = remember(circuit.puntosInteres) {
        circuit.puntosInteres.sortedBy { it.orden }
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var sharingPoi by remember { mutableStateOf<Pair<PointOfInterest, Boolean>?>(null) }
    val graphicsLayer = rememberGraphicsLayer()

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

                val visitedRecord = visitedPois.find { it.poiId == poi.id }
                val isVisited = visitedRecord != null
                val isValidated = visitedRecord?.isValidated == true

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    PoiDetailCard(
                        poi = poi,
                        isVisited = isVisited,
                        isValidated = isValidated,
                        onToggleVisited = { onToggleVisited(poi.id) },
                        onShareClick = {
                            sharingPoi = poi to isValidated
                        }
                    )
                }
            }
        }

        // Hidden Shareable Card for capture
        if (sharingPoi != null) {
            Box(
                modifier = Modifier
                    .offset(y = (-1000).dp) // Off-screen
                    .wrapContentSize()
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                ShareableCard(
                    poi = sharingPoi!!.first,
                    isValidated = sharingPoi!!.second
                )
            }

            LaunchedEffect(sharingPoi) {
                coroutineScope.launch {
                    // Give it a frame to render
                    kotlinx.coroutines.delay(100)
                    if (graphicsLayer.size.width > 0 && graphicsLayer.size.height > 0) {
                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                        ShareUtils.shareBitmap(context, bitmap)
                        sharingPoi = null
                    }
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
    isValidated: Boolean,
    onToggleVisited: () -> Unit,
    onShareClick: () -> Unit
) {
    val context = LocalContext.current
    val statusColor = if (isValidated) GoldColor else if (isVisited) Color(0xFF4CAF50) else GoldColor

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
                        .background(statusColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVisited) {
                        Icon(
                            if (isValidated) Icons.Default.Verified else Icons.Default.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
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

                if (isValidated) {
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = AzulPetroleo)
                    }
                }
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
                val buttonColor = if (isValidated) GoldColor else if (isVisited) Color(0xFF4CAF50) else AzulPetroleo
                val contentColor = if (isVisited) buttonColor else Color.White
                val containerColor = if (isVisited) buttonColor.copy(alpha = 0.1f) else buttonColor

                Button(
                    onClick = onToggleVisited,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = if (isVisited) androidx.compose.foundation.BorderStroke(1.dp, buttonColor) else null
                ) {
                    Icon(
                        if (isValidated) Icons.Default.Verified else if (isVisited) Icons.Default.CheckCircle else Icons.Default.Done,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isValidated) "Verificado" else if (isVisited) "Visitado" else "Ya lo visité",
                        fontSize = 12.sp
                    )
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
fun ShareableCard(poi: PointOfInterest, isValidated: Boolean) {
    val brush = Brush.verticalGradient(listOf(AzulPetroleo, Color(0xFF1A5F7A)))
    
    Card(
        modifier = Modifier
            .width(350.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.background(brush)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡NUEVO LOGRO!",
                    color = GoldColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    AsyncImage(
                        model = poi.galeria.firstOrNull()?.imagen?.toFullUrl(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = poi.nombre,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (isValidated) GoldColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        if (isValidated) Icons.Default.Verified else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isValidated) GoldColor else Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isValidated) "VISITA VERIFICADA" else "VISITA REGISTRADA",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Explore,
                        contentDescription = null,
                        tint = GoldColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CODISE 路",
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
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
