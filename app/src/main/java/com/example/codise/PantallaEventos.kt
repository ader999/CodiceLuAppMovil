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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Evento
import com.example.codise.ui.theme.*
import com.example.codise.utils.aUrlCompleta
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun PantallaEventos(
    viewModel: ViewModelEventos,
    puedeSubir: Boolean,
    alHacerClicEnSubir: () -> Unit,
    alHacerClicEnEvento: (Evento) -> Unit,
    modoVista: Int, // 2 para Lista, 3 para Calendario
    paddingSuperior: Dp = 0.dp
) {
    val estadoUi by viewModel.estadoUi

    Scaffold(
        floatingActionButton = {
            if (puedeSubir) {
                FloatingActionButton(
                    onClick = alHacerClicEnSubir,
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
            when (estadoUi) {
                is EstadoUiEventos.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldColor)
                }
                is EstadoUiEventos.Error -> {
                    Text(
                        text = (estadoUi as EstadoUiEventos.Error).mensaje,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(top = paddingSuperior)
                    )
                }
                is EstadoUiEventos.Exito -> {
                    val eventos = (estadoUi as EstadoUiEventos.Exito).eventos
                    if (eventos.isEmpty()) {
                        EstadoEventosVacio(modifier = Modifier.padding(top = paddingSuperior))
                    } else {
                        if (modoVista == 2) {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 16.dp, top = paddingSuperior + 8.dp, end = 16.dp, bottom = 76.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(eventos) { evento ->
                                    TarjetaEvento(evento, alHacerClicEnEvento)
                                }
                            }
                        } else {
                            VistaCalendarioEventos(
                                eventos = eventos,
                                alHacerClicEnEvento = alHacerClicEnEvento,
                                modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = paddingSuperior + 8.dp, bottom = 76.dp)
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun EstadoEventosVacio(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
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
fun TarjetaEvento(evento: Evento, alHacerClicEnVerMas: (Evento) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            if (evento.imagen != null) {
                AsyncImage(
                    model = evento.imagen.aUrlCompleta(),
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
                        text = evento.titulo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo,
                        modifier = Modifier.weight(1f)
                    )
                    if (evento.esGratuito) {
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
                    Text(text = evento.ubicacion, fontSize = 14.sp, color = NegroPuro)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = GoldColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${evento.fechaInicio.take(10)} - ${evento.fechaFin.take(10)}",
                        fontSize = 14.sp,
                        color = NegroPuro
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = evento.descripcion,
                    fontSize = 14.sp,
                    color = NegroPuro.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { alHacerClicEnVerMas(evento) },
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
fun VistaCalendarioEventos(
    eventos: List<Evento>,
    alHacerClicEnEvento: (Evento) -> Unit,
    modifier: Modifier = Modifier
) {
    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
    val mesActual = remember(fechaSeleccionada) { YearMonth.from(fechaSeleccionada) }
    
    val diasEnMes = mesActual.lengthOfMonth()
    val primerDiaDelMes = mesActual.atDay(1).dayOfWeek.value % 7 // 0 para Domingo
    
    val eventosPorFecha = remember(eventos) {
        eventos.groupBy { 
            try {
                LocalDate.parse(it.fechaInicio.take(10))
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(modifier = modifier) {
        // Encabezado Calendario
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { fechaSeleccionada = fechaSeleccionada.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, null, tint = AzulPetroleo)
            }
            Text(
                text = mesActual.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AzulPetroleo
            )
            IconButton(onClick = { fechaSeleccionada = fechaSeleccionada.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, null, tint = AzulPetroleo)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Días de la semana
        Row(modifier = Modifier.fillMaxWidth()) {
            val dias = listOf("Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab")
            dias.forEach { dia ->
                Text(
                    text = dia,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisClaro
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cuadrícula del Calendario
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(280.dp)
        ) {
            items(primerDiaDelMes) {
                Box(modifier = Modifier.aspectRatio(1f))
            }

            items(diasEnMes) { dia ->
                val fecha = mesActual.atDay(dia + 1)
                val estaSeleccionado = fecha == fechaSeleccionada
                val tieneEventos = eventosPorFecha.containsKey(fecha)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(if (estaSeleccionado) AzulPetroleo else Color.Transparent)
                        .clickable { fechaSeleccionada = fecha },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (dia + 1).toString(),
                            color = if (estaSeleccionado) Color.White else NegroPuro,
                            fontSize = 14.sp,
                            fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Normal
                        )
                        if (tieneEventos) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(if (estaSeleccionado) Color.White else GoldColor)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = GrisClaro.copy(alpha = 0.3f))

        // Lista de eventos para la fecha seleccionada
        val eventosFechaSeleccionada = eventosPorFecha[fechaSeleccionada] ?: emptyList()
        if (eventosFechaSeleccionada.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No hay eventos para esta fecha", color = GrisClaro, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 76.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(eventosFechaSeleccionada) { evento ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { alHacerClicEnEvento(evento) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (evento.imagen != null) {
                                AsyncImage(
                                    model = evento.imagen.aUrlCompleta(),
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = evento.titulo, fontWeight = FontWeight.Bold, color = AzulPetroleo, maxLines = 1)
                                Text(text = evento.ubicacion, fontSize = 12.sp, color = NegroPuro.copy(alpha = 0.6f), maxLines = 1)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = GoldColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
