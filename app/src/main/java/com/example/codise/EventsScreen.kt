package com.example.codise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Event
import com.example.codise.ui.theme.*
import com.example.codise.utils.toFullUrl

@Composable
fun EventsScreen(
    viewModel: EventsViewModel,
    canUpload: Boolean,
    onUploadClick: () -> Unit
) {
    val uiState by viewModel.uiState

    Scaffold(
        floatingActionButton = {
            if (canUpload) {
                FloatingActionButton(
                    onClick = onUploadClick,
                    containerColor = GoldColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Subir Evento")
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                is EventsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldColor)
                }
                is EventsUiState.Error -> {
                    Text(
                        text = (uiState as EventsUiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is EventsUiState.Success -> {
                    val events = (uiState as EventsUiState.Success).events
                    if (events.isEmpty()) {
                        EmptyEventsState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(events) { event ->
                                EventCard(event)
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun EmptyEventsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = AzulPetroleo.copy(alpha = 0.3f),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No hay eventos próximos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AzulPetroleo
        )
        Text(
            text = "Vuelve más tarde para ver nuevas actividades",
            fontSize = 14.sp,
            color = GrisClaro
        )
    }
}

@Composable
fun EventCard(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            if (event.imagen != null) {
                AsyncImage(
                    model = event.imagen.toFullUrl(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.titulo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo,
                        modifier = Modifier.weight(1f)
                    )
                    if (event.esGratuito) {
                        Surface(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "GRATIS",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = GoldColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = event.ubicacion, fontSize = 14.sp, color = NegroPuro)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = GoldColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.fechaInicio.take(10)} - ${event.fechaFin.take(10)}",
                        fontSize = 14.sp,
                        color = NegroPuro
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = event.descripcion,
                    fontSize = 14.sp,
                    color = NegroPuro.copy(alpha = 0.7f),
                    maxLines = 3
                )
            }
        }
    }
}
