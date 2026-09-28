package com.logex.fleteros.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.logex.fleteros.util.crearArchivoTemporalImagen

/**
 * Botones para adjuntar fotos, ya sea sacandolas con la camara o
 * eligiendolas de la galeria. Las fotos elegidas se van acumulando
 * sobre la lista que ya tenia (no la reemplaza).
 */
@Composable
fun SelectorDeFotos(
    imagenesSeleccionadas: List<Uri>,
    onImagenesCambiadas: (List<Uri>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var uriFotoPendiente by remember { mutableStateOf<Uri?>(null) }

    val selectorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onImagenesCambiadas(imagenesSeleccionadas + uris)
        }
    }

    val selectorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito: Boolean ->
        val uri = uriFotoPendiente
        if (exito && uri != null) {
            onImagenesCambiadas(imagenesSeleccionadas + uri)
        }
        uriFotoPendiente = null
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) {
            val nuevoUri = crearArchivoTemporalImagen(context)
            uriFotoPendiente = nuevoUri
            selectorCamara.launch(nuevoUri)
        }
    }

    fun abrirCamara() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            val nuevoUri = crearArchivoTemporalImagen(context)
            uriFotoPendiente = nuevoUri
            selectorCamara.launch(nuevoUri)
        } else {
            pedirPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { abrirCamara() },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Camara")
        }
        OutlinedButton(
            onClick = { selectorGaleria.launch("image/*") },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                if (imagenesSeleccionadas.isEmpty())
                    "Galeria"
                else
                    "${imagenesSeleccionadas.size} foto(s)"
            )
        }
    }
}