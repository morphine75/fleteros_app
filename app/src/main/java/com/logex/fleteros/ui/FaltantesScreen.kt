package com.logex.fleteros.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.logex.fleteros.data.ArticuloFactura
import com.logex.fleteros.data.FacturaConArticulos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaltantesScreen(
    viewModel: FaltantesViewModel,
    nombreCliente: String,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarDetalle()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Faltantes - $nombreCliente") },
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
            when {
                estado.cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                estado.facturas.isEmpty() -> {
                    Text(
                        text = "No hay facturas para este cliente",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        estado.facturas.forEach { factura ->
                            Text(
                                text = "F. ${factura.letra}-${factura.nrodoc}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            factura.articulos.forEach { articulo ->
                                ArticuloRow(
                                    factura = factura,
                                    articulo = articulo,
                                    marcado = estado.marcados.contains(articulo.codart),
                                    bultos = estado.bultosIngresados[articulo.codart] ?: "",
                                    unidades = estado.unidadesIngresadas[articulo.codart] ?: "",
                                    guardando = estado.guardandoCodart == articulo.codart,
                                    yaGuardado = articulo.faltante != null,
                                    onToggle = { viewModel.toggleMarcado(articulo.codart) },
                                    onBultosChange = { viewModel.onBultosChange(articulo.codart, it) },
                                    onUnidadesChange = { viewModel.onUnidadesChange(articulo.codart, it) },
                                    onGuardar = { viewModel.guardarFaltante(factura, articulo) }
                                )
                                HorizontalDivider()
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        val mensajeError = estado.error
                        if (mensajeError != null) {
                            Text(mensajeError, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        val mensajeOk = estado.mensaje
                        if (mensajeOk != null) {
                            Text(mensajeOk, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticuloRow(
    factura: FacturaConArticulos,
    articulo: ArticuloFactura,
    marcado: Boolean,
    bultos: String,
    unidades: String,
    guardando: Boolean,
    yaGuardado: Boolean,
    onToggle: () -> Unit,
    onBultosChange: (String) -> Unit,
    onUnidadesChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (yaGuardado) {
                Text(
                    text = "Marcado como faltante",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(articulo.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Bts. ${quitarDecimalesSiEntero(articulo.cant)} - Uds. ${quitarDecimalesSiEntero(articulo.resto)}",
                style = MaterialTheme.typography.bodySmall
            )

            if (marcado) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bultos,
                        onValueChange = onBultosChange,
                        label = { Text("Bultos") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unidades,
                        onValueChange = onUnidadesChange,
                        label = { Text("Unidades") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onGuardar, enabled = !guardando) {
                    Text(if (guardando) "Guardando..." else "Guardar")
                }
            }
        }
        Checkbox(checked = marcado, onCheckedChange = { onToggle() })
    }
}

private fun quitarDecimalesSiEntero(valor: Double): String {
    return if (valor == valor.toInt().toDouble()) valor.toInt().toString() else valor.toString()
}