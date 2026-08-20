package com.example.codise.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object UtilidadesCompartir {
    fun compartirBitmap(contexto: Context, bitmap: Bitmap, nombreArchivo: String = "visita_codise.png") {
        try {
            val rutaCache = File(contexto.cacheDir, "shared_images")
            rutaCache.mkdirs()
            val archivo = File(rutaCache, nombreArchivo)
            val flujoSalida = FileOutputStream(archivo)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, flujoSalida)
            flujoSalida.close()

            val uriContenido = FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)

            if (uriContenido != null) {
                val intentoCompartir = Intent().apply {
                    action = Intent.ACTION_SEND
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(uriContenido, contexto.contentResolver.getType(uriContenido))
                    putExtra(Intent.EXTRA_STREAM, uriContenido)
                    type = "image/png"
                }
                contexto.startActivity(Intent.createChooser(intentoCompartir, "Compartir visita"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
