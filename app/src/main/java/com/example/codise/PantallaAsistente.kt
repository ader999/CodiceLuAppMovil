package com.example.codise

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.codise.data.HerramientaUtilizada
import com.example.codise.data.IdiomaApp
import com.example.codise.data.PuntoInteres
import com.example.codise.data.local.PuntoVisitado
import com.example.codise.ui.theme.*
import com.example.codise.utils.LocalCadenas
import com.example.codise.utils.aUrlCompleta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaAsistente(
    viewModel: ViewModelAsistente,
    idiomaActual: IdiomaApp,
    paddingSuperior: Dp = 0.dp,
    alSolicitarUbicacion: () -> Unit = {},
    todosLosPuntos: List<PuntoInteres> = emptyList(),
    puntosVisitados: List<PuntoVisitado> = emptyList(),
    alAlternarVisitado: (Int) -> Unit = {}
) {
    val cadenas = LocalCadenas.current
    val contexto = LocalContext.current
    val estadoScroll = rememberLazyListState()
    val controladorTeclado = LocalSoftwareKeyboardController.current

    val mensajes by viewModel.mensajes
    val estaEscribiendo by viewModel.estaEscribiendo
    val ubicacionGps by viewModel.ubicacionGps

    var textoInput by remember { mutableStateOf("") }
    var puntoSeleccionadoParaDetalle by remember { mutableStateOf<PuntoInteres?>(null) }

    // Inicializar con mensaje de bienvenida en el idioma seleccionado
    LaunchedEffect(idiomaActual) {
        viewModel.inicializarBienvenida(
            mensajeBienvenida = cadenas.asistenteMensaje,
            idiomaCodigo = idiomaActual.codigo
        )
    }

    // Auto-scroll al final cuando llega un nuevo mensaje o está escribiendo
    LaunchedEffect(mensajes.size, estaEscribiendo) {
        if (mensajes.isNotEmpty()) {
            estadoScroll.animateScrollToItem(mensajes.size - 1)
        }
    }

    val enviar: (String) -> Unit = { mensajeAEnviar ->
        val limpio = mensajeAEnviar.trim()
        if (limpio.isNotBlank() && !estaEscribiendo) {
            textoInput = ""
            controladorTeclado?.hide()
            viewModel.enviarMensaje(
                textoMensaje = limpio,
                idiomaCodigo = idiomaActual.codigo,
                mensajeErrorPorDefecto = cadenas.asistenteError
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingSuperior)
            .imePadding()
    ) {
        // Lista de Mensajes y Sugerencias
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = estadoScroll,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Si solo está el mensaje de bienvenida, mostrar sugerencias rápidas
                if (mensajes.size <= 1) {
                    item {
                        SugerenciasRapidas(
                            alSeleccionarSugerencia = { sugerencia -> enviar(sugerencia) }
                        )
                    }
                }

                items(mensajes, key = { it.id }) { mensaje ->
                    BurbujaMensaje(
                        mensaje = mensaje,
                        todosLosPuntos = todosLosPuntos,
                        puntosVisitados = puntosVisitados,
                        alSeleccionarPuntoDetalle = { punto -> puntoSeleccionadoParaDetalle = punto },
                        alCopiar = { texto ->
                            val clipboard = contexto.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Eduardo", texto)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(contexto, cadenas.copiadoAlPortapapeles, Toast.LENGTH_SHORT).show()
                        },
                        alReintentar = {
                            viewModel.enviarMensaje(
                                textoMensaje = mensaje.texto,
                                idiomaCodigo = idiomaActual.codigo,
                                mensajeErrorPorDefecto = cadenas.asistenteError
                            )
                        }
                    )
                }

                if (estaEscribiendo) {
                    item {
                        BurbujaEscribiendo(mensaje = cadenas.asistentePensando)
                    }
                }
            }
        }

        // Barra de Entrada de Texto (Bottom Input Bar)
        BarraEntradaMensaje(
            texto = textoInput,
            alCambiarTexto = { textoInput = it },
            estaEscribiendo = estaEscribiendo,
            tieneUbicacion = ubicacionGps != null,
            alSolicitarUbicacion = alSolicitarUbicacion,
            alEnviar = { enviar(textoInput) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .padding(bottom = 70.dp) // Espacio para la barra de navegación inferior flotante
        )
    }

    // Modal de Detalles Completos del Punto de Interés seleccionado
    if (puntoSeleccionadoParaDetalle != null) {
        val punto = puntoSeleccionadoParaDetalle!!
        val registroVisita = puntosVisitados.find { it.puntoInteresId == punto.id }
        DialogoDetallePuntoInteres(
            punto = punto,
            estaVisitado = registroVisita != null,
            estaValidado = registroVisita?.estaValidado == true,
            alAlternarVisitado = { alAlternarVisitado(punto.id) },
            alCerrar = { puntoSeleccionadoParaDetalle = null }
        )
    }
}



@Composable
fun SugerenciasRapidas(
    alSeleccionarSugerencia: (String) -> Unit
) {
    val cadenas = LocalCadenas.current
    val sugerencias = listOf(
        cadenas.asistenteSugerencia1,
        cadenas.asistenteSugerencia2,
        cadenas.asistenteSugerencia3,
        cadenas.asistenteSugerencia4
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = "Preguntas sugeridas:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AzulPetroleo.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(sugerencias) { sugerencia ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BlancoBase,
                    border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.5f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.clickable { alSeleccionarSugerencia(sugerencia) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sugerencia,
                            fontSize = 12.sp,
                            color = AzulPetroleo,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BurbujaMensaje(
    mensaje: MensajeChat,
    alCopiar: (String) -> Unit,
    alReintentar: () -> Unit,
    todosLosPuntos: List<PuntoInteres> = emptyList(),
    puntosVisitados: List<PuntoVisitado> = emptyList(),
    alSeleccionarPuntoDetalle: (PuntoInteres) -> Unit = {}
) {
    val cadenas = LocalCadenas.current
    val esUsuario = mensaje.emisor == EmisorMensaje.USUARIO
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val horaTexto = remember(mensaje.fecha) { formatoHora.format(Date(mensaje.fecha)) }

    // Identificar qué puntos de interés corresponden a este mensaje (por IDs del backend o por coincidencia en texto)
    val puntosDelMensaje = remember(mensaje, todosLosPuntos) {
        if (esUsuario || mensaje.esError || todosLosPuntos.isEmpty()) {
            emptyList()
        } else {
            val porIds = mensaje.puntosInteresIds?.mapNotNull { id ->
                todosLosPuntos.find { it.id == id }
            } ?: emptyList()

            if (porIds.isNotEmpty()) {
                porIds.distinctBy { it.id }
            } else {
                todosLosPuntos.filter { punto ->
                    val nombreLimpio = punto.nombre.trim()
                    if (nombreLimpio.length >= 4) {
                        mensaje.texto.contains(nombreLimpio, ignoreCase = true)
                    } else false
                }.distinctBy { it.id }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esUsuario) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!esUsuario) {
            // Icono de Eduardo
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AzulPetroleo)
                    .border(1.dp, GoldColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconasistente),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = if (esUsuario) Modifier.widthIn(max = 300.dp) else Modifier.weight(1f),
            horizontalAlignment = if (esUsuario) Alignment.End else Alignment.Start
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 315.dp),
                shape = if (esUsuario) {
                    RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
                } else {
                    RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
                },
                color = when {
                    mensaje.esError -> Color(0xFFFFEBEE)
                    esUsuario -> AzulPetroleo
                    else -> BlancoBase
                },
                border = when {
                    mensaje.esError -> BorderStroke(1.dp, Color(0xFFEF5350))
                    !esUsuario -> BorderStroke(1.dp, GoldColor.copy(alpha = 0.25f))
                    else -> null
                },
                shadowElevation = if (esUsuario) 2.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Si Eduardo utilizó herramientas / live query, mostrar etiqueta
                    if (!esUsuario && !mensaje.herramientas.isNullOrEmpty()) {
                        EtiquetaHerramientasUtilizadas(mensaje.herramientas)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (mensaje.esError) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mensaje.texto,
                                color = Color(0xFFC62828),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Renderizado enriquecido con soporte completo de Markdown (encabezados, listas, divisores, negritas)
                        ContenidoMensajeMarkdown(
                            texto = mensaje.texto,
                            esUsuario = esUsuario
                        )
                    }

                    // Fila inferior dentro de la burbuja: hora y botón copiar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = horaTexto,
                            fontSize = 10.sp,
                            color = if (esUsuario) BlancoBase.copy(alpha = 0.6f) else Color.Gray
                        )
                        if (!esUsuario && !mensaje.esError) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar",
                                tint = Color.Gray.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { alCopiar(mensaje.texto) }
                            )
                        }
                    }
                }
            }

            if (mensaje.esError) {
                TextButton(
                    onClick = alReintentar,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = GoldColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cadenas.reintentar,
                        color = GoldColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Si Eduardo recomendó lugares, mostrar carrusel de tarjetas previas interactivas
            if (!esUsuario && !mensaje.esError && puntosDelMensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = GoldColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (puntosDelMensaje.size == 1) cadenas.puntosDeInteres else cadenas.puntosDelRecorrido,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(puntosDelMensaje, key = { it.id }) { punto ->
                        val registroVisita = puntosVisitados.find { it.puntoInteresId == punto.id }
                        val estaVisitado = registroVisita != null
                        val estaValidado = registroVisita?.estaValidado == true

                        TarjetaPreviaPuntoAsistente(
                            punto = punto,
                            estaVisitado = estaVisitado,
                            estaValidado = estaValidado,
                            alHacerClic = { alSeleccionarPuntoDetalle(punto) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mini-tarjeta visual previa para cada punto de interés recomendado por Eduardo
 */
@Composable
fun TarjetaPreviaPuntoAsistente(
    punto: PuntoInteres,
    estaVisitado: Boolean,
    estaValidado: Boolean,
    alHacerClic: () -> Unit
) {
    val cadenas = LocalCadenas.current
    val contexto = LocalContext.current
    val primeraImagen = punto.galeria.firstOrNull()?.imagen?.aUrlCompleta()

    Card(
        modifier = Modifier
            .width(245.dp)
            .clickable { alHacerClic() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.35f))
    ) {
        Column {
            // Portada con tipo e indicador de visita
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                if (primeraImagen != null) {
                    AsyncImage(
                        model = primeraImagen,
                        contentDescription = punto.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AzulPetroleo.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = AzulPetroleo.copy(alpha = 0.35f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Badge de Tipo en esquina superior derecha
                Surface(
                    color = when (punto.tipo.lowercase()) {
                        "historico" -> Color(0xFFFFF3E0)
                        "cultural" -> Color(0xFFE1F5FE)
                        else -> Color(0xFFF3E5F5)
                    },
                    shape = RoundedCornerShape(bottomStart = 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = punto.tipo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (punto.tipo.lowercase()) {
                            "historico" -> Color(0xFFE65100)
                            "cultural" -> Color(0xFF01579B)
                            else -> Color(0xFF4A148C)
                        }
                    )
                }

                // Badge de Visitado en esquina superior izquierda si aplica
                if (estaVisitado) {
                    Surface(
                        color = if (estaValidado) GoldColor else Color(0xFF4CAF50),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (estaValidado) Icons.Default.Verified else Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (estaValidado) cadenas.verificado else cadenas.visitado,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Información del Punto
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = punto.nombre,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulPetroleo,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = GoldColor,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = punto.circuitoNombre,
                        fontSize = 11.sp,
                        color = GoldColor,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fila de Botones: Ver detalles, Cómo llegar (ajustados con proporciones limpias)
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Ver Detalles
                        OutlinedButton(
                            onClick = alHacerClic,
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            border = BorderStroke(1.dp, AzulPetroleo)
                        ) {
                            Text(
                                text = cadenas.verMas,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzulPetroleo,
                                maxLines = 1
                            )
                        }

                        // Botón Cómo llegar
                        FilledIconButton(
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
                            modifier = Modifier.size(34.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = GoldColor,
                                contentColor = BlancoBase
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = cadenas.comoLlegar,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo modal con la tarjeta completa de detalles del Punto de Interés (carrusel de fotos, descripción, datos históricos y cómo llegar/visitar)
 */
@Composable
fun DialogoDetallePuntoInteres(
    punto: PuntoInteres,
    estaVisitado: Boolean,
    estaValidado: Boolean,
    alAlternarVisitado: () -> Unit,
    alCerrar: () -> Unit
) {
    val cadenas = LocalCadenas.current
    val contexto = LocalContext.current

    Dialog(
        onDismissRequest = alCerrar,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { alCerrar() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoBase),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Carrusel de imágenes
                        if (punto.galeria.isNotEmpty()) {
                            CarruselGaleria(galeria = punto.galeria)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(AzulPetroleo.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = AzulPetroleo.copy(alpha = 0.35f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            // Título y Tipo
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = punto.nombre,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulPetroleo,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = when (punto.tipo.lowercase()) {
                                        "historico" -> Color(0xFFFFF3E0)
                                        "cultural" -> Color(0xFFE1F5FE)
                                        else -> Color(0xFFF3E5F5)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = punto.tipo,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (punto.tipo.lowercase()) {
                                            "historico" -> Color(0xFFE65100)
                                            "cultural" -> Color(0xFF01579B)
                                            else -> Color(0xFF4A148C)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Circuito
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GoldColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = punto.circuitoNombre,
                                    fontSize = 12.5.sp,
                                    color = GoldColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Descripción
                            Text(
                                text = punto.descripcion,
                                fontSize = 14.sp,
                                color = NegroPuro.copy(alpha = 0.75f),
                                lineHeight = 20.sp
                            )

                            // Datos Históricos
                            if (punto.datosHistoricos.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.HistoryEdu,
                                        contentDescription = null,
                                        tint = AzulPetroleo,
                                        modifier = Modifier.size(18.dp)
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
                                            color = NegroPuro.copy(alpha = 0.65f),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Botones de Acción
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Botón Cómo llegar
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
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Directions,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cadenas.comoLlegar,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Botón Marcar Visita
                                Button(
                                    onClick = alAlternarVisitado,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (estaValidado) GoldColor
                                        else if (estaVisitado) Color(0xFF4CAF50)
                                        else GoldColor
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (estaValidado) Icons.Default.Verified
                                        else if (estaVisitado) Icons.Default.Check
                                        else Icons.Default.AddLocationAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (estaValidado) cadenas.verificado
                                        else if (estaVisitado) cadenas.visitado
                                        else cadenas.yaLoVisite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Botón circular de Cerrar en la esquina superior derecha
                    IconButton(
                        onClick = alCerrar,
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopEnd)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = cadenas.cerrar,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EtiquetaHerramientasUtilizadas(herramientas: List<HerramientaUtilizada>) {
    val cadenas = LocalCadenas.current
    val nombres = herramientas.mapNotNull { it.nombre }.joinToString(", ")

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Celeste.copy(alpha = 0.35f),
        border = BorderStroke(0.5.dp, AzulPetroleo.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AzulPetroleo,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${cadenas.asistenteHerramientaUsada} $nombres",
                fontSize = 11.sp,
                color = AzulPetroleo,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun BurbujaEscribiendo(mensaje: String) {
    val transicion = rememberInfiniteTransition(label = "puntos")
    val alpha1 by transicion.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(500), repeatMode = RepeatMode.Reverse),
        label = "p1"
    )
    val alpha2 by transicion.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(500, delayMillis = 180), repeatMode = RepeatMode.Reverse),
        label = "p2"
    )
    val alpha3 by transicion.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(500, delayMillis = 360), repeatMode = RepeatMode.Reverse),
        label = "p3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(AzulPetroleo)
                .border(1.dp, GoldColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.iconasistente),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp),
            color = BlancoBase,
            border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.25f)),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mensaje,
                    fontSize = 13.sp,
                    color = AzulPetroleo.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GoldColor.copy(alpha = alpha1)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GoldColor.copy(alpha = alpha2)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GoldColor.copy(alpha = alpha3)))
                }
            }
        }
    }
}

@Composable
fun BarraEntradaMensaje(
    texto: String,
    alCambiarTexto: (String) -> Unit,
    estaEscribiendo: Boolean,
    tieneUbicacion: Boolean,
    alSolicitarUbicacion: () -> Unit,
    alEnviar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cadenas = LocalCadenas.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = BlancoBase,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.35f))
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono para activar GPS
                IconButton(
                    onClick = alSolicitarUbicacion,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (tieneUbicacion) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        contentDescription = if (tieneUbicacion) cadenas.asistenteUbicacionActiva else cadenas.asistenteUbicacionDesactivada,
                        tint = if (tieneUbicacion) GoldColor else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Campo de Texto
                TextField(
                    value = texto,
                    onValueChange = alCambiarTexto,
                    placeholder = {
                        Text(
                            text = cadenas.asistentePreguntale,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = NegroPuro,
                        unfocusedTextColor = NegroPuro
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { alEnviar() }),
                    maxLines = 4
                )

                // Botón Enviar
                val puedeEnviar = texto.isNotBlank() && !estaEscribiendo
                IconButton(
                    onClick = alEnviar,
                    enabled = puedeEnviar,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (puedeEnviar) GoldColor else Color.LightGray.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = cadenas.publicar,
                        tint = if (puedeEnviar) BlancoBase else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Tipos de bloques reconocidos para la estructura markdown
 */
sealed class ElementoMarkdown {
    data object Separador : ElementoMarkdown()
    data class Encabezado(val nivel: Int, val texto: String) : ElementoMarkdown()
    data class ElementoLista(val esNumerada: Boolean, val prefijo: String, val texto: String) : ElementoMarkdown()
    data class Cita(val texto: String) : ElementoMarkdown()
    data class Parrafo(val texto: String) : ElementoMarkdown()
}

/**
 * Parsea el texto en líneas identificando encabezados (###), listas (*, -, 1.), citas (>) y separadores (---)
 */
fun parsearLineasMarkdown(texto: String): List<ElementoMarkdown> {
    val lineas = texto.lines()
    val resultado = mutableListOf<ElementoMarkdown>()
    val parrafoAcumulado = StringBuilder()

    fun flushParrafo() {
        val contenido = parrafoAcumulado.toString().trim()
        if (contenido.isNotEmpty()) {
            resultado.add(ElementoMarkdown.Parrafo(contenido))
        }
        parrafoAcumulado.clear()
    }

    for (linea in lineas) {
        val trimLinea = linea.trim()

        if (trimLinea.isEmpty()) {
            flushParrafo()
            continue
        }

        // Comprobar si es un separador horizontal: ---, ***, ___ (al menos 3 caracteres repetidos)
        if (trimLinea.matches(Regex("^([-*_])\\1{2,}$"))) {
            flushParrafo()
            if (resultado.lastOrNull() !is ElementoMarkdown.Separador) {
                resultado.add(ElementoMarkdown.Separador)
            }
            continue
        }

        // Comprobar encabezados: #, ##, ###, ####, etc.
        val headingMatch = Regex("^(#{1,6})\\s+(.*)$").find(trimLinea)
        if (headingMatch != null) {
            flushParrafo()
            val nivel = headingMatch.groupValues[1].length
            val textoEncabezado = headingMatch.groupValues[2].trim()
            resultado.add(ElementoMarkdown.Encabezado(nivel, textoEncabezado))
            continue
        }

        // Comprobar elementos de lista no ordenada: *, -, + seguido de espacio
        val listaNoOrdenadaMatch = Regex("^([*\\-+])\\s+(.*)$").find(trimLinea)
        if (listaNoOrdenadaMatch != null) {
            flushParrafo()
            val textoItem = listaNoOrdenadaMatch.groupValues[2].trim()
            resultado.add(ElementoMarkdown.ElementoLista(esNumerada = false, prefijo = "•", texto = textoItem))
            continue
        }

        // Comprobar elementos de lista numerada: 1., 2., 1), etc.
        val listaNumeradaMatch = Regex("^(\\d+)[.)]\\s+(.*)$").find(trimLinea)
        if (listaNumeradaMatch != null) {
            flushParrafo()
            val numero = listaNumeradaMatch.groupValues[1]
            val textoItem = listaNumeradaMatch.groupValues[2].trim()
            resultado.add(ElementoMarkdown.ElementoLista(esNumerada = true, prefijo = "$numero.", texto = textoItem))
            continue
        }

        // Comprobar citas tipo blockquote: > texto
        val citaMatch = Regex("^>\\s*(.*)$").find(trimLinea)
        if (citaMatch != null) {
            flushParrafo()
            val textoCita = citaMatch.groupValues[1].trim()
            resultado.add(ElementoMarkdown.Cita(textoCita))
            continue
        }

        // Si es una línea de texto normal, acumular
        if (parrafoAcumulado.isNotEmpty()) {
            parrafoAcumulado.append("\n")
        }
        parrafoAcumulado.append(trimLinea)
    }

    flushParrafo()

    // Eliminar separadores superfluos al inicio o al final
    while (resultado.isNotEmpty() && resultado.first() is ElementoMarkdown.Separador) {
        resultado.removeAt(0)
    }
    while (resultado.isNotEmpty() && resultado.last() is ElementoMarkdown.Separador) {
        resultado.removeAt(resultado.size - 1)
    }

    return resultado
}

/**
 * Convierte texto con formato markdown en línea (negritas, cursivas, monoespaciado, enlaces) en AnnotatedString
 */
fun construirTextoAnotadoMarkdown(
    texto: String,
    esUsuario: Boolean,
    esEncabezado: Boolean = false
): AnnotatedString {
    return buildAnnotatedString {
        val colorNegrita = if (esUsuario) BlancoBase else AzulPetroleo
        val colorNormal = if (esUsuario) BlancoBase else if (esEncabezado) AzulPetroleo else NegroPuro

        val regexToken = Regex(
            """(\*\*\*(.+?)\*\*\*|\*\*(.+?)\*\*|__(.+?)__|(?<!\w)\*(.+?)\*(?!\w)|(?<!\w)_(.+?)_(?!\w)|`(.+?)`|\[(.+?)]\((.+?)\))"""
        )

        var indiceActual = 0
        val coincidencias = regexToken.findAll(texto)

        for (match in coincidencias) {
            if (match.range.first > indiceActual) {
                withStyle(SpanStyle(color = colorNormal)) {
                    append(texto.substring(indiceActual, match.range.first))
                }
            }

            val valor = match.value
            when {
                // ***negrita cursiva***
                valor.startsWith("***") && valor.endsWith("***") && valor.length >= 6 -> {
                    val contenido = valor.substring(3, valor.length - 3)
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = colorNegrita
                        )
                    ) {
                        append(contenido)
                    }
                }
                // **negrita**
                valor.startsWith("**") && valor.endsWith("**") && valor.length >= 4 -> {
                    val contenido = valor.substring(2, valor.length - 2)
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = colorNegrita
                        )
                    ) {
                        append(contenido)
                    }
                }
                // __negrita__
                valor.startsWith("__") && valor.endsWith("__") && valor.length >= 4 -> {
                    val contenido = valor.substring(2, valor.length - 2)
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = colorNegrita
                        )
                    ) {
                        append(contenido)
                    }
                }
                // *cursiva*
                valor.startsWith("*") && valor.endsWith("*") && valor.length >= 2 -> {
                    val contenido = valor.substring(1, valor.length - 1)
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = colorNormal
                        )
                    ) {
                        append(contenido)
                    }
                }
                // _cursiva_
                valor.startsWith("_") && valor.endsWith("_") && valor.length >= 2 -> {
                    val contenido = valor.substring(1, valor.length - 1)
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = colorNormal
                        )
                    ) {
                        append(contenido)
                    }
                }
                // `código`
                valor.startsWith("`") && valor.endsWith("`") && valor.length >= 2 -> {
                    val contenido = valor.substring(1, valor.length - 1)
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = if (esUsuario) Color(0x33FFFFFF) else Color(0x14000000),
                            color = colorNormal
                        )
                    ) {
                        append(" $contenido ")
                    }
                }
                // [texto](url)
                valor.startsWith("[") && valor.contains("](") && valor.endsWith(")") -> {
                    val textoEnlace = valor.substringAfter("[").substringBefore("](")
                    withStyle(
                        SpanStyle(
                            color = GoldColor,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(textoEnlace)
                    }
                }
                else -> {
                    withStyle(SpanStyle(color = colorNormal)) {
                        append(valor)
                    }
                }
            }

            indiceActual = match.range.last + 1
        }

        if (indiceActual < texto.length) {
            withStyle(SpanStyle(color = colorNormal)) {
                append(texto.substring(indiceActual))
            }
        }
    }
}

/**
 * Renderizador de mensajes con formato Markdown completo para Eduardo y el usuario
 */
@Composable
fun ContenidoMensajeMarkdown(
    texto: String,
    esUsuario: Boolean,
    modifier: Modifier = Modifier
) {
    val elementos = remember(texto) { parsearLineasMarkdown(texto) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        elementos.forEach { elemento ->
            when (elemento) {
                is ElementoMarkdown.Separador -> {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        thickness = 0.8.dp,
                        color = if (esUsuario) BlancoBase.copy(alpha = 0.3f) else AzulPetroleo.copy(alpha = 0.18f)
                    )
                }
                is ElementoMarkdown.Encabezado -> {
                    val tamanoFuente = when (elemento.nivel) {
                        1 -> 17.sp
                        2 -> 16.sp
                        3 -> 15.sp
                        else -> 14.5.sp
                    }
                    Text(
                        text = construirTextoAnotadoMarkdown(
                            texto = elemento.texto,
                            esUsuario = esUsuario,
                            esEncabezado = true
                        ),
                        color = if (esUsuario) BlancoBase else AzulPetroleo,
                        fontSize = tamanoFuente,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (tamanoFuente.value + 5).sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is ElementoMarkdown.ElementoLista -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (elemento.esNumerada) "${elemento.prefijo} " else "• ",
                            color = if (esUsuario) BlancoBase.copy(alpha = 0.9f) else GoldColor,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = construirTextoAnotadoMarkdown(
                                texto = elemento.texto,
                                esUsuario = esUsuario
                            ),
                            color = if (esUsuario) BlancoBase else NegroPuro,
                            fontSize = 14.5.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
                is ElementoMarkdown.Cita -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(GoldColor, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = construirTextoAnotadoMarkdown(elemento.texto, esUsuario),
                            color = if (esUsuario) BlancoBase.copy(alpha = 0.85f) else NegroPuro.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 19.sp
                        )
                    }
                }
                is ElementoMarkdown.Parrafo -> {
                    Text(
                        text = construirTextoAnotadoMarkdown(
                            texto = elemento.texto,
                            esUsuario = esUsuario
                        ),
                        color = if (esUsuario) BlancoBase else NegroPuro,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

/**
 * Función utilitaria para analizar y estilizar formato markdown simple
 */
@Composable
fun formatearMarkdown(texto: String, esUsuario: Boolean): AnnotatedString {
    return remember(texto, esUsuario) {
        construirTextoAnotadoMarkdown(texto, esUsuario)
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaAsistente() {
    Codice路Theme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Celeste)
                .padding(12.dp)
        ) {

            SugerenciasRapidas(alSeleccionarSugerencia = {})
            Spacer(modifier = Modifier.height(12.dp))
            BurbujaMensaje(
                mensaje = MensajeChat(
                    emisor = EmisorMensaje.ASISTENTE,
                    texto = "¡Hola! Soy **Eduardo**, tu guía virtual en las Ciudades Creativas de Nicaragua. ¿Qué deseas descubrir hoy?",
                    herramientas = listOf(
                        HerramientaUtilizada(
                            nombre = "buscar_circuitos",
                            argumentos = mapOf("ciudad" to "Granada")
                        )
                    )
                ),
                alCopiar = {},
                alReintentar = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            BurbujaMensaje(
                mensaje = MensajeChat(
                    emisor = EmisorMensaje.USUARIO,
                    texto = "¿Qué lugares me recomiendas en Granada?"
                ),
                alCopiar = {},
                alReintentar = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            BurbujaEscribiendo(mensaje = "Eduardo está consultando la información...")
            Spacer(modifier = Modifier.weight(1f))
            BarraEntradaMensaje(
                texto = "",
                alCambiarTexto = {},
                estaEscribiendo = false,
                tieneUbicacion = true,
                alSolicitarUbicacion = {},
                alEnviar = {}
            )
        }
    }
}
