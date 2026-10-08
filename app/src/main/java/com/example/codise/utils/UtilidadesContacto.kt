package com.example.codise.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UtilidadesContacto {
    fun abrirWhatsApp(
        contexto: Context,
        numeroOEnlace: String,
        mensaje: String = "Hola, me gustaría comunicarme con ustedes..."
    ) {
        try {
            val intent: Intent = when {
                numeroOEnlace.startsWith("http://", ignoreCase = true) || numeroOEnlace.startsWith("https://", ignoreCase = true) -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse(numeroOEnlace))
                }
                else -> {
                    val soloDigitos = numeroOEnlace.replace(Regex("[^0-9]"), "")
                    val numeroConCodigo = if (soloDigitos.length == 8) "505$soloDigitos" else soloDigitos
                    val url = "https://api.whatsapp.com/send?phone=$numeroConCodigo&text=${Uri.encode(mensaje)}"
                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            contexto.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(contexto, "No se pudo abrir WhatsApp: ${e.localizedMessage ?: "error"}", Toast.LENGTH_SHORT).show()
        }
    }
}
