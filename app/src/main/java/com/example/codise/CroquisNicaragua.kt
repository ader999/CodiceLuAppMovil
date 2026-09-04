package com.example.codise

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.sp
import com.example.codise.data.Ciudad
import com.example.codise.ui.theme.*
import com.example.codise.utils.LocalCadenas
import kotlin.math.roundToInt

// Coordenadas geográficas límite de Nicaragua con margen para visualización
private const val MIN_LON = -87.85
private const val MAX_LON = -83.00
private const val MIN_LAT = 10.55
private const val MAX_LAT = 15.20

private const val RANGE_LON = MAX_LON - MIN_LON // 4.85
private const val RANGE_LAT = MAX_LAT - MIN_LAT // 4.65
private const val FACTOR_ASPECTO_GEO = 0.976f   // cos(12.5° latitud)

// Polígono detallado del contorno de Nicaragua (Longitud, Latitud)
private val FronteraNicaragua = listOf(
    // 1. Península de Cosigüina y Golfo de Fonseca
    Pair(-87.68, 12.98), // Punta Cosigüina
    Pair(-87.56, 13.06), // Costa norte de Cosigüina
    Pair(-87.45, 13.02),
    Pair(-87.35, 12.92), // Entrada Estero Real
    Pair(-87.20, 12.98),
    Pair(-87.05, 13.12), // Frontera El Guasaule

    // 2. Frontera norte con Honduras
    Pair(-86.92, 13.24), // Somotillo
    Pair(-86.80, 13.36),
    Pair(-86.66, 13.52), // Somoto / El Espino
    Pair(-86.58, 13.66), // Ocotal / Dipilto
    Pair(-86.42, 13.82),
    Pair(-86.20, 13.98), // Jalapa (pico norte)
    Pair(-85.95, 14.04), // Wiwilí
    Pair(-85.65, 14.20), // Bocay / Bosawas
    Pair(-85.25, 14.42), // Río Coco medio
    Pair(-84.80, 14.62),
    Pair(-84.30, 14.80), // Waspam
    Pair(-83.75, 14.95), // Bajo Río Coco
    Pair(-83.14, 15.02), // Cabo Gracias a Dios (extremo noreste)

    // 3. Costa Caribe (Este) - Ajustada para reflejar la costa real y asegurar que las ciudades caribeñas queden dentro
    Pair(-83.22, 14.68), // Sandy Bay
    Pair(-83.30, 14.03), // Puerto Cabezas (Bilwi)
    Pair(-83.48, 13.55), // Wawa Bar / Prinzapolka
    Pair(-83.50, 13.05), // Karawala / Desembocadura Río Grande
    Pair(-83.52, 12.50), // Laguna de Perlas
    Pair(-83.58, 12.00), // Bahía de Bluefields / El Bluff (asegura que Bluefields quede claramente dentro del croquis)
    Pair(-83.58, 11.60), // Monkey Point
    Pair(-83.68, 11.20), // Punta Gorda
    Pair(-83.68, 10.92), // San Juan de Nicaragua (Greytown)

    // 4. Frontera sur con Costa Rica (Río San Juan y frontera terrestre)
    Pair(-83.95, 10.74), // Bajo Río San Juan
    Pair(-84.30, 10.70), // Curva sur del Río San Juan (punto más al sur)
    Pair(-84.60, 10.82), // Boca de San Carlos
    Pair(-84.85, 11.02), // Al sur de San Carlos
    Pair(-85.20, 11.12), // Peñas Blancas
    Pair(-85.55, 11.22), // Sapoa
    Pair(-85.73, 11.21), // Bahía de Salinas (encuentro con el Pacífico)

    // 5. Costa del Pacífico (Suroeste)
    Pair(-85.86, 11.25), // San Juan del Sur
    Pair(-86.04, 11.44), // Tola / Gigante
    Pair(-86.28, 11.63), // Chacocente
    Pair(-86.51, 11.78), // Masachapa / Pochomil
    Pair(-86.74, 12.04), // Montelimar
    Pair(-86.95, 12.30), // Puerto Sandino
    Pair(-87.08, 12.37), // Las Peñitas / Poneloya
    Pair(-87.24, 12.48), // Corinto / Paso Caballos
    Pair(-87.38, 12.68), // Jiquilillo
    Pair(-87.48, 12.85)  // Padre Ramos / Mechapa
)

// Lago Cocibolca (Gran Lago de Nicaragua)
// La costa occidental se sitúa al este de Granada (la ciudad queda firmemente en tierra firme a -85.960°)
private val LagoCocibolca = listOf(
    Pair(-85.800, 11.930), // Costa oeste frente a Granada (deja a Granada en tierra firme con margen visual claro)
    Pair(-85.760, 11.990), // Hacia Malacatoya / El Paso
    Pair(-85.680, 12.040), // Malacatoya
    Pair(-85.450, 12.040), // Tecolostote
    Pair(-85.250, 11.880), // Costa Juigalpa
    Pair(-85.050, 11.680), // Puerto Díaz
    Pair(-84.950, 11.450), // Morrito / San Miguelito
    Pair(-84.770, 11.140), // San Carlos (extremo sureste)
    Pair(-84.920, 11.080), // Costa sur
    Pair(-85.150, 11.120), // Papaturro
    Pair(-85.450, 11.250), // Cárdenas / Sapoa
    Pair(-85.680, 11.420), // La Virgen
    Pair(-85.780, 11.600), // San Jorge / Rivas
    Pair(-85.800, 11.780), // Ochomogo
    Pair(-85.810, 11.880)  // Las Isletas al sureste de Granada
)

// Isla de Ometepe (dentro del Lago Cocibolca)
private val IslaOmetepe = listOf(
    Pair(-85.60, 11.54), // Volcán Concepción
    Pair(-85.54, 11.53), // Istmo
    Pair(-85.48, 11.48), // Volcán Maderas
    Pair(-85.46, 11.43),
    Pair(-85.53, 11.45),
    Pair(-85.57, 11.49),
    Pair(-85.65, 11.52)
)

// Lago Xolotlán (Lago de Managua)
private val LagoXolotlan = listOf(
    Pair(-86.42, 12.52), // Extremo norte
    Pair(-86.26, 12.46), // San Francisco Libre
    Pair(-86.12, 12.32), // Hacia Tipitapa
    Pair(-86.08, 12.20), // Salida río Tipitapa
    Pair(-86.20, 12.16), // Frente costero de Managua
    Pair(-86.32, 12.18), // Acahualinca
    Pair(-86.46, 12.28), // Mateare
    Pair(-86.58, 12.38), // Base Volcán Momotombo / Nagarote
    Pair(-86.55, 12.48)  // Bahía noroeste
)

// Coordenadas verificadas de la Red de Ciudades Creativas y de referencia de Nicaragua
private val CoordenadasConocidas = mapOf(
    "león" to Pair(12.4379, -86.8780),
    "leon" to Pair(12.4379, -86.8780),
    "masaya" to Pair(11.9744, -86.0942),
    "granada" to Pair(11.9300, -85.9600),
    "estelí" to Pair(13.0919, -86.3538),
    "esteli" to Pair(13.0919, -86.3538),
    "managua" to Pair(12.1364, -86.2514),
    "matagalpa" to Pair(12.9256, -85.9178),
    "bluefields" to Pair(12.0137, -83.7635),
    "san juan de oriente" to Pair(11.9061, -86.0740),
    "juigalpa" to Pair(12.1063, -85.3646),
    "nagarote" to Pair(12.2659, -86.5647),
    "somoto" to Pair(13.4808, -86.5821),
    "jinotega" to Pair(13.0969, -86.0021),
    "ocotal" to Pair(13.6322, -86.4752),
    "chinandega" to Pair(12.6294, -87.1311),
    "corinto" to Pair(12.4819, -87.1733),
    "rivas" to Pair(11.4372, -85.8263),
    "san carlos" to Pair(11.1236, -84.7779),
    "corn island" to Pair(12.1744, -83.0489),
    "puerto cabezas" to Pair(14.0351, -83.3888),
    "bilwi" to Pair(14.0351, -83.3888),
    "ciudad darío" to Pair(12.7314, -86.1242),
    "dario" to Pair(12.7314, -86.1242),
    "jinotepe" to Pair(11.8500, -86.1994),
)

// Lista de respaldo para mostrar los puntos de las ciudades creativas principales
private val CiudadesPorDefecto = listOf(
    Ciudad(1, "León", "Ciudad Creativa del Aprendizaje", null, 12.4379, -86.8780, emptyList(), emptyList(), emptyList()),
    Ciudad(2, "Masaya", "Ciudad Creativa de las Artes Populares", null, 11.9744, -86.0942, emptyList(), emptyList(), emptyList()),
    Ciudad(3, "Granada", "Ciudad Creativa del Diseño", null, 11.9300, -85.9600, emptyList(), emptyList(), emptyList()),
    Ciudad(4, "Estelí", "Ciudad Creativa del Muralismo", null, 13.0919, -86.3538, emptyList(), emptyList(), emptyList()),
    Ciudad(5, "Managua", "Ciudad Creativa Multicultural", null, 12.1364, -86.2514, emptyList(), emptyList(), emptyList()),
)

/**
 * Obtiene las coordenadas geográficas (latitud, longitud) de una ciudad.
 * Garantiza que ciudades críticas como Granada y Bluefields se posicionen correctamente
 * sobre tierra firme y dentro de las fronteras cartográficas del croquis.
 */
fun obtenerCoordenadasCiudad(ciudad: Ciudad): Pair<Double, Double>? {
    val clave = ciudad.nombre.trim().lowercase()
    if (clave.contains("granada")) {
        // Asegurar la posición exacta en tierra firme al oeste de la costa del Lago Cocibolca
        return Pair(11.9300, -85.9600)
    }
    if (clave.contains("bluefields")) {
        // Posición exacta en tierra firme dentro de la bahía caribeña
        return Pair(12.0137, -83.7635)
    }
    if ((ciudad.latitudCentro != 0.0) && (ciudad.longitudCentro != 0.0)) {
        return Pair(ciudad.latitudCentro, ciudad.longitudCentro)
    }
    return CoordenadasConocidas[clave]
        ?: CoordenadasConocidas.entries.find { clave.contains(it.key) }?.value
}

/**
 * Convierte una lista de puntos en una curva cerrada suave utilizando
 * interpolación cuadrática de Bézier con puntos medios.
 */
private fun construirPathSuave(puntos: List<Offset>): Path {
    val path = Path()
    if (puntos.isEmpty()) return path
    if (puntos.size == 1) {
        path.moveTo(puntos[0].x, puntos[0].y)
        return path
    }
    val n = puntos.size
    val primerPuntoMedioX = (puntos[n - 1].x + puntos[0].x) / 2f
    val primerPuntoMedioY = (puntos[n - 1].y + puntos[0].y) / 2f
    path.moveTo(primerPuntoMedioX, primerPuntoMedioY)

    for (i in 0 until n) {
        val actual = puntos[i]
        val siguiente = puntos[(i + 1) % n]
        val puntoMedioX = (actual.x + siguiente.x) / 2f
        val puntoMedioY = (actual.y + siguiente.y) / 2f
        path.quadraticTo(actual.x, actual.y, puntoMedioX, puntoMedioY)
    }
    path.close()
    return path
}

@Composable
fun CroquisNicaragua(
    ciudades: List<Ciudad>,
    modifier: Modifier = Modifier,
    ciudadSeleccionada: Ciudad? = null,
    alSeleccionarCiudad: (Ciudad?) -> Unit = {},
    alHacerClicEnCiudad: (Ciudad) -> Unit,
    alHacerClicEnPin: (Ciudad) -> Unit,
) {
    val cadenas = LocalCadenas.current
    val densidad = LocalDensity.current

    // Estados de transformación táctil (Pellizcar para ampliar / arrastrar)
    var escala by remember { mutableFloatStateOf(1f) }
    var offsetCentro by remember { mutableStateOf(Offset.Zero) }

    // Animación de pulso continuo para los marcadores
    val transicionInfinita = rememberInfiniteTransition(label = "pulsoMarcador")
    val escalaPulso by transicionInfinita.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "escalaPulso"
    )
    val alfaPulso by transicionInfinita.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alfaPulso"
    )

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F7FB),
                        Color(0xFFE8F2F8)
                    )
                )
            )
            .border(1.dp, Celeste.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
    ) {
        val anchoDisponible = constraints.maxWidth.toFloat()
        val altoDisponible = constraints.maxHeight.toFloat()

        // Márgenes para asegurar visibilidad del croquis y de los marcadores
        val paddingHorizontal = with(densidad) { 14.dp.toPx() }
        val paddingVertical = with(densidad) { 10.dp.toPx() }

        val anchoUtil = (anchoDisponible - (paddingHorizontal * 2)).coerceAtLeast(10f)
        val altoUtil = (altoDisponible - (paddingVertical * 2)).coerceAtLeast(10f)

        val aspectoDeseado = ((RANGE_LON * FACTOR_ASPECTO_GEO) / RANGE_LAT).toFloat()

        val anchoMapa: Float
        val altoMapa: Float
        if ((anchoUtil / altoUtil) > aspectoDeseado) {
            altoMapa = altoUtil
            anchoMapa = altoUtil * aspectoDeseado
        } else {
            anchoMapa = anchoUtil
            altoMapa = anchoUtil / aspectoDeseado
        }

        val offsetX = (anchoDisponible - anchoMapa) / 2f
        val offsetY = (altoDisponible - altoMapa) / 2f

        // Manejador del gesto multitáctil de pellizcar para ampliar (Pinch to Zoom hasta 4x - 5x)
        val estadoTransformable = rememberTransformableState { zoomChange, panChange, _ ->
            val nuevaEscala = (escala * zoomChange).coerceIn(1f, 5.0f)
            escala = nuevaEscala
            if (nuevaEscala <= 1.02f) {
                offsetCentro = Offset.Zero
            } else {
                val maxPanX = (anchoMapa * (nuevaEscala - 1f)) / 2f
                val maxPanY = (altoMapa * (nuevaEscala - 1f)) / 2f
                offsetCentro = Offset(
                    x = (offsetCentro.x + panChange.x).coerceIn(-maxPanX, maxPanX),
                    y = (offsetCentro.y + panChange.y).coerceIn(-maxPanY, maxPanY)
                )
            }
        }

        // Función de proyección de coordenadas geográficas a pantalla
        val proyectar: (Double, Double) -> Offset = remember(anchoMapa, altoMapa, offsetX, offsetY) {
            { lon, lat ->
                val normX = ((lon - MIN_LON) / RANGE_LON).toFloat().coerceIn(0f, 1f)
                val normY = ((MAX_LAT - lat) / RANGE_LAT).toFloat().coerceIn(0f, 1f)
                Offset(
                    x = offsetX + normX * anchoMapa,
                    y = offsetY + normY * altoMapa
                )
            }
        }

        // Construir los Paths geométricos
        val pathPais = remember(anchoMapa, altoMapa, offsetX, offsetY) {
            construirPathSuave(FronteraNicaragua.map { proyectar(it.first, it.second) })
        }
        val pathSombraPais = remember(anchoMapa, altoMapa, offsetX, offsetY) {
            val desplazamiento = Offset(2f, 3f)
            construirPathSuave(FronteraNicaragua.map { proyectar(it.first, it.second) + desplazamiento })
        }
        val pathLagoCocibolca = remember(anchoMapa, altoMapa, offsetX, offsetY) {
            construirPathSuave(LagoCocibolca.map { proyectar(it.first, it.second) })
        }
        val pathIslaOmetepe = remember(anchoMapa, altoMapa, offsetX, offsetY) {
            construirPathSuave(IslaOmetepe.map { proyectar(it.first, it.second) })
        }
        val pathLagoXolotlan = remember(anchoMapa, altoMapa, offsetX, offsetY) {
            construirPathSuave(LagoXolotlan.map { proyectar(it.first, it.second) })
        }

        // Colores cartográficos armonizados con la paleta de Codice
        val colorTierra = Color(0xFFFBFDFC)
        val colorTierraBorde = AzulPetroleo.copy(alpha = 0.65f)
        val colorLago = Color(0xFFBFE0F7)
        val colorLagoBorde = AzulPetroleo.copy(alpha = 0.40f)

        // Contenedor transformable que soporta ampliación con los dedos (Pinch-to-zoom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .transformable(state = estadoTransformable)
        ) {
            // Capa del mapa con escala y traslación acelerada por GPU
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        translationX = offsetCentro.x
                        translationY = offsetCentro.y
                    }
            ) {
                // 1. Lienzo con el croquis de Nicaragua y sus lagos
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(escala) {
                            detectTapGestures(
                                onDoubleTap = { tapOffset ->
                                    if (escala > 1.5f) {
                                        escala = 1f
                                        offsetCentro = Offset.Zero
                                    } else {
                                        escala = 4.0f
                                        val centroX = anchoDisponible / 2f
                                        val centroY = altoDisponible / 2f
                                        val targetPanX = (centroX - tapOffset.x) * 3.0f
                                        val targetPanY = (centroY - tapOffset.y) * 3.0f
                                        val maxPanX = (anchoMapa * 3.0f) / 2f
                                        val maxPanY = (altoMapa * 3.0f) / 2f
                                        offsetCentro = Offset(
                                            x = targetPanX.coerceIn(-maxPanX, maxPanX),
                                            y = targetPanY.coerceIn(-maxPanY, maxPanY)
                                        )
                                    }
                                },
                                onTap = {
                                    alSeleccionarCiudad(null)
                                }
                            )
                        }
                        .pointerInput(escala) {
                            if (escala > 1.05f) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val maxPanX = (anchoMapa * (escala - 1f)) / 2f
                                    val maxPanY = (altoMapa * (escala - 1f)) / 2f
                                    offsetCentro = Offset(
                                        x = (offsetCentro.x + (dragAmount.x * escala)).coerceIn(-maxPanX, maxPanX),
                                        y = (offsetCentro.y + (dragAmount.y * escala)).coerceIn(-maxPanY, maxPanY)
                                    )
                                }
                            }
                        }
                ) {
                    // Sombra suave de la silueta del país
                    drawPath(pathSombraPais, color = Color(0x12000000))

                    // Relleno de la tierra de Nicaragua
                    drawPath(
                        path = pathPais,
                        brush = Brush.linearGradient(
                            colors = listOf(colorTierra, Color(0xFFEFF5F0)),
                            start = Offset(offsetX, offsetY),
                            end = Offset(offsetX + anchoMapa, offsetY + altoMapa)
                        )
                    )

                    // Borde exterior del país
                    drawPath(
                        path = pathPais,
                        color = colorTierraBorde,
                        style = Stroke(width = with(densidad) { 1.6.dp.toPx() })
                    )

                    // Gran Lago de Nicaragua (Cocibolca)
                    drawPath(pathLagoCocibolca, color = colorLago)
                    drawPath(
                        path = pathLagoCocibolca,
                        color = colorLagoBorde,
                        style = Stroke(width = with(densidad) { 1.1.dp.toPx() })
                    )

                    // Isla de Ometepe (dentro del Cocibolca)
                    drawPath(pathIslaOmetepe, color = colorTierra)
                    drawPath(
                        path = pathIslaOmetepe,
                        color = colorTierraBorde.copy(alpha = 0.5f),
                        style = Stroke(width = with(densidad) { 0.8.dp.toPx() })
                    )

                    // Lago de Managua (Xolotlán)
                    drawPath(pathLagoXolotlan, color = colorLago)
                    drawPath(
                        path = pathLagoXolotlan,
                        color = colorLagoBorde,
                        style = Stroke(width = with(densidad) { 1.1.dp.toPx() })
                    )
                }

                // 2. Puntos de las Ciudades Creativas superpuestos
                val listaCiudades = remember(ciudades) {
                    ciudades.ifEmpty { CiudadesPorDefecto }
                }

                listaCiudades.forEach { ciudad ->
                    val coords = obtenerCoordenadasCiudad(ciudad)
                    if (coords != null) {
                        val punto = proyectar(coords.second, coords.first)
                        val esSeleccionada = ciudadSeleccionada?.id == ciudad.id

                        val (offsetEtiquetaX, offsetEtiquetaY) = remember(ciudad.nombre) {
                            calcularOffsetEtiqueta(ciudad.nombre)
                        }

                        val nombreMostrado = remember(ciudad.nombre) {
                            if (ciudad.nombre.contains("San Juan de Oriente", ignoreCase = true)) "S. J. de Oriente"
                            else ciudad.nombre
                        }

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(punto.x.roundToInt(), punto.y.roundToInt())
                                }
                                .zIndex(if (esSeleccionada) 10f else 1f)
                                .graphicsLayer {
                                    // Contrarresta la escala para que los textos y pines mantengan
                                    // un tamaño compacto y constante en lugar de volverse gigantes en 4x
                                    val factorContraEscala = (1f + ((escala - 1f) * 0.08f)) / escala
                                    scaleX = factorContraEscala
                                    scaleY = factorContraEscala
                                }
                        ) {
                            val radioBase = if (esSeleccionada) 13.dp else 9.dp
                            val radioAnimado = radioBase * escalaPulso

                            // Anillo de pulso exterior animado
                            Box(
                                modifier = Modifier
                                    .size(radioAnimado * 2)
                                    .offset(
                                        x = -radioAnimado,
                                        y = -radioAnimado
                                    )
                                    .background(
                                        color = GoldColor.copy(alpha = if (esSeleccionada) alfaPulso else alfaPulso * 0.7f),
                                        shape = CircleShape
                                    )
                            )

                            // Pin central interactivo
                            Box(
                                modifier = Modifier
                                    .size(if (esSeleccionada) 15.dp else 10.dp)
                                    .offset(
                                        x = if (esSeleccionada) (-7.5).dp else (-5).dp,
                                        y = if (esSeleccionada) (-7.5).dp else (-5).dp
                                    )
                                    .shadow(2.dp, CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = if (esSeleccionada) listOf(Color(0xFFFFD54F), GoldColor)
                                            else listOf(GoldColor, Color(0xFF8C6D32))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (esSeleccionada) 1.8.dp else 1.dp,
                                        color = BlancoBase,
                                        shape = CircleShape
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (esSeleccionada) {
                                            alHacerClicEnCiudad(ciudad)
                                        } else {
                                            alSeleccionarCiudad(ciudad)
                                        }
                                    }
                            )

                            // Etiqueta flotante con el nombre de la ciudad
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (esSeleccionada) AzulPetroleo else BlancoBase.copy(alpha = 0.94f),
                                shadowElevation = if (esSeleccionada) 3.dp else 1.dp,
                                border = if (esSeleccionada) BorderStroke(1.dp, GoldColor) else BorderStroke(
                                    0.5.dp,
                                    GrisClaro.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier
                                    .offset(x = offsetEtiquetaX, y = offsetEtiquetaY)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        alSeleccionarCiudad(ciudad)
                                        alHacerClicEnCiudad(ciudad)
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 1.5.dp)
                                ) {
                                    if (esSeleccionada) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = GoldColor,
                                            modifier = Modifier.size(9.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = nombreMostrado,
                                        fontSize = 7.8.sp,
                                        fontWeight = if (esSeleccionada) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (esSeleccionada) BlancoBase else AzulPetroleo,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Decoraciones fijas superiores (no se ven afectadas por el zoom del mapa)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = null,
                tint = GoldColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "N",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AzulPetroleo.copy(alpha = 0.7f)
            )
        }

        Text(
            text = cadenas.nicaragua,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = AzulPetroleo.copy(alpha = 0.35f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        // 4. Controles táctiles de Zoom en la esquina superior derecha
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Botón directo para alternar ampliación rápida a 4x / 1x
            Surface(
                shape = RoundedCornerShape(7.dp),
                color = if (escala >= 3.8f) GoldColor else AzulPetroleo.copy(alpha = 0.88f),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .clickable {
                        if (escala >= 3.8f) {
                            escala = 1f
                            offsetCentro = Offset.Zero
                        } else {
                            escala = 4.0f
                            // Centrar en la zona de las ciudades del Pacífico
                            val centroX = anchoDisponible / 2f
                            val centroY = altoDisponible / 2f
                            val puntoPacifico = proyectar(-86.35, 12.25)
                            val targetPanX = (centroX - puntoPacifico.x) * 2.8f
                            val targetPanY = (centroY - puntoPacifico.y) * 2.8f
                            val maxPanX = (anchoMapa * 3.0f) / 2f
                            val maxPanY = (altoMapa * 3.0f) / 2f
                            offsetCentro = Offset(
                                x = targetPanX.coerceIn(-maxPanX, maxPanX),
                                y = targetPanY.coerceIn(-maxPanY, maxPanY)
                            )
                        }
                    }
            ) {
                Text(
                    text = if (escala >= 3.8f) "1x" else "4x",
                    color = BlancoBase,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Botón Zoom In [+]
            Surface(
                shape = CircleShape,
                color = BlancoBase.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                border = BorderStroke(0.5.dp, GrisClaro.copy(alpha = 0.5f)),
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        escala = (escala + 1.0f).coerceAtMost(5.0f)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = AzulPetroleo,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Botón Zoom Out [-]
            Surface(
                shape = CircleShape,
                color = BlancoBase.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                border = BorderStroke(0.5.dp, GrisClaro.copy(alpha = 0.5f)),
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        val nuevaEscala = (escala - 1.0f).coerceAtLeast(1f)
                        escala = nuevaEscala
                        if (nuevaEscala <= 1.05f) {
                            offsetCentro = Offset.Zero
                        } else {
                            val maxPanX = (anchoMapa * (nuevaEscala - 1f)) / 2f
                            val maxPanY = (altoMapa * (nuevaEscala - 1f)) / 2f
                            offsetCentro = Offset(
                                x = offsetCentro.x.coerceIn(-maxPanX, maxPanX),
                                y = offsetCentro.y.coerceIn(-maxPanY, maxPanY)
                            )
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "−",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo
                    )
                }
            }

            // Botón de restablecer vista al centro y escala 1x
            if (escala > 1.05f) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = CircleShape,
                    color = BlancoBase.copy(alpha = 0.95f),
                    shadowElevation = 2.dp,
                    border = BorderStroke(0.5.dp, GoldColor.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            escala = 1f
                            offsetCentro = Offset.Zero
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restablecer",
                            tint = GoldColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // 5. Barra interactiva en la parte inferior del mapa al seleccionar una ciudad
        AnimatedVisibility(
            visible = ciudadSeleccionada != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 8.dp, end = 8.dp, bottom = 6.dp)
        ) {
            ciudadSeleccionada?.let { ciudad ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlancoBase),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth(0.96f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { alHacerClicEnCiudad(ciudad) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = ciudad.nombre,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulPetroleo,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Toca para explorar",
                                    fontSize = 9.sp,
                                    color = AzulPetroleo.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Botón para ver circuitos
                            FilledTonalIconButton(
                                onClick = { alHacerClicEnPin(ciudad) },
                                modifier = Modifier.size(28.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = GoldColor.copy(alpha = 0.15f),
                                    contentColor = GoldColor
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = cadenas.circuitos,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Botón para ver detalle de la ciudad
                            FilledTonalIconButton(
                                onClick = { alHacerClicEnCiudad(ciudad) },
                                modifier = Modifier.size(28.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = AzulPetroleo.copy(alpha = 0.12f),
                                    contentColor = AzulPetroleo
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Detalle",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Cerrar selección
                            IconButton(
                                onClick = { alSeleccionarCiudad(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = GrisClaro,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subtítulo / Guía sutil cuando no hay ciudad seleccionada
        if (ciudadSeleccionada == null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (escala > 1.05f) "Arrastra para explorar · Doble toque 1x" else "Pellizca o toca '4x' para ampliar",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = AzulPetroleo.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Calcula el desplazamiento adecuado de la etiqueta de texto para evitar
 * que las ciudades cercanas se solapen entre sí.
 */
private fun calcularOffsetEtiqueta(nombre: String): Pair<Dp, Dp> {
    val clave = nombre.trim().lowercase()
    return when {
        // Managua: hacia arriba sobre las aguas del Lago Xolotlán
        clave.contains("managua") -> Pair((-18).dp, (-17).dp)
        // León: hacia el oeste hacia el océano Pacífico
        clave.contains("león") || clave.contains("leon") -> Pair((-42).dp, (-5).dp)
        // Nagarote: hacia el suroeste
        clave.contains("nagarote") -> Pair((-44).dp, 5.dp)
        // Masaya: hacia el suroeste
        clave.contains("masaya") -> Pair((-40).dp, 4.dp)
        // San Juan de Oriente: hacia el sur
        clave.contains("san juan") || clave.contains("oriente") -> Pair((-18).dp, 11.dp)
        // Granada: hacia arriba / noroeste en tierra firme (evita las aguas del Lago Cocibolca)
        clave.contains("granada") -> Pair((-16).dp, (-16).dp)
        // Juigalpa: hacia el este hacia Chontales
        clave.contains("juigalpa") -> Pair(9.dp, (-7).dp)
        // Estelí: hacia el noreste
        clave.contains("estelí") || clave.contains("esteli") -> Pair(8.dp, (-11).dp)
        // Matagalpa: hacia el este
        clave.contains("matagalpa") -> Pair(8.dp, (-6).dp)
        // Bluefields: hacia el oeste tierra adentro
        clave.contains("bluefields") -> Pair((-48).dp, (-6).dp)
        // Chinandega: hacia el oeste
        clave.contains("chinandega") || clave.contains("corinto") -> Pair((-52).dp, (-6).dp)
        else -> Pair(8.dp, (-6).dp)
    }
}
