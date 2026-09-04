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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codise.data.HerramientaUtilizada
import com.example.codise.data.IdiomaApp
import com.example.codise.ui.theme.*
import com.example.codise.utils.LocalCadenas
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaAsistente(
    viewModel: ViewModelAsistente,
    idiomaActual: IdiomaApp,
    paddingSuperior: Dp = 0.dp,
    alSolicitarUbicacion: () -> Unit = {}
) {
    val cadenas = LocalCadenas.current
    val contexto = LocalContext.current
    val estadoScroll = rememberLazyListState()
    val controladorTeclado = LocalSoftwareKeyboardController.current

    val mensajes by viewModel.mensajes
    val estaEscribiendo by viewModel.estaEscribiendo
    val ubicacionGps by viewModel.ubicacionGps

    var textoInput by remember { mutableStateOf("") }

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
        // Tarjeta de Cabecera: Presentación de Eduardo
        TarjetaCabeceraEduardo(
            ubicacionActiva = ubicacionGps != null,
            alReiniciar = {
                viewModel.reiniciarConversacion(
                    mensajeBienvenida = cadenas.asistenteMensaje,
                    idiomaCodigo = idiomaActual.codigo
                )
            },
            alHacerClicEnUbicacion = alSolicitarUbicacion
        )

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
}

@Composable
fun TarjetaCabeceraEduardo(
    ubicacionActiva: Boolean,
    alReiniciar: () -> Unit,
    alHacerClicEnUbicacion: () -> Unit
) {
    val cadenas = LocalCadenas.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar de Eduardo (Guardabarranco)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(AzulPetroleo)
                    .border(2.dp, GoldColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconasistente),
                    contentDescription = cadenas.asistenteTitulo,
                    modifier = Modifier.size(34.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Textos: Nombre, Rol y Estado
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cadenas.asistenteTitulo,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                }
                Text(
                    text = cadenas.asistenteSubtitulo,
                    fontSize = 12.sp,
                    color = AzulPetroleo.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Gemini IA • Datos en tiempo real",
                    fontSize = 10.sp,
                    color = GoldColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Indicador de GPS y botón de Nueva Conversación
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = alHacerClicEnUbicacion,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (ubicacionActiva) Icons.Default.GpsFixed else Icons.Default.LocationSearching,
                        contentDescription = if (ubicacionActiva) cadenas.asistenteUbicacionActiva else cadenas.asistenteUbicacionDesactivada,
                        tint = if (ubicacionActiva) GoldColor else GrisClaro,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = alReiniciar,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = cadenas.asistenteNuevaConversacion,
                        tint = AzulPetroleo,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
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
    alReintentar: () -> Unit
) {
    val cadenas = LocalCadenas.current
    val esUsuario = mensaje.emisor == EmisorMensaje.USUARIO
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val horaTexto = remember(mensaje.fecha) { formatoHora.format(Date(mensaje.fecha)) }

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
            modifier = Modifier.widthIn(max = 310.dp),
            horizontalAlignment = if (esUsuario) Alignment.End else Alignment.Start
        ) {
            Surface(
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
                        // Renderizado enriquecido con soporte de negritas en Markdown (**texto**)
                        val textoAnotado = formatearMarkdown(mensaje.texto, esUsuario)
                        Text(
                            text = textoAnotado,
                            color = if (esUsuario) BlancoBase else NegroPuro,
                            fontSize = 14.5.sp,
                            lineHeight = 20.sp
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

/**
 * Función utilitaria para analizar y estilizar formato markdown simple (**negrita**)
 */
@Composable
fun formatearMarkdown(texto: String, esUsuario: Boolean): AnnotatedString {
    return remember(texto, esUsuario) {
        buildAnnotatedString {
            val partes = texto.split("**")
            var esNegrita = false
            for (parte in partes) {
                if (esNegrita) {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = if (esUsuario) BlancoBase else AzulPetroleo
                        )
                    ) {
                        append(parte)
                    }
                } else {
                    append(parte)
                }
                esNegrita = !esNegrita
            }
        }
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
            TarjetaCabeceraEduardo(
                ubicacionActiva = true,
                alReiniciar = {},
                alHacerClicEnUbicacion = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
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
