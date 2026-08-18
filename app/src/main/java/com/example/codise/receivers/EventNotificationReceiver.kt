package com.example.codise.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.codise.utils.NotificationHelper

class EventNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Recordatorio de Evento"
        val message = intent.getStringExtra("message") ?: "¡El evento está por comenzar!"
        val location = intent.getStringExtra("location")
        
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message, location)
    }
}
