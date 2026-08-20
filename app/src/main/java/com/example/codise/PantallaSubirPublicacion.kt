package com.example.codise

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.codise.data.Ciudad
import com.example.codise.data.Evento
import com.example.codise.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSubirPublicacion(
    ciudades: List<Ciudad>,
    eventos: List<Evento>,
    alVolver: () -> Unit,
    alSubir: (String, Int?, Int?, Int?, List<Uri>) -> Unit,
    estaSubiendo: Boolean,
    subidaExitosa: Boolean,
    mensajeError: String? = null
) {
    var descripcion by remember { mutableStateOf("") }
    var urisImagenesSeleccionadas by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var idCiudadSeleccionada by remember { mutableStateOf<Int?>(null) }
    var idEventoSeleccionado by remember { mutableStateOf<Int?>(null) }
    var esPublicacionEvento by remember { mutableStateOf(false) }

    var menuCiudadExpandido by remember { mutableStateOf(false) }
    var menuEventoExpandido by remember { mutableStateOf(false) }

    val contexto = LocalContext.current
    val clienteUbicacion = remember { LocationServices.getFusedLocationProviderClient(contexto) }

    val lanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.size > 10) {
            Toast.makeText(contexto, "Máximo 10 imágenes permitidas", Toast.LENGTH_SHORT).show()
            urisImagenesSeleccionadas = uris.take(10)
        } else {
            urisImagenesSeleccionadas = uris
        }
    }

    LaunchedEffect(subidaExitosa) {
        if (subidaExitosa) {
            alVolver()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (estaSubiendo) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    color = GoldColor,
                    trackColor = AzulPetroleo.copy(alpha = 0.5f)
                )
            }

            // Selector de Imagen / Carrusel
            if (urisImagenesSeleccionadas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AzulPetroleo.copy(alpha = 0.05f))
                        .clickable { lanzador.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = AzulPetroleo, modifier = Modifier.size(48.dp))
                        Text("Seleccionar hasta 10 imágenes", color = AzulPetroleo, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                val estadoPaginador = rememberPagerState(pageCount = { urisImagenesSeleccionadas.size })
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    HorizontalPager(state = estadoPaginador, modifier = Modifier.fillMaxSize()) { pagina ->
                        AsyncImage(
                            model = urisImagenesSeleccionadas[pagina],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Botón para eliminar imagen actual
                    IconButton(
                        onClick = {
                            urisImagenesSeleccionadas = urisImagenesSeleccionadas.filterIndexed { indice, _ -> indice != estadoPaginador.currentPage }
                        },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color.White)
                    }

                    // Botón para volver a seleccionar
                    Button(
                        onClick = { lanzador.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo.copy(alpha = 0.7f))
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cambiar", fontSize = 12.sp)
                    }

                    // Indicadores
                    if (urisImagenesSeleccionadas.size > 1) {
                        Row(
                            Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(urisImagenesSeleccionadas.size) { iteracion ->
                                val color = if (estadoPaginador.currentPage == iteracion) Color.White else Color.White.copy(alpha = 0.5f)
                                Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(6.dp))
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Escribe una descripción...") },
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = esPublicacionEvento, 
                    onCheckedChange = { esPublicacionEvento = it },
                    colors = CheckboxDefaults.colors(checkedColor = AzulPetroleo)
                )
                Text("Es una publicación sobre un evento", color = AzulPetroleo, fontWeight = FontWeight.Medium)
            }

            if (esPublicacionEvento) {
                ExposedDropdownMenuBox(
                    expanded = menuEventoExpandido,
                    onExpandedChange = { menuEventoExpandido = !menuEventoExpandido }
                ) {
                    OutlinedTextField(
                        value = eventos.find { it.id == idEventoSeleccionado }?.titulo ?: "Seleccionar Evento",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Evento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuEventoExpandido) },
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
                        expanded = menuEventoExpandido,
                        onDismissRequest = { menuEventoExpandido = false }
                    ) {
                        eventos.forEach { evento ->
                            DropdownMenuItem(
                                text = { Text(evento.titulo) },
                                onClick = {
                                    idEventoSeleccionado = evento.id
                                    idCiudadSeleccionada = evento.ciudad
                                    menuEventoExpandido = false
                                }
                            )
                        }
                    }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = menuCiudadExpandido,
                    onExpandedChange = { menuCiudadExpandido = !menuCiudadExpandido }
                ) {
                    OutlinedTextField(
                        value = ciudades.find { it.id == idCiudadSeleccionada }?.nombre ?: "Seleccionar Ciudad (Opcional)",
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
            }

            if (mensajeError != null) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (esPublicacionEvento && idEventoSeleccionado != null) {
                        val evento = eventos.find { it.id == idEventoSeleccionado }
                        if (evento?.latitud != null && evento.longitud != null) {
                            verificarUbicacionYSubir(
                                contexto,
                                clienteUbicacion,
                                evento.latitud,
                                evento.longitud,
                                {
                                    alSubir(descripcion, idCiudadSeleccionada, null, idEventoSeleccionado, urisImagenesSeleccionadas)
                                },
                                {
                                    Toast.makeText(contexto, "No pareces estar en la ubicación del evento. Verifica tu GPS.", Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            alSubir(descripcion, idCiudadSeleccionada, null, idEventoSeleccionado, urisImagenesSeleccionadas)
                        }
                    } else {
                        alSubir(descripcion, idCiudadSeleccionada, null, null, urisImagenesSeleccionadas)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                enabled = !estaSubiendo && (descripcion.isNotEmpty() || urisImagenesSeleccionadas.isNotEmpty())
            ) {
                if (estaSubiendo) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("PUBLICAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun verificarUbicacionYSubir(
    contexto: Context,
    clienteUbicacion: com.google.android.gms.location.FusedLocationProviderClient,
    latitudEvento: Double,
    longitudEvento: Double,
    alTenerExito: () -> Unit,
    alFallar: () -> Unit
) {
    if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        alTenerExito()
        return
    }

    try {
        clienteUbicacion.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { ubicacion ->
                if (ubicacion != null) {
                    val resultados = FloatArray(1)
                    Location.distanceBetween(ubicacion.latitude, ubicacion.longitude, latitudEvento, longitudEvento, resultados)
                    if (resultados[0] < 500) { // Umbral de 500 metros
                        alTenerExito()
                    } else {
                        alFallar()
                    }
                } else {
                    alFallar()
                }
            }
            .addOnFailureListener {
                alFallar()
            }
    } catch (e: SecurityException) {
        alTenerExito()
    }
}
