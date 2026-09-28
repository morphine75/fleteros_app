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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.logex.fleteros.data.FacturaPlanilla

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguimientoScreen(
    viewModel: SeguimientoViewModel,
    nombreCliente: String,
    onVolver: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.cargarDetalle()
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EstadoRadio("ENTREGADO", estado.estado == EstadoSeguimiento.ENTREGADO) {
                            viewModel.seleccionarEstado(EstadoSeguimiento.ENTREGADO)
                        }
                        EstadoRadio("RCH. TOTAL", estado.estado == EstadoSeguimiento.RECHAZADO) {
                            viewModel.seleccionarEstado(EstadoSeguimiento.RECHAZADO)
                        }
                        EstadoRadio("RCH. PARCIAL", estado.estado == EstadoSeguimiento.RECHAZADO_PARCIAL) {
                            viewModel.seleccionarEstado(EstadoSeguimiento.RECHAZADO_PARCIAL)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    when (estado.estado) {
                        EstadoSeguimiento.ENTREGADO -> {
                            SeccionMediosDePago(viewModel = viewModel, estado = estado)
                        }
                        EstadoSeguimiento.RECHAZADO -> {
                            SeccionMotivoRechazo(viewModel = viewModel, estado = estado)
                        }
                        EstadoSeguimiento.RECHAZADO_PARCIAL -> {
                            SeccionFacturasParcial(viewModel = viewModel, estado = estado)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            SeccionMediosDePago(viewModel = viewModel, estado = estado)
                        }
                        EstadoSeguimiento.NINGUNO -> {
                            Text(
                                text = "Elegi arriba: Entregado, Rechazo total, o Rechazo parcial",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (estado.estado != EstadoSeguimiento.NINGUNO) {
                        if (estado.imagenesExistentes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Fotos ya subidas", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            estado.imagenesExistentes.forEach { imagen ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    ImagenTicket(
                                        url = com.logex.fleteros.network.ApiClient.urlImagenPlanilla(imagen.nombre_archivo),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(140.dp)
                                    )
                                    IconButton(onClick = { viewModel.eliminarImagenExistente(imagen.id_detalle_img) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar foto")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Agregar comprobante (opcional)", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectorDeFotos(
                            imagenesSeleccionadas = estado.imagenesSeleccionadas,
                            onImagenesCambiadas = viewModel::onImagenesCambiadas
                        )
                    }

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
                        Text(if (estado.guardando) "Guardando..." else "Guardar")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun EstadoRadio(etiqueta: String, seleccionado: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = seleccionado, onClick = onClick)
        Text(etiqueta, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeccionMotivoRechazo(viewModel: SeguimientoViewModel, estado: SeguimientoUiState) {
    Text("Motivo del rechazo", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(8.dp))
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it }
    ) {
        OutlinedTextField(
            value = estado.motivoSeleccionado?.descripcion ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Seleccione un motivo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            estado.motivos.forEach { motivo ->
                DropdownMenuItem(
                    text = { Text(motivo.descripcion) },
                    onClick = {
                        viewModel.seleccionarMotivo(motivo)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SeccionFacturasParcial(viewModel: SeguimientoViewModel, estado: SeguimientoUiState) {
    Text("Facturas del cliente", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Indica cuanto cobraste de cada una. Lo que quede por debajo del total se considera rechazado.",
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(modifier = Modifier.height(8.dp))

    estado.facturas.forEach { factura: FacturaPlanilla ->
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Text("Factura ${factura.nrodoc} - Total $ ${"%.2f".format(factura.total)}")
            OutlinedTextField(
                value = estado.importesCobrados[factura.nrodoc] ?: "",
                onValueChange = { viewModel.onImporteFacturaChange(factura.nrodoc, it) },
                label = { Text("Importe cobrado") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SeccionMediosDePago(viewModel: SeguimientoViewModel, estado: SeguimientoUiState) {
    Text("Medio de pago", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(8.dp))

    MedioDePago(
        etiqueta = "Efectivo",
        activo = estado.efectivoActivo,
        importe = estado.efectivoImporte,
        onToggle = viewModel::toggleEfectivo,
        onImporteChange = viewModel::onEfectivoImporteChange
    )
    MedioDePago(
        etiqueta = "Cheque",
        activo = estado.chequeActivo,
        importe = estado.chequeImporte,
        onToggle = viewModel::toggleCheque,
        onImporteChange = viewModel::onChequeImporteChange
    )
    MedioDePago(
        etiqueta = "Retenciones",
        activo = estado.retencionActivo,
        importe = estado.retencionImporte,
        onToggle = viewModel::toggleRetencion,
        onImporteChange = viewModel::onRetencionImporteChange
    )
    MedioDePago(
        etiqueta = "Transferencia",
        activo = estado.transferenciaActivo,
        importe = estado.transferenciaImporte,
        onToggle = viewModel::toggleTransferencia,
        onImporteChange = viewModel::onTransferenciaImporteChange
    )
    if (estado.transferenciaActivo) {
        Column(modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)) {
            OutlinedTextField(
                value = estado.bancoTransferencia,
                onValueChange = viewModel::onBancoChange,
                label = { Text("Banco") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = estado.fechaTransferencia,
                onValueChange = viewModel::onFechaTransferenciaChange,
                label = { Text("Fecha (AAAA-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    MedioDePago(
        etiqueta = "Cuenta Corriente",
        activo = estado.ctaCteActivo,
        importe = estado.ctaCteImporte,
        onToggle = viewModel::toggleCtaCte,
        onImporteChange = viewModel::onCtaCteImporteChange
    )
}

@Composable
private fun MedioDePago(
    etiqueta: String,
    activo: Boolean,
    importe: String,
    onToggle: (Boolean) -> Unit,
    onImporteChange: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = activo, onCheckedChange = onToggle)
        Text(etiqueta, modifier = Modifier.width(120.dp))
        if (activo) {
            OutlinedTextField(
                value = importe,
                onValueChange = onImporteChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}