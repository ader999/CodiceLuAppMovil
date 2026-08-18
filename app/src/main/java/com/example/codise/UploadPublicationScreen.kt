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
import com.example.codise.data.City
import com.example.codise.data.Event
import com.example.codise.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPublicationScreen(
    cities: List<City>,
    events: List<Event>,
    onBack: () -> Unit,
    onUpload: (String, Int?, Int?, Int?, List<Uri>) -> Unit,
    isUploading: Boolean,
    uploadSuccess: Boolean,
    errorMessage: String? = null
) {
    var descripcion by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedCityId by remember { mutableStateOf<Int?>(null) }
    var selectedEventId by remember { mutableStateOf<Int?>(null) }
    var isEventPublication by remember { mutableStateOf(false) }

    var cityExpanded by remember { mutableStateOf(false) }
    var eventExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.size > 10) {
            Toast.makeText(context, "Máximo 10 imágenes permitidas", Toast.LENGTH_SHORT).show()
            selectedImageUris = uris.take(10)
        } else {
            selectedImageUris = uris
        }
    }

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            onBack()
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
            if (isUploading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    color = GoldColor,
                    trackColor = AzulPetroleo.copy(alpha = 0.5f)
                )
            }

            // Image Selector / Carousel
            if (selectedImageUris.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AzulPetroleo.copy(alpha = 0.05f))
                        .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = AzulPetroleo, modifier = Modifier.size(48.dp))
                        Text("Seleccionar hasta 10 imágenes", color = AzulPetroleo, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { selectedImageUris.size })
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(
                            model = selectedImageUris[page],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Remove current image button
                    IconButton(
                        onClick = {
                            selectedImageUris = selectedImageUris.filterIndexed { index, _ -> index != pagerState.currentPage }
                        },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color.White)
                    }

                    // Re-select button
                    Button(
                        onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo.copy(alpha = 0.7f))
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cambiar", fontSize = 12.sp)
                    }

                    // Indicators
                    if (selectedImageUris.size > 1) {
                        Row(
                            Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(selectedImageUris.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
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
                    checked = isEventPublication, 
                    onCheckedChange = { isEventPublication = it },
                    colors = CheckboxDefaults.colors(checkedColor = AzulPetroleo)
                )
                Text("Es una publicación sobre un evento", color = AzulPetroleo, fontWeight = FontWeight.Medium)
            }

            if (isEventPublication) {
                ExposedDropdownMenuBox(
                    expanded = eventExpanded,
                    onExpandedChange = { eventExpanded = !eventExpanded }
                ) {
                    OutlinedTextField(
                        value = events.find { it.id == selectedEventId }?.titulo ?: "Seleccionar Evento",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Evento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventExpanded) },
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
                        expanded = eventExpanded,
                        onDismissRequest = { eventExpanded = false }
                    ) {
                        events.forEach { event ->
                            DropdownMenuItem(
                                text = { Text(event.titulo) },
                                onClick = {
                                    selectedEventId = event.id
                                    selectedCityId = event.ciudad
                                    eventExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = cityExpanded,
                    onExpandedChange = { cityExpanded = !cityExpanded }
                ) {
                    OutlinedTextField(
                        value = cities.find { it.id == selectedCityId }?.nombre ?: "Seleccionar Ciudad (Opcional)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ciudad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
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
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false }
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city.nombre) },
                                onClick = {
                                    selectedCityId = city.id
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isEventPublication && selectedEventId != null) {
                        val event = events.find { it.id == selectedEventId }
                        if (event?.latitud != null && event.longitud != null) {
                            checkLocationAndUpload(
                                context,
                                fusedLocationClient,
                                event.latitud,
                                event.longitud,
                                {
                                    onUpload(descripcion, selectedCityId, null, selectedEventId, selectedImageUris)
                                },
                                {
                                    Toast.makeText(context, "No pareces estar en la ubicación del evento. Verifica tu GPS.", Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            // Event has no coordinates, just upload
                            onUpload(descripcion, selectedCityId, null, selectedEventId, selectedImageUris)
                        }
                    } else {
                        onUpload(descripcion, selectedCityId, null, null, selectedImageUris)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                enabled = !isUploading && (descripcion.isNotEmpty() || selectedImageUris.isNotEmpty())
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("PUBLICAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}private fun checkLocationAndUpload(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    eventLat: Double,
    eventLon: Double,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        // No permission, we can't validate, so we let it through but maybe without "highlight"
        // For this task, we'll just allow it but log a warning.
        onSuccess()
        return
    }

    try {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(location.latitude, location.longitude, eventLat, eventLon, results)
                    if (results[0] < 500) { // 500 meters threshold
                        onSuccess()
                    } else {
                        onFailure()
                    }
                } else {
                    onFailure()
                }
            }
            .addOnFailureListener {
                onFailure()
            }
    } catch (e: SecurityException) {
        onSuccess()
    }
}
