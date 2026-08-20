package com.example.codise.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.codise.MainActivity

class AyudanteNotificaciones(private val contexto: Context) {

    companion object {
        const val ID_CANAL = "event_notifications"
        const val NOMBRE_CANAL = "Notificaciones de Eventos"
    }

    fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val canal = NotificationChannel(ID_CANAL, NOMBRE_CANAL, importancia).apply {
                description = "Recordatorios de eventos a los que asistirás"
            }
            val administradorNotificaciones: NotificationManager =
                contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            administradorNotificaciones.createNotificationChannel(canal)
        }
    }

    fun mostrarNotificacion(titulo: String, mensaje: String, ubicacionEvento: String? = null) {
        val intent = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            contexto, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val constructor = NotificationCompat.Builder(contexto, ID_CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Agregar botón "Llegar" si se proporciona ubicación
        if (ubicacionEvento != null) {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(ubicacionEvento)}"))
            val mapPendingIntent = PendingIntent.getActivity(
                contexto, 1, mapIntent, PendingIntent.FLAG_IMMUTABLE
            )
            constructor.addAction(android.R.drawable.ic_menu_directions, "Llegar", mapPendingIntent)
        }

        val administradorNotificaciones = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    contexto,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                administradorNotificaciones.notify(System.currentTimeMillis().toInt(), constructor.build())
            }
        } else {
            administradorNotificaciones.notify(System.currentTimeMillis().toInt(), constructor.build())
        }
    }
}
