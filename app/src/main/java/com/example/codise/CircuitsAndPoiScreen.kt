package com.example.codise
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.codise.GalleryCarousel
import com.example.codise.data.HistoricalData
import com.example.codise.ui.theme.BlancoBase
import com.example.codise.ui.theme.NegroPuro
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codise.data.City
import com.example.codise.data.Circuit
import com.example.codise.data.GalleryItem
import com.example.codise.data.PointOfInterest
import com.example.codise.ui.theme.AzulPetroleo
import com.example.codise.ui.theme.GoldColor
import com.example.codise.ui.theme.Codise路Theme
import com.example.codise.utils.extractYoutubeVideoId
import com.example.codise.utils.getYoutubeThumbnailUrl
import com.example.codise.utils.toFullUrl

@Composable
fun CircuitsAndPoiScreen(
    city: City,
    selectedTab: Int,
    onVerMasClick: (Circuit) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
    ) {
        when (selectedTab) {
            0 -> CircuitsList(city.circuitos, onVerMasClick)
            1 -> PoisList(city.circuitos.flatMap { it.puntosInteres })
        }
    }
}

@Composable
fun CircuitsList(circuitos: List<Circuit>, onVerMasClick: (Circuit) -> Unit) {
    if (circuitos.isEmpty()) {
        EmptyState("No hay circuitos disponibles.")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(circuitos) { circuit ->
                CircuitCard(circuit = circuit, onVerMasClick = onVerMasClick)
            }
        }
    }
}

@Composable
fun PoisList(pois: List<PointOfInterest>) {
    if (pois.isEmpty()) {
        EmptyState("No hay puntos de interés disponibles.")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(pois) { poi ->
                PoiCard(poi = poi)
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = AzulPetroleo)
    }
}

@Composable
fun CircuitCard(circuit: Circuit, onVerMasClick: (Circuit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (circuit.imagenMapa != null) {
                AsyncImage(
                    model = circuit.imagenMapa.toFullUrl(),
                    contentDescription = "Imagen de ${circuit.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(AzulPetroleo.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = AzulPetroleo.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = circuit.nombre,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = GoldColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = circuit.descripcion,
                    fontSize = 14.sp,
                    color = NegroPuro.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircuitBadge(
                        icon = Icons.Default.DirectionsWalk,
                        text = "${circuit.distanciaKm} km",
                        backgroundColor = Color(0xFFE3F2FD),
                        contentColor = Color(0xFF1976D2)
                    )
                    CircuitBadge(
                        icon = Icons.Default.Timer,
                        text = circuit.duracionEstimada,
                        backgroundColor = Color(0xFFFFF3E0),
                        contentColor = Color(0xFFF57C00)
                    )
                    CircuitBadge(
                        icon = null,
                        text = circuit.dificultad,
                        backgroundColor = when(circuit.dificultad.lowercase()) {
                            "baja" -> Color(0xFFE8F5E9)
                            "media" -> Color(0xFFFFFDE7)
                            else -> Color(0xFFFFEBEE)
                        },
                        contentColor = when(circuit.dificultad.lowercase()) {
                            "baja" -> Color(0xFF388E3C)
                            "media" -> Color(0xFFFBC02D)
                            else -> Color(0xFFD32F2F)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onVerMasClick(circuit) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ver más", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CircuitBadge(
    icon: ImageVector?,
    text: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun PoiCard(poi: PointOfInterest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (poi.galeria.isNotEmpty()) {
                GalleryCarousel(gallery = poi.galeria)
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = poi.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = when(poi.tipo.lowercase()) {
                            "historico" -> Color(0xFFFFF3E0)
                            "cultural" -> Color(0xFFE1F5FE)
                            else -> Color(0xFFF3E5F5)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = poi.tipo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when(poi.tipo.lowercase()) {
                                "historico" -> Color(0xFFE65100)
                                "cultural" -> Color(0xFF01579B)
                                else -> Color(0xFF4A148C)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn, 
                        contentDescription = null, 
                        tint = GoldColor, 
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = poi.circuitoNombre,
                        fontSize = 12.sp,
                        color = GoldColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = poi.descripcion,
                    fontSize = 14.sp,
                    color = NegroPuro.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
                
                if (poi.datosHistoricos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.HistoryEdu, 
                            contentDescription = null, 
                            tint = AzulPetroleo, 
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Dato Histórico",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulPetroleo
                        )
                    }
                    
                    poi.datosHistoricos.forEach { history ->
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = history.titulo,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NegroPuro
                            )
                            Text(
                                text = history.contenido,
                                fontSize = 12.sp,
                                color = NegroPuro.copy(alpha = 0.6f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                val context = LocalContext.current
                Button(
                    onClick = {
                        val uri = Uri.parse("geo:0,0?q=${poi.latitud},${poi.longitud}(${poi.nombre})")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${poi.latitud},${poi.longitud}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cómo llegar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircuitsAndPoiScreenPreview() {
    Codise路Theme {
        CircuitsAndPoiScreen(
            city = City(
                id = 1,
                nombre = "León",
                descripcion = "Ciudad universitaria",
                imagenPortada = null,
                latitudCentro = 0.0,
                longitudCentro = 0.0,
                datosHistoricos = emptyList(),
                galeria = emptyList(),
                circuitos = listOf(
                    Circuit(
                        id = 1,
                        ciudad = 1,
                        ciudadNombre = "León",
                        nombre = "Ruta de los Poetas",
                        descripcion = "Un recorrido caminando por la arquitectura colonial y murales históricos.",
                        distanciaKm = "3.2",
                        duracionEstimada = "2 horas",
                        dificultad = "Baja",
                        imagenMapa = "https://example.com/image.jpg",
                        puntosInteres = listOf(
                            PointOfInterest(
                                id = 1,
                                circuito = 1,
                                circuitoNombre = "Ruta de los Poetas",
                                nombre = "Catedral de León",
                                descripcion = "La catedral más grande de Centroamérica.",
                                tipo = "Historico",
                                orden = 1,
                                latitud = 0.0,
                                longitud = 0.0,
                                datosHistoricos = emptyList(),
                                galeria = listOf(
                                    GalleryItem(1, null, 1, "Fachada", "Imagen", "https://example.com/img1.jpg", null),
                                    GalleryItem(2, null, 1, "Video Tour", "Video", null, "https://youtube.com/watch?v=123")
                                )
                            )
                        )
                    )
                )
            ),
            selectedTab = 1,
            onVerMasClick = {}
        )
    }
}
