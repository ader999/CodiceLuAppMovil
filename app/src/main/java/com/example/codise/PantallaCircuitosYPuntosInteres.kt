package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.codise.data.Ciudad
import com.example.codise.data.Circuito
import com.example.codise.data.ItemGaleria
import com.example.codise.data.PuntoInteres
import com.example.codise.ui.theme.AzulPetroleo
import com.example.codise.ui.theme.BlancoBase
import com.example.codise.ui.theme.GoldColor
import com.example.codise.ui.theme.NegroPuro
import com.example.codise.ui.theme.Codice路Theme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codise.utils.aUrlCompleta

@Composable
fun PantallaCircuitosYPuntos(
    ciudad: Ciudad,
    pestanaSeleccionada: Int,
    alHacerClicEnVerMas: (Circuito) -> Unit,
    paddingSuperior: Dp = 0.dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
    ) {
        when (pestanaSeleccionada) {
            0 -> ListaCircuitos(ciudad.circuitos, alHacerClicEnVerMas, paddingSuperior)
            1 -> ListaPuntosInteres(ciudad.circuitos.flatMap { it.puntosInteres }, paddingSuperior)
            else -> ListaCircuitos(ciudad.circuitos, alHacerClicEnVerMas, paddingSuperior)
        }
    }
}

@Composable
fun ListaCircuitos(
    circuitos: List<Circuito>,
    alHacerClicEnVerMas: (Circuito) -> Unit,
    paddingSuperior: Dp = 0.dp
) {
    if (circuitos.isEmpty()) {
        EstadoVacio("No hay circuitos disponibles.", Modifier.padding(top = paddingSuperior))
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, top = paddingSuperior + 8.dp, end = 16.dp, bottom = 76.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(circuitos) { circuito ->
                TarjetaCircuito(circuito = circuito, alHacerClicEnVerMas = alHacerClicEnVerMas)
            }
        }
    }
}

@Composable
fun ListaPuntosInteres(
    puntos: List<PuntoInteres>,
    paddingSuperior: Dp = 0.dp
) {
    if (puntos.isEmpty()) {
        EstadoVacio("No hay puntos de interés disponibles.", Modifier.padding(top = paddingSuperior))
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, top = paddingSuperior + 8.dp, end = 16.dp, bottom = 76.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(puntos) { punto ->
                TarjetaPuntoInteres(punto = punto)
            }
        }
    }
}

@Composable
fun EstadoVacio(mensaje: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(mensaje, color = AzulPetroleo)
    }
}

@Composable
fun TarjetaCircuito(circuito: Circuito, alHacerClicEnVerMas: (Circuito) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (circuito.imagenMapa != null) {
                AsyncImage(
                    model = circuito.imagenMapa.aUrlCompleta(),
                    contentDescription = "Imagen de ${circuito.nombre}",
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
                        text = circuito.nombre,
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
                    text = circuito.descripcion,
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
                    InsigniaCircuito(
                        icono = Icons.Default.DirectionsWalk,
                        texto = "${circuito.distanciaKm} km",
                        colorFondo = Color(0xFFE3F2FD),
                        colorContenido = Color(0xFF1976D2)
                    )
                    InsigniaCircuito(
                        icono = Icons.Default.Timer,
                        texto = circuito.duracionEstimada,
                        colorFondo = Color(0xFFFFF3E0),
                        colorContenido = Color(0xFFF57C00)
                    )
                    InsigniaCircuito(
                        icono = null,
                        texto = circuito.dificultad,
                        colorFondo = when(circuito.dificultad.lowercase()) {
                            "baja" -> Color(0xFFE8F5E9)
                            "media" -> Color(0xFFFFFDE7)
                            else -> Color(0xFFFFEBEE)
                        },
                        colorContenido = when(circuito.dificultad.lowercase()) {
                            "baja" -> Color(0xFF388E3C)
                            "media" -> Color(0xFFFBC02D)
                            else -> Color(0xFFD32F2F)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { alHacerClicEnVerMas(circuito) },
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
fun InsigniaCircuito(
    icono: ImageVector?,
    texto: String,
    colorFondo: Color,
    colorContenido: Color
) {
    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icono != null) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorContenido,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = texto,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colorContenido
            )
        }
    }
}

@Composable
fun TarjetaPuntoInteres(punto: PuntoInteres) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (punto.galeria.isNotEmpty()) {
                CarruselGaleria(galeria = punto.galeria)
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
                        text = punto.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = when(punto.tipo.lowercase()) {
                            "historico" -> Color(0xFFFFF3E0)
                            "cultural" -> Color(0xFFE1F5FE)
                            else -> Color(0xFFF3E5F5)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = punto.tipo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when(punto.tipo.lowercase()) {
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
                        text = punto.circuitoNombre,
                        fontSize = 12.sp,
                        color = GoldColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = punto.descripcion,
                    fontSize = 14.sp,
                    color = NegroPuro.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
                
                if (punto.datosHistoricos.isNotEmpty()) {
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
                    
                    punto.datosHistoricos.forEach { datoHistorico ->
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = datoHistorico.titulo,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NegroPuro
                            )
                            Text(
                                text = datoHistorico.contenido,
                                fontSize = 12.sp,
                                color = NegroPuro.copy(alpha = 0.6f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                val contexto = LocalContext.current
                Button(
                    onClick = {
                        val uri = Uri.parse("geo:0,0?q=${punto.latitud},${punto.longitud}(${punto.nombre})")
                        val intento = Intent(Intent.ACTION_VIEW, uri)
                        intento.setPackage("com.google.android.apps.maps")
                        try {
                            contexto.startActivity(intento)
                        } catch (e: Exception) {
                            val uriWeb = Uri.parse("https://www.google.com/maps/search/?api=1&query=${punto.latitud},${punto.longitud}")
                            contexto.startActivity(Intent(Intent.ACTION_VIEW, uriWeb))
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
fun VistaPreviaPantallaCircuitosYPuntos() {
    Codice路Theme {
        PantallaCircuitosYPuntos(
            ciudad = Ciudad(
                id = 1,
                nombre = "León",
                descripcion = "Ciudad universitaria",
                imagenPortada = null,
                latitudCentro = 0.0,
                longitudCentro = 0.0,
                datosHistoricos = emptyList(),
                galeria = emptyList(),
                circuitos = listOf(
                    Circuito(
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
                            PuntoInteres(
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
                                    ItemGaleria(1, null, 1, "Fachada", "Imagen", "https://example.com/img1.jpg", null),
                                    ItemGaleria(2, null, 1, "Video Tour", "Video", null, "https://youtube.com/watch?v=123")
                                )
                            )
                        )
                    )
                )
            ),
            pestanaSeleccionada = 1,
            alHacerClicEnVerMas = {}
        )
    }
}
