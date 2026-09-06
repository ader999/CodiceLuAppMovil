package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun DialogoVistaPreviaImagen(
    imagenes: List<String>,
    paginaInicial: Int = 0,
    alCerrar: () -> Unit
) {
    if (imagenes.isEmpty()) return

    val paginaValida = paginaInicial.coerceIn(0, (imagenes.size - 1).coerceAtLeast(0))
    val estadoPaginador = rememberPagerState(initialPage = paginaValida, pageCount = { imagenes.size })
    val coroutineScope = rememberCoroutineScope()
    var zoomActivo by remember { mutableStateOf(false) }

    LaunchedEffect(estadoPaginador.currentPage) {
        zoomActivo = false
    }

    Dialog(
        onDismissRequest = alCerrar,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = estadoPaginador,
                userScrollEnabled = !zoomActivo,
                modifier = Modifier.fillMaxSize()
            ) { pagina ->
                key(pagina) {
                    ElementoImagenZoomable(
                        url = imagenes[pagina],
                        alCambiarZoom = { estaZoomed ->
                            if (estadoPaginador.currentPage == pagina) {
                                zoomActivo = estaZoomed
                            }
                        }
                    )
                }
            }

            // Contador de páginas
            if (imagenes.size > 1) {
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${estadoPaginador.currentPage + 1} / ${imagenes.size}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Botón de cerrar
            IconButton(
                onClick = alCerrar,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }

            // Flecha navegación anterior
            if (imagenes.size > 1 && !zoomActivo && estadoPaginador.currentPage > 0) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            estadoPaginador.animateScrollToPage(estadoPaginador.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Anterior",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp).padding(start = 4.dp)
                    )
                }
            }

            // Flecha navegación siguiente
            if (imagenes.size > 1 && !zoomActivo && estadoPaginador.currentPage < imagenes.size - 1) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            estadoPaginador.animateScrollToPage(estadoPaginador.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Siguiente",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Indicadores inferiores interactivos (dots)
            if (imagenes.size > 1) {
                Row(
                    Modifier
                        .navigationBarsPadding()
                        .height(50.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(imagenes.size) { iteracion ->
                        val esActual = estadoPaginador.currentPage == iteracion
                        val color = if (esActual) Color.White else Color.White.copy(alpha = 0.5f)
                        val tamano = if (esActual) 10.dp else 7.dp

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(tamano)
                                .clickable {
                                    coroutineScope.launch {
                                        estadoPaginador.animateScrollToPage(iteracion)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ElementoImagenZoomable(
    url: String,
    alCambiarZoom: (Boolean) -> Unit
) {
    var escala by remember { mutableFloatStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(escala) {
        alCambiarZoom(escala > 1.05f)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { toque ->
                        if (escala > 1.05f) {
                            escala = 1f
                            desplazamiento = Offset.Zero
                        } else {
                            escala = 2.5f
                            val centroX = size.width / 2f
                            val centroY = size.height / 2f
                            desplazamiento = Offset(
                                (centroX - toque.x) * 1.5f,
                                (centroY - toque.y) * 1.5f
                            )
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val touchCount = event.changes.count { it.pressed }
                        val canceled = event.changes.any { it.isConsumed }

                        if (!canceled) {
                            val isZoomed = escala > 1.05f
                            val isMultiTouch = touchCount > 1

                            // Solo interceptar y consumir si hay zoom activo o si el usuario usa 2 dedos (pellizco)
                            // Si está en escala normal (1x) con 1 dedo, NO consumimos para que HorizontalPager deslice normalmente
                            if (isZoomed || isMultiTouch) {
                                val zoomChange = if (isMultiTouch) event.calculateZoom() else 1f
                                val panChange = if (isZoomed) event.calculatePan() else Offset.Zero

                                if (zoomChange != 1f || panChange != Offset.Zero) {
                                    val nuevaEscala = (escala * zoomChange).coerceIn(1f, 4f)
                                    escala = nuevaEscala
                                    if (nuevaEscala > 1.05f) {
                                        desplazamiento += panChange
                                    } else {
                                        desplazamiento = Offset.Zero
                                    }
                                }

                                event.changes.forEach {
                                    if (it.positionChanged()) {
                                        it.consume()
                                    }
                                }
                            }
                        }
                    } while (!canceled && event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val anchoMax = constraints.maxWidth.toFloat()
        val altoMax = constraints.maxHeight.toFloat()
        val limiteX = (anchoMax * (escala - 1f) / 2f).coerceAtLeast(0f)
        val limiteY = (altoMax * (escala - 1f) / 2f).coerceAtLeast(0f)

        val desplazamientoAjustado = if (escala > 1.05f) {
            Offset(
                x = desplazamiento.x.coerceIn(-limiteX, limiteX),
                y = desplazamiento.y.coerceIn(-limiteY, limiteY)
            )
        } else {
            Offset.Zero
        }

        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                    translationX = desplazamientoAjustado.x
                    translationY = desplazamientoAjustado.y
                },
            contentScale = ContentScale.Fit
        )
    }
}
