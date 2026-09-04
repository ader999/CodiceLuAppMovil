package com.example.codise.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.codise.data.ServicioApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

fun String.aUrlCompleta(): String {
    return if (this.startsWith("/")) {
        ServicioApi.URL_BASE.removeSuffix("/") + this
    } else {
        this
    }
}

fun extraerIdVideoYoutube(url: String): String? {
    val patron = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
    val patronCompilado = java.util.regex.Pattern.compile(patron)
    val emparejador = patronCompilado.matcher(url)
    return if (emparejador.find()) {
        emparejador.group()
    } else {
        null
    }
}

fun obtenerUrlMiniaturaYoutube(idVideo: String): String {
    return "https://img.youtube.com/vi/$idVideo/hqdefault.jpg"
}

suspend fun Context.obtenerArchivoComprimidoDeUri(uri: Uri): File = withContext(Dispatchers.IO) {
    val flujoEntrada = contentResolver.openInputStream(uri)
    val bitmapOriginal = BitmapFactory.decodeStream(flujoEntrada)

    val archivo = File(cacheDir, "evento_img_${System.currentTimeMillis()}.jpg")
    val flujoSalida = FileOutputStream(archivo)

    val escala = if (bitmapOriginal.width > 1280 || bitmapOriginal.height > 1280) {
        val ladoMayor = maxOf(bitmapOriginal.width, bitmapOriginal.height)
        1280f / ladoMayor
    } else {
        1.0f
    }

    val bitmapFinal = if (escala < 1.0f) {
        Bitmap.createScaledBitmap(
            bitmapOriginal,
            (bitmapOriginal.width * escala).toInt(),
            (bitmapOriginal.height * escala).toInt(),
            true
        )
    } else {
        bitmapOriginal
    }

    bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 80, flujoSalida)
    flujoSalida.flush()
    flujoSalida.close()

    if (bitmapFinal != bitmapOriginal) bitmapFinal.recycle()
    bitmapOriginal.recycle()

    archivo
}
