package com.logex.fleteros.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.logex.fleteros.network.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarImagenScreen(
    viewModel: AgregarImagenViewModel,
    nombreCliente: String,
    liquidacion: Int,
    onVolver: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.cargarImagenes()
    }

    LaunchedEffect(estado.guardadoExitoso) {
        if (estado.guardadoExitoso) {
            onGuardadoExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreCliente) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Cliente incluido en la liquidacion $liquidacion",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Este pedido ya fue liquidado: solo se pueden agregar fotos, no se puede modificar el pago ni el estado.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (estado.imagenesExistentes.isNotEmpty()) {
                        Text("Fotos ya subidas", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        estado.imagenesExistentes.forEach { imagen ->
                            ImagenTicket(
                                url = ApiClient.urlImagenPlanilla(imagen.nombre_archivo),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .padding(vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text("Agregar fotos nuevas", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectorDeFotos(
                        imagenesSeleccionadas = estado.imagenesSeleccionadas,
                        onImagenesCambiadas = viewModel::onImagenesCambiadas
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val mensajeError = estado.error
                    if (mensajeError != null) {
                        Snackbar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(mensajeError)
                        }
                    }

                    Button(
                        onClick = { viewModel.guardar(context) },
                        enabled = !estado.guardando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (estado.guardando) "Guardando..." else "Guardar fotos")
                    }
                }
            }
        }
    }
}