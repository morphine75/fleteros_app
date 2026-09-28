package com.logex.fleteros.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Pide permiso de ubicacion en primer plano, y si el celular es Android 10+
 * pide por separado el de segundo plano (asi lo exige el sistema operativo,
 * no se puede pedir todo junto). Llama a onListo() cuando terminó el flujo,
 * haya quedado o no concedido el de segundo plano.
 */
@Composable
fun SolicitarPermisosUbicacion(onListo: () -> Unit) {
    val context = LocalContext.current
    var mostrarDialogoFondo by remember { mutableStateOf(false) }
    var yaProcesado by remember { mutableStateOf(false) }

    val lanzadorForeground = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        val concedido = resultados[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                resultados[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (concedido && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mostrarDialogoFondo = true
        } else {
            onListo()
        }
    }

    val lanzadorBackground = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onListo()
    }

    LaunchedEffect(Unit) {
        if (yaProcesado) return@LaunchedEffect
        yaProcesado = true

        val tieneForeground = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!tieneForeground) {
            lanzadorForeground.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
            return@LaunchedEffect
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val tieneBackground = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (tieneBackground) onListo() else mostrarDialogoFondo = true
        } else {
            onListo()
        }
    }

    if (mostrarDialogoFondo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoFondo = false
                onListo()
            },
            title = { Text("Ubicacion en segundo plano") },
            text = {
                Text("Para registrar tu recorrido incluso con la app cerrada, en la pantalla siguiente elegi \"Permitir todo el tiempo\".")
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoFondo = false
                    lanzadorBackground.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoFondo = false
                    onListo()
                }) {
                    Text("Ahora no")
                }
            }
        )
    }
}