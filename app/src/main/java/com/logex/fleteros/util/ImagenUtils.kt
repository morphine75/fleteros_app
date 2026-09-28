package com.logex.fleteros.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private const val MAX_ANCHO_O_ALTO = 1600
private const val CALIDAD_JPEG = 70

/**
 * Lee una imagen desde su Uri (galeria o camara), la redimensiona si hace
 * falta y la comprime a JPEG, devolviendola lista como parte multipart
 * para subir por Retrofit. Devuelve null si la imagen no se pudo leer.
 */
fun comprimirImagenComoParte(context: Context, uri: Uri, nombreCampo: String, indice: Int): MultipartBody.Part? {
    val bitmapOriginal = decodificarBitmap(context, uri) ?: return null
    val bitmapEscalado = escalarBitmap(bitmapOriginal)

    val outputStream = ByteArrayOutputStream()
    bitmapEscalado.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, outputStream)
    val bytes = outputStream.toByteArray()

    val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
    val nombreArchivo = "comprobante_$indice.jpg"
    return MultipartBody.Part.createFormData(nombreCampo, nombreArchivo, requestBody)
}

private fun decodificarBitmap(context: Context, uri: Uri): Bitmap? {
    return context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input)
    }
}

/**
 * Crea un archivo temporal vacio en la carpeta de cache de la app y
 * devuelve su Uri "compartible" (via FileProvider) para pasarselo a
 * la app de Camara, que va a escribir la foto ahi adentro.
 */
fun crearArchivoTemporalImagen(context: Context): Uri {
    val carpeta = File(context.cacheDir, "fotos_temp").apply { mkdirs() }
    val archivo = File.createTempFile("foto_", ".jpg", carpeta)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}

private fun escalarBitmap(original: Bitmap): Bitmap {
    val ancho = original.width
    val alto = original.height
    if (ancho <= MAX_ANCHO_O_ALTO && alto <= MAX_ANCHO_O_ALTO) return original

    val ratio = if (ancho > alto) {
        MAX_ANCHO_O_ALTO.toFloat() / ancho
    } else {
        MAX_ANCHO_O_ALTO.toFloat() / alto
    }
    val nuevoAncho = (ancho * ratio).toInt()
    val nuevoAlto = (alto * ratio).toInt()
    return Bitmap.createScaledBitmap(original, nuevoAncho, nuevoAlto, true)
}

/**
 * Baja una imagen del servidor (por ejemplo un comprobante ya subido)
 * y la decodifica como Bitmap para mostrarla en pantalla. Devuelve
 * null si no se pudo descargar o no es una imagen valida.
 */
suspend fun descargarBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
    var conexion: HttpURLConnection? = null
    try {
        conexion = URL(url).openConnection() as HttpURLConnection
        conexion.connectTimeout = 15000
        conexion.readTimeout = 15000
        conexion.inputStream.use { input ->
            BitmapFactory.decodeStream(input)
        }
    } catch (e: Exception) {
        null
    } finally {
        conexion?.disconnect()
    }
}