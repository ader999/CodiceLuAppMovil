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
import com.example.codise.data.Event
import com.example.codise.receivers.EventNotificationReceiver
import com.example.codise.ui.theme.*
import com.example.codise.utils.NotificationHelper
import com.example.codise.utils.toFullUrl
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventDetailScreen(
    event: Event,
    eventsViewModel: EventsViewModel? = null
) {
    Column(
        modifier = Modifier
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

        Column(modifier = Modifier.padding(20.dp)) {
            val context = LocalContext.current
            
            // Notification Permission Launcher
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    scheduleNotifications(context, event)
                } else {
                    Toast.makeText(context, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        eventsViewModel?.registerAttendance(event.id)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            scheduleNotifications(context, event)
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
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(event.ubicacion)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun scheduleNotifications(context: Context, event: Event) {
    val notificationHelper = NotificationHelper(context)
    notificationHelper.createNotificationChannel()

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    
    try {
        val eventDate = LocalDate.parse(event.fechaInicio.take(10), formatter)
        val eventDateTime = LocalDateTime.of(eventDate, LocalTime.of(8, 0)) // 8:00 AM of the day
        
        val reminders = listOf(
            Triple(eventDateTime.minusDays(2), "Recordatorio de Evento", "Faltan 2 días para: ${event.titulo}"),
            Triple(eventDateTime.minusDays(1), "Recordatorio de Evento", "Mañana es el evento: ${event.titulo}"),
            Triple(eventDateTime, "¡Es hoy!", "Hoy es: ${event.titulo}. ¡No te lo pierdas!")
        )

        reminders.forEachIndexed { index, reminder ->
            val triggerAt = reminder.first.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            if (triggerAt > System.currentTimeMillis()) {
                val intent = Intent(context, EventNotificationReceiver::class.java).apply {
                    putExtra("title", reminder.second)
                    putExtra("message", reminder.third)
                    if (index == 2) { // Day of the event
                        putExtra("location", event.ubicacion)
                    }
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context, 
                    event.id * 10 + index, 
                    intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            }
        }
        
        Toast.makeText(context, "Recordatorios programados para el evento", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al programar recordatorios", Toast.LENGTH_SHORT).show()
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
