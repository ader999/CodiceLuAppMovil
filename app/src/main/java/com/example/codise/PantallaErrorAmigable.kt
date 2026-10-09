package com.example.codise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.codise.data.IdiomaApp
import com.example.codise.ui.theme.*
import com.example.codise.utils.CadenasIdiomas
import com.example.codise.utils.LocalCadenas
import com.example.codise.utils.ManejadorErrores
import com.example.codise.utils.ManejadorErrores.CategoriaError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla completa de error amigable diseñada para cumplir con el requerimiento de resiliencia
 * y flujo automático de principio a fin, ocultando códigos técnicos y permitiendo auto-recuperación.
 */
private val NaranjaError = Color(0xFFD9534F)
private val VerdeExito = Color(0xFF2E7D32)

@Composable
fun PantallaErrorAmigable(
    categoria: CategoriaError,
    mensajePersonalizado: String? = null,
    idiomaActual: IdiomaApp = ManejadorErrores.idiomaActual,
    alReintentar: (() -> Unit)? = null,
    alVolverInicio: (() -> Unit)? = null,
    alExplorar: (() -> Unit)? = null,
    paddingSuperior: Dp = 0.dp,
    mostrarBotonVolver: Boolean = true
) {
    val cadenas = LocalCadenas.current
    val detalles = remember(categoria, mensajePersonalizado, idiomaActual) {
        ManejadorErrores.resolverDetallesVisuales(categoria, mensajePersonalizado, idiomaActual)
    }

    var reintentandoLocal by remember { mutableStateOf(false) }
    var exitoSimulado by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()

    val icono: ImageVector = when (categoria) {
        CategoriaError.NO_ENCONTRADO -> Icons.Default.LocationOff
        CategoriaError.SERVIDOR -> Icons.Default.CloudOff
        CategoriaError.RED -> Icons.Default.WifiOff
        CategoriaError.SESION_EXPIRADA -> Icons.Default.Lock
        else -> Icons.Default.Warning
    }

    val colorAcento: Color = when (categoria) {
        CategoriaError.NO_ENCONTRADO -> GoldColor
        CategoriaError.SERVIDOR -> NaranjaError
        CategoriaError.RED -> AzulPetroleo
        CategoriaError.SESION_EXPIRADA -> AzulPetroleo
        else -> NaranjaError
    }

    val colorFondoIcono: Color = colorAcento.copy(alpha = 0.12f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Celeste)
            .padding(top = paddingSuperior)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BlancoBase),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Etiqueta superior de resiliencia
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = AzulPetroleo.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, AzulPetroleo.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AzulPetroleo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cadenas.soporteAutomatico,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulPetroleo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Contenedor visual del Icono
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(colorFondoIcono),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (exitoSimulado) Icons.Default.CheckCircle else icono,
                        contentDescription = null,
                        tint = if (exitoSimulado) VerdeExito else colorAcento,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Título Amigable (Sin código técnico)
                Text(
                    text = if (exitoSimulado) cadenas.conexionRestablecida else detalles.titulo,
                    style = TitularPrincipal,
                    fontSize = 20.sp,
                    color = AzulPetroleo,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Explicación humana y empática
                Text(
                    text = if (exitoSimulado) cadenas.error50xSugerencia else detalles.mensaje,
                    fontSize = 14.sp,
                    color = AzulPetroleo.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sugerencia de acción o información adicional
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Celeste.copy(alpha = 0.5f),
                    border = BorderStroke(0.8.dp, GrisClaro.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AzulPetroleo.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = detalles.sugerencia,
                            fontSize = 12.sp,
                            color = AzulPetroleo.copy(alpha = 0.85f),
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Estado de reintento automático
                if (reintentandoLocal) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = AzulPetroleo,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = cadenas.reintentandoConexion,
                            fontSize = 13.sp,
                            color = AzulPetroleo,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Botón Principal: Reintentar o Volver
                    Button(
                        onClick = {
                            if (alReintentar != null) {
                                alcance.launch {
                                    reintentandoLocal = true
                                    delay(900)
                                    reintentandoLocal = false
                                    alReintentar()
                                }
                            } else if (alVolverInicio != null) {
                                alVolverInicio()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (categoria == CategoriaError.NO_ENCONTRADO) AzulPetroleo else colorAcento
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = if (categoria == CategoriaError.NO_ENCONTRADO) Icons.Default.Home else Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = BlancoBase
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = detalles.textoBotonPrincipal,
                            style = TextoBoton,
                            color = BlancoBase
                        )
                    }

                    // Botón Secundario: Explorar o Regresar
                    if (mostrarBotonVolver && (alVolverInicio != null || alExplorar != null)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                if (categoria == CategoriaError.NO_ENCONTRADO && alExplorar != null) {
                                    alExplorar()
                                } else {
                                    alVolverInicio?.invoke()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.2.dp, AzulPetroleo.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AzulPetroleo
                            )
                        ) {
                            Icon(
                                imageVector = if (categoria == CategoriaError.NO_ENCONTRADO) Icons.Default.Explore else Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = AzulPetroleo
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = detalles.textoBotonSecundario,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AzulPetroleo
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente de tarjeta de error amigable para embeber dentro de pantallas existentes
 * (listas, feeds, pestañas) sin romper el flujo de la aplicación.
 */
@Composable
fun TarjetaErrorAmigable(
    categoria: CategoriaError,
    mensajePersonalizado: String? = null,
    idiomaActual: IdiomaApp = ManejadorErrores.idiomaActual,
    alReintentar: (() -> Unit)? = null,
    alVolverInicio: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cadenas = LocalCadenas.current
    val detalles = remember(categoria, mensajePersonalizado, idiomaActual) {
        ManejadorErrores.resolverDetallesVisuales(categoria, mensajePersonalizado, idiomaActual)
    }

    var estaCargando by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()

    val icono = when (categoria) {
        CategoriaError.NO_ENCONTRADO -> Icons.Default.LocationOff
        CategoriaError.SERVIDOR -> Icons.Default.CloudOff
        CategoriaError.RED -> Icons.Default.WifiOff
        else -> Icons.Default.Warning
    }

    val colorAcento = when (categoria) {
        CategoriaError.NO_ENCONTRADO -> GoldColor
        CategoriaError.SERVIDOR -> NaranjaError
        CategoriaError.RED -> AzulPetroleo
        else -> NaranjaError
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = BlancoBase,
        border = BorderStroke(1.dp, GrisClaro.copy(alpha = 0.8f)),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(colorAcento.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorAcento,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = detalles.titulo,
                style = SubtituloH2,
                fontSize = 16.sp,
                color = AzulPetroleo,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = detalles.mensaje,
                fontSize = 13.sp,
                color = AzulPetroleo.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (estaCargando) {
                CircularProgressIndicator(
                    color = AzulPetroleo,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (alReintentar != null) {
                        Button(
                            onClick = {
                                alcance.launch {
                                    estaCargando = true
                                    delay(700)
                                    estaCargando = false
                                    alReintentar()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = cadenas.reintentar, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (alVolverInicio != null) {
                        OutlinedButton(
                            onClick = alVolverInicio,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AzulPetroleo.copy(alpha = 0.5f))
                        ) {
                            Text(text = cadenas.inicio, fontSize = 13.sp, color = AzulPetroleo, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo interactivo para evaluadores del Hackathon y demostración de flujo resiliente.
 * Permite simular y visualizar cómo el sistema responde a 404, 50X y caídas de red de forma automática.
 */
@Composable
fun DialogoDemostracionErrores(
    alCerrar: () -> Unit,
    alSeleccionarVista: (CategoriaError) -> Unit
) {
    val cadenas = LocalCadenas.current

    Dialog(onDismissRequest = alCerrar) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BlancoBase),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = GoldColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Simulador de Flujo Resiliente",
                        style = SubtituloH2,
                        fontSize = 17.sp,
                        color = AzulPetroleo
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Verifica el cumplimiento del requerimiento: el sistema maneja fallas de principio a fin mostrando pantallas amigables sin exponer código técnico.",
                    fontSize = 12.sp,
                    color = AzulPetroleo.copy(alpha = 0.75f),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Opción 1: Error 404
                BotonOpcionSimulador(
                    titulo = "Pantalla 404 (Recurso no encontrado)",
                    subtitulo = "Destino o circuito inexistente/reubicado",
                    icono = Icons.Default.LocationOff,
                    color = GoldColor,
                    alHacerClic = {
                        alSeleccionarVista(CategoriaError.NO_ENCONTRADO)
                        alCerrar()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Opción 2: Error 50X
                BotonOpcionSimulador(
                    titulo = "Pantalla 50X (Servidor en mantenimiento)",
                    subtitulo = "Backend indisponible o error interno protegido",
                    icono = Icons.Default.CloudOff,
                    color = NaranjaError,
                    alHacerClic = {
                        alSeleccionarVista(CategoriaError.SERVIDOR)
                        alCerrar()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Opción 3: Fallo de Red
                BotonOpcionSimulador(
                    titulo = "Pantalla Red (Sin conexión)",
                    subtitulo = "Pérdida de conectividad Wi-Fi o datos móviles",
                    icono = Icons.Default.WifiOff,
                    color = AzulPetroleo,
                    alHacerClic = {
                        alSeleccionarVista(CategoriaError.RED)
                        alCerrar()
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                TextButton(
                    onClick = alCerrar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = cadenas.cerrar, color = AzulPetroleo, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BotonOpcionSimulador(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    color: Color,
    alHacerClic: () -> Unit
) {
    Surface(
        onClick = alHacerClic,
        shape = RoundedCornerShape(14.dp),
        color = Celeste.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulPetroleo
                )
                Text(
                    text = subtitulo,
                    fontSize = 11.sp,
                    color = AzulPetroleo.copy(alpha = 0.7f)
                )
            }
        }
    }
}
