package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Evento", color = GoldColor, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPetroleo)
            )
        },
        containerColor = Celeste
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (event.imagen != null) {
                AsyncImage(
                    model = event.imagen.toFullUrl(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(GrisClaro.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = AzulPetroleo.copy(alpha = 0.2f), modifier = Modifier.size(80.dp))
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = event.titulo,
                            fontSize = 24.sp,
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
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailItem(icon = Icons.Default.LocationOn, label = "Ubicación", value = event.ubicacion)
                    DetailItem(icon = Icons.Default.CalendarMonth, label = "Fecha", value = "${event.fechaInicio.take(10)} al ${event.fechaFin.take(10)}")
                    if (event.empresaNombre != null) {
                        DetailItem(icon = Icons.Default.Person, label = "Organizado por", value = event.empresaNombre)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = GrisClaro.copy(alpha = 0.5f))

                    Text(
                        text = "Descripción",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.descripcion,
                        fontSize = 15.sp,
                        color = NegroPuro.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                    
                    if (event.precioEntrada != "0.00" && !event.esGratuito) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Precio: C$ ${event.precioEntrada}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, null, tint = GoldColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = GrisClaro, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 15.sp, color = NegroPuro)
        }
    }
}
