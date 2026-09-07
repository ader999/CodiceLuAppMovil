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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.codise.data.Ciudad
import com.example.codise.data.Circuito
import com.example.codise.data.PuntoInteres
import com.example.codise.data.ItemGaleria
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import com.example.codise.ui.theme.AzulPetroleo
import com.example.codise.ui.theme.Codice路Theme
import com.example.codise.ui.theme.GoldColor
import com.example.codise.ui.theme.NegroPuro
import com.example.codise.utils.LocalCadenas
import com.example.codise.utils.aUrlCompleta

@Composable
fun PantallaDetalleCiudad(
    ciudad: Ciudad,
    alRegresar: () -> Unit = {},
    paddingSuperior: Dp = 0.dp
) {
    val cadenas = LocalCadenas.current
    var videoSeleccionado by remember { mutableStateOf<ItemGaleria?>(null) }
    var videoParaPantallaCompleta by remember { mutableStateOf<ItemGaleria?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingSuperior)
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
                text = cadenas.descripcion,
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

            // Agrupación de galerías por tipo de punto de interés
            val mapaPuntos = remember(ciudad) {
                ciudad.circuitos.flatMap { it.puntosInteres }.associateBy { it.id }
            }

            val galeriasOrdenadas = remember(ciudad, mapaPuntos) {
                val pares = mutableListOf<Pair<String, ItemGaleria>>()

                // 1. Fotos de los puntos de interés de los circuitos de la ciudad
                ciudad.circuitos.flatMap { it.puntosInteres }.forEach { punto ->
                    val tipo = normalizarTipoPunto(punto.tipo)
                    punto.galeria.forEach { item ->
                        pares.add(tipo to item)
                    }
                }

                // 2. Elementos de ciudad.galeria
                ciudad.galeria.forEach { item ->
                    val tipoPunto = item.puntoInteres?.let { id ->
                        mapaPuntos[id]?.tipo?.let { normalizarTipoPunto(it) }
                    }
                    pares.add((tipoPunto ?: "General") to item)
                }

                // 3. Deduplicar manteniendo el tipo más específico
                val vistos = mutableSetOf<String>()
                val unicos = mutableListOf<Pair<String, ItemGaleria>>()
                pares.forEach { (tipo, item) ->
                    val clave = when {
                        item.id != 0 -> "id_${item.id}"
                        !item.imagen.isNullOrBlank() -> "img_${item.imagen}"
                        !item.videoArchivo.isNullOrBlank() -> "vidarch_${item.videoArchivo}"
                        !item.videoUrl.isNullOrBlank() -> "vid_${item.videoUrl}"
                        else -> "tit_${item.titulo}"
                    }
                    if (vistos.add(clave)) {
                        unicos.add(tipo to item)
                    }
                }

                unicos.groupBy({ it.first }, { it.second })
                    .filterValues { it.isNotEmpty() }
                    .toList()
                    .sortedWith(
                        compareBy { (tipo, _) -> if (tipo.equals("General", ignoreCase = true)) 1 else 0 }
                    )
            }

            if (galeriasOrdenadas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = cadenas.galeriaMultimedia,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulPetroleo
                )

                if (videoSeleccionado != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            key(videoSeleccionado!!.id, videoSeleccionado!!.urlVideo) {
                                ReproductorMultimedia(
                                    elemento = videoSeleccionado!!,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { videoParaPantallaCompleta = videoSeleccionado },
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Fullscreen,
                                        contentDescription = "Pantalla completa",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { videoSeleccionado = null },
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = cadenas.cerrar,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                galeriasOrdenadas.forEach { (tipo, items) ->
                    key(tipo) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            // Subtítulo con ícono y conteo si hay múltiples tipos o no es categoría General
                            if (galeriasOrdenadas.size > 1 || !tipo.equals("General", ignoreCase = true)) {
                                EncabezadoTipoGaleria(
                                    tipo = tipo,
                                    cantidad = items.size
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            val galeriaMezclada = remember(items) { items.shuffled() }
                            CarruselGaleria(
                                galeria = galeriaMezclada,
                                alHacerClicEnVideo = { elemento ->
                                    videoSeleccionado = elemento
                                }
                            )
                        }
                    }
                }
            }

            if (ciudad.datosHistoricos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = cadenas.datosHistoricos,
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
                            Text(text = "${cadenas.epoca}: ${datoHistorico.epocaOAno}", fontSize = 12.sp, color = GoldColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = datoHistorico.contenido, fontSize = 14.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (videoParaPantallaCompleta != null) {
        DialogoVistaPreviaGaleria(
            galeria = listOf(videoParaPantallaCompleta!!),
            paginaInicial = 0,
            alCerrar = { videoParaPantallaCompleta = null }
        )
    }
}

private fun normalizarTipoPunto(tipo: String): String {
    val limpio = tipo.trim()
    if (limpio.isBlank()) return "General"
    return limpio.split(Regex("\\s+")).joinToString(" ") { palabra ->
        palabra.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

@Composable
private fun EncabezadoTipoGaleria(
    tipo: String,
    cantidad: Int,
    modifier: Modifier = Modifier
) {
    val tipoLower = tipo.lowercase()
    val (fondoBadge, colorTextoBadge) = when {
        "historico" in tipoLower || "histórico" in tipoLower -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "natural" in tipoLower || "ecologico" in tipoLower || "ecológico" in tipoLower -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "cultural" in tipoLower -> Color(0xFFE1F5FE) to Color(0xFF01579B)
        "religioso" in tipoLower -> Color(0xFFEDE7F6) to Color(0xFF512DA8)
        "gastronomico" in tipoLower || "gastronómico" in tipoLower -> Color(0xFFFBE9E7) to Color(0xFFD84315)
        else -> Color(0xFFE0F2F1) to Color(0xFF00695C)
    }

    val icono: ImageVector = when {
        "historico" in tipoLower || "histórico" in tipoLower -> Icons.Default.AccountBalance
        "natural" in tipoLower || "ecologico" in tipoLower || "ecológico" in tipoLower -> Icons.Default.Park
        "cultural" in tipoLower -> Icons.Default.Palette
        "religioso" in tipoLower -> Icons.Default.Church
        "gastronomico" in tipoLower || "gastronómico" in tipoLower -> Icons.Default.Restaurant
        else -> Icons.Default.Place
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorTextoBadge,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tipo,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AzulPetroleo
            )
        }

        Surface(
            color = fondoBadge,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "$cantidad",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colorTextoBadge
            )
        }
    }
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
                        puntosInteres = listOf(
                            PuntoInteres(
                                id = 1,
                                circuito = 1,
                                circuitoNombre = "Circuito Colonial",
                                nombre = "Catedral de León",
                                descripcion = "Catedral de la Asunción de la Bienaventurada Virgen María.",
                                tipo = "Sitio Histórico",
                                orden = 1,
                                latitud = 12.435,
                                longitud = -86.879,
                                datosHistoricos = emptyList(),
                                galeria = listOf(
                                    ItemGaleria(1, 1, 1, "Catedral de León", "Imagen", "https://example.com/cat.jpg", null)
                                )
                            ),
                            PuntoInteres(
                                id = 2,
                                circuito = 1,
                                circuitoNombre = "Circuito Colonial",
                                nombre = "Volcán Cerro Negro",
                                descripcion = "Volcán activo para sandboarding.",
                                tipo = "Sitio Natural",
                                orden = 2,
                                latitud = 12.500,
                                longitud = -86.700,
                                datosHistoricos = emptyList(),
                                galeria = listOf(
                                    ItemGaleria(2, 1, 2, "Cerro Negro", "Imagen", "https://example.com/volcan.jpg", null)
                                )
                            )
                        )
                    )
                ),
                datosHistoricos = emptyList(),
                galeria = listOf(
                    ItemGaleria(1, 1, 1, "Catedral de León", "Imagen", "https://example.com/cat.jpg", null),
                    ItemGaleria(2, 1, 2, "Cerro Negro", "Imagen", "https://example.com/volcan.jpg", null),
                    ItemGaleria(3, 1, null, "Documental León", "Video", null, "https://youtube.com/watch?v=123")
                )
            ),
            alRegresar = {}
        )
    }
}
