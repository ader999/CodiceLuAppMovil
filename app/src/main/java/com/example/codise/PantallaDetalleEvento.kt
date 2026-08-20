package com.example.codise

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Evento
import com.example.codise.receivers.ReceptorNotificacionesEvento
import com.example.codise.ui.theme.*
import com.example.codise.utils.AyudanteNotificaciones
import com.example.codise.utils.aUrlCompleta
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PantallaDetalleEvento(
    evento: Evento,
    viewModelEventos: ViewModelEventos? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (evento.imagen != null) {
            AsyncImage(
                model = evento.imagen.aUrlCompleta(),
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

        Column(modifier = Modifier.padding(20.dp)) {
            val contexto = LocalContext.current
            
            // Lanzador de permiso de notificaciones
            val lanzador = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { concedido ->
                if (concedido) {
                    programarNotificaciones(contexto, evento)
                } else {
                    Toast.makeText(contexto, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModelEventos?.registrarAsistencia(evento.id)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            programarNotificaciones(contexto, evento)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Asistir", color = AzulPetroleo, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(evento.ubicacion)}")
                        val mapaIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapaIntent.setPackage("com.google.android.apps.maps")
                        if (mapaIntent.resolveActivity(contexto.packageManager) != null) {
                            contexto.startActivity(mapaIntent)
                        } else {
                            contexto.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Place, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Llegar", color = GoldColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = evento.titulo,
                    fontSize = 24.sp,
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElementoDetalle(icono = Icons.Default.LocationOn, etiqueta = "Ubicación", valor = evento.ubicacion)
            ElementoDetalle(icono = Icons.Default.CalendarMonth, etiqueta = "Fecha", valor = "${evento.fechaInicio.take(10)} al ${evento.fechaFin.take(10)}")
            if (evento.empresaNombre != null) {
                ElementoDetalle(icono = Icons.Default.Person, etiqueta = "Organizado por", valor = evento.empresaNombre)
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
                text = evento.descripcion,
                fontSize = 15.sp,
                color = NegroPuro.copy(alpha = 0.8f),
                lineHeight = 22.sp
            )
            
            if (evento.precioEntrada != "0.00" && !evento.esGratuito) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Precio: C$ ${evento.precioEntrada}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun programarNotificaciones(contexto: Context, evento: Evento) {
    val ayudanteNotificaciones = AyudanteNotificaciones(contexto)
    ayudanteNotificaciones.crearCanalNotificaciones()

    val administradorAlarmas = contexto.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    
    try {
        val fechaEvento = LocalDate.parse(evento.fechaInicio.take(10), formateador)
        val fechaHoraEvento = LocalDateTime.of(fechaEvento, LocalTime.of(8, 0))
        
        val recordatorios = listOf(
            Triple(fechaHoraEvento.minusDays(2), "Recordatorio de Evento", "Faltan 2 días para: ${evento.titulo}"),
            Triple(fechaHoraEvento.minusDays(1), "Recordatorio de Evento", "Mañana es el evento: ${evento.titulo}"),
            Triple(fechaHoraEvento, "¡Es hoy!", "Hoy es: ${evento.titulo}. ¡No te lo pierdas!")
        )

        recordatorios.forEachIndexed { indice, recordatorio ->
            val dispararEn = recordatorio.first.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            if (dispararEn > System.currentTimeMillis()) {
                val intento = Intent(contexto, ReceptorNotificacionesEvento::class.java).apply {
                    putExtra("title", recordatorio.second)
                    putExtra("message", recordatorio.third)
                    if (indice == 2) {
                        putExtra("location", evento.ubicacion)
                    }
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    contexto, 
                    evento.id * 10 + indice, 
                    intento, 
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (administradorAlarmas.canScheduleExactAlarms()) {
                        administradorAlarmas.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dispararEn, pendingIntent)
                    } else {
                        administradorAlarmas.set(AlarmManager.RTC_WAKEUP, dispararEn, pendingIntent)
                    }
                } else {
                    administradorAlarmas.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dispararEn, pendingIntent)
                }
            }
        }
        
        Toast.makeText(contexto, "Recordatorios programados para el evento", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(contexto, "Error al programar recordatorios", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ElementoDetalle(icono: androidx.compose.ui.graphics.vector.ImageVector, etiqueta: String, valor: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icono, null, tint = GoldColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = etiqueta, fontSize = 12.sp, color = GrisClaro, fontWeight = FontWeight.Medium)
            Text(text = valor, fontSize = 15.sp, color = NegroPuro)
        }
    }
}
