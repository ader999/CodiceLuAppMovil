package com.example.codise

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Ciudad
import com.example.codise.data.SolicitudEvento
import com.example.codise.ui.theme.*
import com.example.codise.utils.UtilidadesUbicacion
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSubirEvento(
    ciudades: List<Ciudad>,
    alVolver: () -> Unit,
    alSubir: (SolicitudEvento, Uri?) -> Unit,
    estaSubiendo: Boolean,
    subidaExitosa: Boolean,
    paddingSuperior: Dp = 0.dp
) {
    val contexto = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var idCiudadSeleccionada by remember { mutableStateOf<Int?>(null) }
    var ubicacion by remember { mutableStateOf("") }
    var direccionOMaps by remember { mutableStateOf("") }

    // Imagen del evento
    var uriImagenSeleccionada by remember { mutableStateOf<Uri?>(null) }
    val lanzadorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uriImagenSeleccionada = uri
    }

    // Coordenadas detectadas y estado
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var latitudTexto by remember { mutableStateOf("") }
    var longitudTexto by remember { mutableStateOf("") }
    var buscandoCoordenadas by remember { mutableStateOf(false) }
    var errorCoordenadas by remember { mutableStateOf(false) }
    var mostrarEdicionManualCoordenadas by remember { mutableStateOf(false) }

    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var esGratuito by remember { mutableStateOf(true) }
    var precioEntrada by remember { mutableStateOf("0.00") }
    var cupoMaximo by remember { mutableStateOf("") }
    var estaActivo by remember { mutableStateOf(true) }

    var menuCiudadExpandido by remember { mutableStateOf(false) }
    var mostrarDialogoFechaInicio by remember { mutableStateOf(false) }
    var mostrarDialogoFechaFin by remember { mutableStateOf(false) }

    LaunchedEffect(subidaExitosa) {
        if (subidaExitosa) {
            alVolver()
        }
    }

    // Efecto para detectar latitud y longitud automáticamente de enlaces o direcciones
    LaunchedEffect(direccionOMaps, idCiudadSeleccionada) {
        val entrada = direccionOMaps.trim()
        if (entrada.isEmpty()) {
            latitud = null
            longitud = null
            latitudTexto = ""
            longitudTexto = ""
            buscandoCoordenadas = false
            errorCoordenadas = false
            return@LaunchedEffect
        }

        // 1. Detección inmediata si contiene coordenadas directas o @lat,lng
        val directas = UtilidadesUbicacion.extraerCoordenadasDirectas(entrada)
        if (directas != null) {
            latitud = directas.first
            longitud = directas.second
            latitudTexto = directas.first.toString()
            longitudTexto = directas.second.toString()
            buscandoCoordenadas = false
            errorCoordenadas = false
            return@LaunchedEffect
        }

        // 2. Si es URL acortada o dirección escrita, esperar pausa del usuario
        delay(600)
        buscandoCoordenadas = true
        errorCoordenadas = false

        val ciudadSeleccionada = ciudades.find { it.id == idCiudadSeleccionada }?.nombre
        val resultado = UtilidadesUbicacion.obtenerCoordenadas(contexto, entrada, ciudadSeleccionada)
        buscandoCoordenadas = false
        if (resultado != null) {
            latitud = resultado.first
            longitud = resultado.second
            latitudTexto = resultado.first.toString()
            longitudTexto = resultado.second.toString()
            errorCoordenadas = false
        } else {
            errorCoordenadas = true
        }
    }

    val coloresDatePicker = DatePickerDefaults.colors(
        containerColor = BlancoBase,
        titleContentColor = AzulPetroleo,
        headlineContentColor = AzulPetroleo,
        weekdayContentColor = AzulPetroleo,
        subheadContentColor = AzulPetroleo,
        navigationContentColor = AzulPetroleo,
        yearContentColor = AzulPetroleo,
        currentYearContentColor = GoldColor,
        selectedYearContentColor = BlancoBase,
        selectedYearContainerColor = AzulPetroleo,
        dayContentColor = NegroPuro,
        disabledDayContentColor = GrisClaro.copy(alpha = 0.4f),
        selectedDayContentColor = BlancoBase,
        selectedDayContainerColor = AzulPetroleo,
        disabledSelectedDayContentColor = BlancoBase.copy(alpha = 0.5f),
        disabledSelectedDayContainerColor = AzulPetroleo.copy(alpha = 0.38f),
        todayContentColor = AzulPetroleo,
        todayDateBorderColor = GoldColor,
        dayInSelectionRangeContentColor = BlancoBase,
        dayInSelectionRangeContainerColor = AzulPetroleo.copy(alpha = 0.2f),
        dividerColor = GrisClaro.copy(alpha = 0.3f),
        dateTextFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AzulPetroleo,
            unfocusedBorderColor = GrisClaro,
            focusedLabelColor = AzulPetroleo,
            cursorColor = AzulPetroleo,
            focusedTextColor = AzulPetroleo,
            unfocusedTextColor = AzulPetroleo
        )
    )

    if (mostrarDialogoFechaInicio) {
        val initialMillis = remember(fechaInicio) {
            if (fechaInicio.isNotEmpty()) {
                try {
                    LocalDate.parse(fechaInicio).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }
            } else {
                System.currentTimeMillis()
            }
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { mostrarDialogoFechaInicio = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            fechaInicio = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        }
                        mostrarDialogoFechaInicio = false
                    }
                ) {
                    Text("Aceptar", color = AzulPetroleo, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoFechaInicio = false }) {
                    Text("Cancelar", color = AzulPetroleo.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = coloresDatePicker
        ) {
            DatePicker(
                state = datePickerState,
                colors = coloresDatePicker
            )
        }
    }

    if (mostrarDialogoFechaFin) {
        val initialMillis = remember(fechaFin) {
            if (fechaFin.isNotEmpty()) {
                try {
                    LocalDate.parse(fechaFin).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }
            } else if (fechaInicio.isNotEmpty()) {
                try {
                    LocalDate.parse(fechaInicio).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }
            } else {
                System.currentTimeMillis()
            }
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { mostrarDialogoFechaFin = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            fechaFin = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        }
                        mostrarDialogoFechaFin = false
                    }
                ) {
                    Text("Aceptar", color = AzulPetroleo, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoFechaFin = false }) {
                    Text("Cancelar", color = AzulPetroleo.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = coloresDatePicker
        ) {
            DatePicker(
                state = datePickerState,
                colors = coloresDatePicker
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = paddingSuperior + 8.dp, bottom = 76.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 80.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Subir Nuevo Evento",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AzulPetroleo
            )

            // Selector de Imagen de Portada del Evento
            if (uriImagenSeleccionada == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AzulPetroleo.copy(alpha = 0.05f))
                        .clickable {
                            lanzadorImagen.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = AzulPetroleo,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Imagen del Evento (Opcional)",
                            color = AzulPetroleo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Toca para seleccionar imagen de portada",
                            color = AzulPetroleo.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = uriImagenSeleccionada,
                        contentDescription = "Foto de portada del evento",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Botón para eliminar imagen
                    IconButton(
                        onClick = { uriImagenSeleccionada = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar imagen",
                            tint = Color.White
                        )
                    }

                    // Botón para cambiar imagen
                    Button(
                        onClick = {
                            lanzadorImagen.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cambiar", fontSize = 12.sp)
                    }
                }
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del Evento") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulPetroleo,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = AzulPetroleo,
                    cursorColor = AzulPetroleo,
                    focusedTextColor = AzulPetroleo,
                    unfocusedTextColor = AzulPetroleo
                )
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulPetroleo,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = AzulPetroleo,
                    cursorColor = AzulPetroleo,
                    focusedTextColor = AzulPetroleo,
                    unfocusedTextColor = AzulPetroleo
                )
            )

            ExposedDropdownMenuBox(
                expanded = menuCiudadExpandido,
                onExpandedChange = { menuCiudadExpandido = !menuCiudadExpandido }
            ) {
                OutlinedTextField(
                    value = ciudades.find { it.id == idCiudadSeleccionada }?.nombre ?: "Seleccionar Ciudad",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ciudad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCiudadExpandido) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPetroleo,
                        unfocusedBorderColor = GrisClaro,
                        focusedLabelColor = AzulPetroleo,
                        focusedTextColor = AzulPetroleo,
                        unfocusedTextColor = AzulPetroleo
                    )
                )
                ExposedDropdownMenu(
                    expanded = menuCiudadExpandido,
                    onDismissRequest = { menuCiudadExpandido = false }
                ) {
                    ciudades.forEach { ciudad ->
                        DropdownMenuItem(
                            text = { Text(ciudad.nombre) },
                            onClick = {
                                idCiudadSeleccionada = ciudad.id
                                menuCiudadExpandido = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación Específica / Nombre del Lugar") },
                placeholder = { Text("Ej. Costado oeste del Parque Central") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulPetroleo,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = AzulPetroleo,
                    cursorColor = AzulPetroleo,
                    focusedTextColor = AzulPetroleo,
                    unfocusedTextColor = AzulPetroleo
                )
            )

            // Campo de direcciones / Google Maps para latitud y longitud
            OutlinedTextField(
                value = direccionOMaps,
                onValueChange = { direccionOMaps = it },
                label = { Text("Dirección o enlace de Google Maps") },
                placeholder = { Text("Pega enlace de Maps, dirección o coordenadas") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AzulPetroleo
                    )
                },
                trailingIcon = {
                    if (direccionOMaps.isNotEmpty()) {
                        IconButton(onClick = { direccionOMaps = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar campo",
                                tint = AzulPetroleo
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulPetroleo,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = AzulPetroleo,
                    cursorColor = AzulPetroleo,
                    focusedTextColor = AzulPetroleo,
                    unfocusedTextColor = AzulPetroleo
                )
            )

            // Indicador de coordenadas detectadas
            if (latitud != null && longitud != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFF81C784)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Coordenadas GPS detectadas",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Lat: ${"%.6f".format(latitud)}, Lng: ${"%.6f".format(longitud)}",
                                fontSize = 13.sp,
                                color = NegroPuro.copy(alpha = 0.85f)
                            )
                        }
                        TextButton(
                            onClick = { mostrarEdicionManualCoordenadas = !mostrarEdicionManualCoordenadas },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (mostrarEdicionManualCoordenadas) "Ocultar" else "Ajustar",
                                fontSize = 12.sp,
                                color = AzulPetroleo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (buscandoCoordenadas) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = AzulPetroleo
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Obteniendo coordenadas de Google Maps...",
                        fontSize = 12.sp,
                        color = AzulPetroleo
                    )
                }
            } else if (errorCoordenadas && direccionOMaps.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "No se pudieron detectar coordenadas automáticamente.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { mostrarEdicionManualCoordenadas = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Ingresar manual",
                            fontSize = 12.sp,
                            color = AzulPetroleo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Edición manual de coordenadas (si el usuario pulsa Ajustar/Ingresar manual)
            if (mostrarEdicionManualCoordenadas) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitudTexto,
                        onValueChange = {
                            latitudTexto = it
                            latitud = it.toDoubleOrNull()
                        },
                        label = { Text("Latitud") },
                        placeholder = { Text("11.9744") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulPetroleo,
                            unfocusedBorderColor = GrisClaro,
                            focusedLabelColor = AzulPetroleo,
                            cursorColor = AzulPetroleo,
                            focusedTextColor = AzulPetroleo,
                            unfocusedTextColor = AzulPetroleo
                        )
                    )
                    OutlinedTextField(
                        value = longitudTexto,
                        onValueChange = {
                            longitudTexto = it
                            longitud = it.toDoubleOrNull()
                        },
                        label = { Text("Longitud") },
                        placeholder = { Text("-86.0942") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulPetroleo,
                            unfocusedBorderColor = GrisClaro,
                            focusedLabelColor = AzulPetroleo,
                            cursorColor = AzulPetroleo,
                            focusedTextColor = AzulPetroleo,
                            unfocusedTextColor = AzulPetroleo
                        )
                    )
                }
            }

            // Selector de Fechas
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha Inicio") },
                        placeholder = { Text("YYYY-MM-DD") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Seleccionar fecha de inicio",
                                tint = AzulPetroleo
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulPetroleo,
                            unfocusedBorderColor = GrisClaro,
                            focusedLabelColor = AzulPetroleo,
                            cursorColor = AzulPetroleo,
                            focusedTextColor = AzulPetroleo,
                            unfocusedTextColor = AzulPetroleo
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { mostrarDialogoFechaInicio = true }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha Fin") },
                        placeholder = { Text("YYYY-MM-DD") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Seleccionar fecha de fin",
                                tint = AzulPetroleo
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulPetroleo,
                            unfocusedBorderColor = GrisClaro,
                            focusedLabelColor = AzulPetroleo,
                            cursorColor = AzulPetroleo,
                            focusedTextColor = AzulPetroleo,
                            unfocusedTextColor = AzulPetroleo
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { mostrarDialogoFechaFin = true }
                    )
                }
            }

            // Precio de entrada y checkbox "Es gratuito"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (esGratuito) "0.00" else precioEntrada,
                    onValueChange = { precioEntrada = it },
                    enabled = !esGratuito,
                    label = { Text("Precio Entrada") },
                    placeholder = { Text("0.00") },
                    prefix = {
                        Text(
                            text = "C$ ",
                            color = if (esGratuito) GrisClaro else AzulPetroleo,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1.2f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPetroleo,
                        unfocusedBorderColor = GrisClaro,
                        focusedLabelColor = AzulPetroleo,
                        cursorColor = AzulPetroleo,
                        focusedTextColor = AzulPetroleo,
                        unfocusedTextColor = AzulPetroleo,
                        disabledBorderColor = GrisClaro.copy(alpha = 0.5f),
                        disabledLabelColor = GrisClaro,
                        disabledTextColor = GrisClaro.copy(alpha = 0.8f)
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            esGratuito = !esGratuito
                            if (esGratuito) {
                                precioEntrada = "0.00"
                            } else if (precioEntrada == "0.00") {
                                precioEntrada = ""
                            }
                        }
                ) {
                    Checkbox(
                        checked = esGratuito,
                        onCheckedChange = { gratuito ->
                            esGratuito = gratuito
                            if (gratuito) {
                                precioEntrada = "0.00"
                            } else if (precioEntrada == "0.00") {
                                precioEntrada = ""
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = AzulPetroleo)
                    )
                    Text(
                        text = "Es gratuito",
                        color = AzulPetroleo,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            // Cupo Máximo
            OutlinedTextField(
                value = cupoMaximo,
                onValueChange = { nuevoCupo ->
                    if (nuevoCupo.all { it.isDigit() }) {
                        cupoMaximo = nuevoCupo
                    }
                },
                label = { Text("Cupo Máximo (Opcional)") },
                placeholder = { Text("Ej. 100 (vacío para cupo ilimitado)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = AzulPetroleo
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulPetroleo,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = AzulPetroleo,
                    cursorColor = AzulPetroleo,
                    focusedTextColor = AzulPetroleo,
                    unfocusedTextColor = AzulPetroleo
                )
            )

            // Switch Evento Activo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Evento Activo",
                        color = AzulPetroleo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Estará activo y visible en la agenda pública",
                        fontSize = 12.sp,
                        color = AzulPetroleo.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = estaActivo,
                    onCheckedChange = { estaActivo = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BlancoBase,
                        checkedTrackColor = AzulPetroleo
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    idCiudadSeleccionada?.let { idCiudad ->
                        val ubicacionFinal = ubicacion.trim().ifEmpty {
                            if (direccionOMaps.isNotBlank() && !direccionOMaps.startsWith("http")) {
                                direccionOMaps.trim()
                            } else {
                                ciudades.find { it.id == idCiudad }?.nombre ?: "Ubicación por confirmar"
                            }
                        }

                        alSubir(
                            SolicitudEvento(
                                titulo = titulo.trim(),
                                descripcion = descripcion.trim(),
                                ciudad = idCiudad,
                                fechaInicio = "${fechaInicio}T00:00:00Z",
                                fechaFin = "${fechaFin}T23:59:59Z",
                                ubicacion = ubicacionFinal,
                                precioEntrada = if (esGratuito) "0.00" else precioEntrada.ifBlank { "0.00" },
                                esGratuito = esGratuito,
                                cupoMaximo = cupoMaximo.toIntOrNull(),
                                latitud = latitud,
                                longitud = longitud,
                                estaActivo = estaActivo
                            ),
                            uriImagenSeleccionada
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                enabled = (!estaSubiendo) &&
                        titulo.isNotBlank() &&
                        (idCiudadSeleccionada != null) &&
                        fechaInicio.isNotBlank() &&
                        fechaFin.isNotBlank() &&
                        (ubicacion.isNotBlank() || direccionOMaps.isNotBlank()) &&
                        (esGratuito || precioEntrada.isNotBlank())
            ) {
                if (estaSubiendo) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("PUBLICAR EVENTO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
