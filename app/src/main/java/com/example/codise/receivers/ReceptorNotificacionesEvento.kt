package com.example.codise.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.codise.utils.AyudanteNotificaciones

class ReceptorNotificacionesEvento : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("title") ?: "Recordatorio de Evento"
        val mensaje = intent.getStringExtra("message") ?: "¡El evento está por comenzar!"
        val ubicacion = intent.getStringExtra("location")
        
        val ayudanteNotificaciones = AyudanteNotificaciones(context)
        ayudanteNotificaciones.mostrarNotificacion(titulo, mensaje, ubicacion)
    }
}
