package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Event
import com.example.codise.ui.theme.*
import com.example.codise.utils.toFullUrl
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun EventsScreen(
    viewModel: EventsViewModel,
    canUpload: Boolean,
    onUploadClick: () -> Unit,
    onEventClick: (Event) -> Unit,
    viewMode: Int // 2 for List, 3 for Calendar
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
                        if (viewMode == 2) {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(events) { event ->
                                    EventCard(event, onEventClick)
                                }
                            }
                        } else {
                            EventsCalendarView(events, onEventClick)
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
fun EventCard(event: Event, onSeeMoreClick: (Event) -> Unit) {
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
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onSeeMoreClick(event) },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Ver más", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun EventsCalendarView(events: List<Event>, onEventClick: (Event) -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMonth = remember(selectedDate) { YearMonth.from(selectedDate) }
    
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // 0 for Sunday
    
    val eventsByDate = remember(events) {
        events.groupBy { 
            try {
                LocalDate.parse(it.fechaInicio.take(10))
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Calendar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { selectedDate = selectedDate.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, null, tint = AzulPetroleo)
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AzulPetroleo
            )
            IconButton(onClick = { selectedDate = selectedDate.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, null, tint = AzulPetroleo)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days of week
        Row(modifier = Modifier.fillMaxWidth()) {
            val days = listOf("Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab")
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisClaro
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(280.dp)
        ) {
            // Empty slots for days before the first day of the month
            items(firstDayOfMonth) {
                Box(modifier = Modifier.aspectRatio(1f))
            }

            items(daysInMonth) { day ->
                val date = currentMonth.atDay(day + 1)
                val isSelected = date == selectedDate
                val hasEvents = eventsByDate.containsKey(date)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) AzulPetroleo else Color.Transparent)
                        .clickable { selectedDate = date },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (day + 1).toString(),
                            color = if (isSelected) Color.White else NegroPuro,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (hasEvents) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else GoldColor)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = GrisClaro.copy(alpha = 0.3f))

        // Selected Date Events List
        val selectedDateEvents = eventsByDate[selectedDate] ?: emptyList()
        if (selectedDateEvents.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No hay eventos para esta fecha", color = GrisClaro, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedDateEvents) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onEventClick(event) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (event.imagen != null) {
                                AsyncImage(
                                    model = event.imagen.toFullUrl(),
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = event.titulo, fontWeight = FontWeight.Bold, color = AzulPetroleo, maxLines = 1)
                                Text(text = event.ubicacion, fontSize = 12.sp, color = NegroPuro.copy(alpha = 0.6f), maxLines = 1)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = GoldColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
