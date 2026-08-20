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
import com.example.codise.utils.UtilidadesCompartir
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Circuito
import com.example.codise.data.PuntoInteres
import com.example.codise.data.local.PuntoVisitado
import com.example.codise.ui.theme.*
import com.example.codise.utils.aUrlCompleta

@Composable
fun PantallaDetalleCircuito(
    circuito: Circuito,
    puntosVisitados: List<PuntoVisitado>,
    alAlternarVisitado: (Int) -> Unit
) {
    val puntosOrdenados = remember(circuito.puntosInteres) {
        circuito.puntosInteres.sortedBy { it.orden }
    }
    val alcanceCorrutina = rememberCoroutineScope()
    val contexto = LocalContext.current
    var puntoACompartir by remember { mutableStateOf<Pair<PuntoInteres, Boolean>?>(null) }
    val capaGrafica = rememberGraphicsLayer()

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
            // Encabezado con información del circuito
            item {
                EncabezadoCircuito(circuito)
            }

            // Encabezado de puntos de interés
            item {
                Text(
                    "Puntos del Recorrido",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulPetroleo,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Elementos animados de los puntos de interés
            itemsIndexed(puntosOrdenados) { indice, punto ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(indice * 100L)
                    visible = true
                }

                val registroVisita = puntosVisitados.find { it.puntoInteresId == punto.id }
                val estaVisitado = registroVisita != null
                val estaValidado = registroVisita?.estaValidado == true

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    TarjetaDetallePuntoInteres(
                        punto = punto,
                        estaVisitado = estaVisitado,
                        estaValidado = estaValidado,
                        alAlternarVisitado = { alAlternarVisitado(punto.id) },
                        alHacerClicEnCompartir = {
                            puntoACompartir = punto to estaValidado
                        }
                    )
                }
            }
        }

        // Tarjeta oculta para capturar y compartir
        if (puntoACompartir != null) {
            Box(
                modifier = Modifier
                    .offset(y = (-1000).dp) // Fuera de la pantalla
                    .wrapContentSize()
                    .drawWithContent {
                        capaGrafica.record {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                TarjetaCompartible(
                    punto = puntoACompartir!!.first,
                    estaValidado = puntoACompartir!!.second
                )
            }

            LaunchedEffect(puntoACompartir) {
                alcanceCorrutina.launch {
                    // Esperar un fotograma para que renderice
                    kotlinx.coroutines.delay(100)
                    if (capaGrafica.size.width > 0 && capaGrafica.size.height > 0) {
                        val mapaDeBits = capaGrafica.toImageBitmap().asAndroidBitmap()
                        UtilidadesCompartir.compartirBitmap(contexto, mapaDeBits)
                        puntoACompartir = null
                    }
                }
            }
        }
    }
}

@Composable
fun EncabezadoCircuito(circuito: Circuito) {
    Column {
        if (circuito.imagenMapa != null) {
            AsyncImage(
                model = circuito.imagenMapa.aUrlCompleta(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = circuito.nombre,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AzulPetroleo
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = circuito.descripcion,
                fontSize = 16.sp,
                color = NegroPuro,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextoConIcono(Icons.Default.DirectionsWalk, "${circuito.distanciaKm} km", AzulPetroleo)
                TextoConIcono(Icons.Default.Timer, circuito.duracionEstimada, AzulPetroleo)
            }
        }
    }
}

@Composable
fun TarjetaDetallePuntoInteres(
    punto: PuntoInteres,
    estaVisitado: Boolean,
    estaValidado: Boolean,
    alAlternarVisitado: () -> Unit,
    alHacerClicEnCompartir: () -> Unit
) {
    val contexto = LocalContext.current
    val colorEstado = if (estaValidado) GoldColor else if (estaVisitado) Color(0xFF4CAF50) else GoldColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Número de orden
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorEstado),
                    contentAlignment = Alignment.Center
                ) {
                    if (estaVisitado) {
                        Icon(
                            if (estaValidado) Icons.Default.Verified else Icons.Default.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = punto.orden.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = punto.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulPetroleo,
                    modifier = Modifier.weight(1f)
                )

                if (estaValidado) {
                    IconButton(onClick = alHacerClicEnCompartir) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = AzulPetroleo)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = punto.descripcion,
                fontSize = 14.sp,
                color = NegroPuro.copy(alpha = 0.8f)
            )

            if (punto.galeria.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                CarruselGaleria(galeria = punto.galeria)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de marcar como visitado
                val colorBoton = if (estaValidado) GoldColor else if (estaVisitado) Color(0xFF4CAF50) else AzulPetroleo
                val colorContenido = if (estaVisitado) colorBoton else Color.White
                val colorContenedor = if (estaVisitado) colorBoton.copy(alpha = 0.1f) else colorBoton

                Button(
                    onClick = alAlternarVisitado,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorContenedor,
                        contentColor = colorContenido
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = if (estaVisitado) androidx.compose.foundation.BorderStroke(1.dp, colorBoton) else null
                ) {
                    Icon(
                        if (estaValidado) Icons.Default.Verified else if (estaVisitado) Icons.Default.CheckCircle else Icons.Default.Done,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (estaValidado) "Verificado" else if (estaVisitado) "Visitado" else "Ya lo visité",
                        fontSize = 12.sp
                    )
                }

                // Botón de cómo llegar
                Button(
                    onClick = {
                        val uri = Uri.parse("google.navigation:q=${punto.latitud},${punto.longitud}")
                        val intento = Intent(Intent.ACTION_VIEW, uri)
                        intento.setPackage("com.google.android.apps.maps")
                        try {
                            contexto.startActivity(intento)
                        } catch (e: Exception) {
                            val uriWeb = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${punto.latitud},${punto.longitud}")
                            contexto.startActivity(Intent(Intent.ACTION_VIEW, uriWeb))
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
fun TarjetaCompartible(punto: PuntoInteres, estaValidado: Boolean) {
    val pincelDegradado = Brush.verticalGradient(listOf(AzulPetroleo, Color(0xFF1A5F7A)))
    
    Card(
        modifier = Modifier
            .width(350.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.background(pincelDegradado)) {
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
                        model = punto.galeria.firstOrNull()?.imagen?.aUrlCompleta(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = punto.nombre,
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
                            if (estaValidado) GoldColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        if (estaValidado) Icons.Default.Verified else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (estaValidado) GoldColor else Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (estaValidado) "VISITA VERIFICADA" else "VISITA REGISTRADA",
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
                        text = "CODICE 路",
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
fun TextoConIcono(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(texto, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
