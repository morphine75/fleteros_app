package com.logex.fleteros.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.logex.fleteros.data.ClientePlanilla
import com.logex.fleteros.data.DetalleImporte
import com.logex.fleteros.data.PlanillaCarga
import com.logex.fleteros.data.ResumenCobranza
import com.logex.fleteros.data.TicketsResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanillasScreen(
    viewModel: PlanillasViewModel,
    onVolver: () -> Unit,
    onAbrirCliente: (PlanillaCarga, ClientePlanilla) -> Unit,
    onMarcarFaltantes: (PlanillaCarga, ClientePlanilla) -> Unit,
    onVerCobranzaPorCliente: (PlanillaCarga) -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    var planillaResumen by remember { mutableStateOf<PlanillaCarga?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarPlanillas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planillas de Carga") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            var mostrarDatePicker by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = estado.fecha,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha") },
                        trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { mostrarDatePicker = true }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.cargarPlanillas() }) {
                    Text("Ver")
                }
            }

            if (mostrarDatePicker) {
                val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val fechaInicialMillis = try {
                    formatoFecha.parse(estado.fecha)?.time
                } catch (e: Exception) {
                    null
                }
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = fechaInicialMillis ?: System.currentTimeMillis()
                )
                DatePickerDialog(
                    onDismissRequest = { mostrarDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                viewModel.onFechaChange(formatoFecha.format(Date(millis)))
                            }
                            mostrarDatePicker = false
                        }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = estado.filtro,
                onValueChange = viewModel::onFiltroChange,
                label = { Text("Buscar por nombre o numero de cliente") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = estado.filtroEstado == FiltroEstado.TODOS,
                    onClick = { viewModel.onFiltroEstadoChange(FiltroEstado.TODOS) },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = estado.filtroEstado == FiltroEstado.NO_INFORMADOS,
                    onClick = { viewModel.onFiltroEstadoChange(FiltroEstado.NO_INFORMADOS) },
                    label = { Text("No informado") }
                )
                FilterChip(
                    selected = estado.filtroEstado == FiltroEstado.INFORMADOS,
                    onClick = { viewModel.onFiltroEstadoChange(FiltroEstado.INFORMADOS) },
                    label = { Text("Informado") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Filtra clientes por nombre/numero y por estado informado/no
            // informado, y descarta planillas que se quedan sin ningun
            // cliente que coincida con los filtros activos.
            val filtro = estado.filtro.trim()
            val planillasFiltradas = estado.planillas.mapNotNull { planilla ->
                val clientesFiltrados = planilla.clientes.filter { cliente ->
                    val coincideTexto = filtro.isEmpty() ||
                            cliente.nombre.contains(filtro, ignoreCase = true) ||
                            cliente.cliente.toString().contains(filtro)

                    val informado = cliente.tiene_registro || cliente.en_liquidacion
                    val coincideEstado = when (estado.filtroEstado) {
                        FiltroEstado.TODOS -> true
                        FiltroEstado.INFORMADOS -> informado
                        FiltroEstado.NO_INFORMADOS -> !informado
                    }

                    coincideTexto && coincideEstado
                }
                if (clientesFiltrados.isEmpty()) null else planilla.copy(clientes = clientesFiltrados)
            }

            PullToRefreshBox(
                isRefreshing = estado.cargando,
                onRefresh = { viewModel.cargarPlanillas() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    estado.planillas.isEmpty() && !estado.cargando -> {
                        Text(
                            text = "No tiene planillas asignadas para esta fecha",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                        )
                    }
                    planillasFiltradas.isEmpty() -> {
                        Text(
                            text = "Ningun cliente coincide con la busqueda",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            planillasFiltradas.forEach { planilla ->
                                item {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Planilla N\u00b0 ${planilla.nplanilla}",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = "${planilla.fletero} \u2014 Total $ ${"%.2f".format(planilla.total)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { planillaResumen = planilla },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF3B7DD8) // azul
                                                )
                                            ) {
                                                Text("Ver Resumen")
                                            }
                                            Button(
                                                onClick = { onVerCobranzaPorCliente(planilla) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF2E7D32) // verde
                                                )
                                            ) {
                                                Text("Cobranza por Cliente")
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                                items(planilla.clientes) { cliente ->
                                    ClienteRow(
                                        cliente = cliente,
                                        onAbrirCliente = { onAbrirCliente(planilla, cliente) },
                                        onMarcarFaltantes = { onMarcarFaltantes(planilla, cliente) }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }

                val mensajeError = estado.error
                if (mensajeError != null) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text(mensajeError)
                    }
                }
            }
        }
    }

    val planillaAbierta = planillaResumen
    if (planillaAbierta != null) {
        ResumenCobranzaDialog(
            planilla = planillaAbierta,
            obtenerResumen = { idPlanillac, total -> viewModel.obtenerResumen(idPlanillac, total) },
            onDismiss = { planillaResumen = null }
        )
    }
}

@Composable
private fun ResumenCobranzaDialog(
    planilla: PlanillaCarga,
    obtenerResumen: suspend (Int, Double) -> TicketsResult<ResumenCobranza>,
    onDismiss: () -> Unit
) {
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var resumen by remember { mutableStateOf<ResumenCobranza?>(null) }

    LaunchedEffect(planilla.planillac) {
        when (val resultado = obtenerResumen(planilla.planillac, planilla.total)) {
            is TicketsResult.Exito -> {
                resumen = resultado.datos
                cargando = false
            }
            is TicketsResult.Error -> {
                error = resultado.mensaje
                cargando = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resumen - Planilla N\u00b0 ${planilla.nplanilla}") },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                when {
                    cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    error != null -> Text(error ?: "")
                    resumen != null -> {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            val r = resumen!!
                            FilaResumenExpandible("Efectivo", r.totales.efectivo, r.detalle_efectivo)
                            FilaResumenExpandible("Transferencia", r.totales.transferencia, r.detalle_transferencia)
                            FilaResumenSimple("Cheques", r.totales.cheque)
                            FilaResumenSimple("Cta.Cte.", r.totales.cta_cte)
                            FilaResumenSimple("Retencion", r.totales.retencion)
                            FilaResumenExpandible("Rechazo", r.totales.rechazo, r.detalle_rechazo, destacado = true)
                            FilaResumenExpandible("Rechazo Parcial", r.totales.rechazo_parcial, r.detalle_rechazo_parcial, destacado = true)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            FilaResumenSimple("Total Cobrado", r.totales.total_cobrado)
                            FilaResumenSimple("Total Planilla", r.totales.total_planilla)
                            FilaResumenSimple("Total Planilla - Rechazos - Retenciones", r.totales.total_planilla_neto)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
private fun FilaResumenSimple(etiqueta: String, importe: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text("$ ${"%.2f".format(importe)}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FilaResumenExpandible(
    etiqueta: String,
    importe: Double,
    detalle: List<DetalleImporte>,
    destacado: Boolean = false
) {
    var expandido by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = detalle.isNotEmpty()) { expandido = !expandido },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (detalle.isNotEmpty()) "$etiqueta (${detalle.size})" else etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = if (destacado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$ ${"%.2f".format(importe)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (destacado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
        if (expandido) {
            Column(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
                detalle.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.cliente} - ${item.nombre}", style = MaterialTheme.typography.bodySmall)
                        Text("$ ${"%.2f".format(item.importe)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClienteRow(
    cliente: ClientePlanilla,
    onAbrirCliente: () -> Unit,
    onMarcarFaltantes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "${cliente.cliente} - ${cliente.nombre}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Total $ ${"%.2f".format(cliente.total)}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))

        when {
            cliente.en_liquidacion -> {
                Text(
                    text = "Incluida en liquidacion ${cliente.liquidacion}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(onClick = onAbrirCliente) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar Imagen")
                }
            }
            cliente.tiene_registro -> {
                OutlinedButton(onClick = onAbrirCliente) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar Rechazo / Entrega")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (cliente.transferencia > 0) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Transf. $ ${"%.2f".format(cliente.transferencia)}") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF2E7D32),
                                labelColor = Color.White
                            )
                        )
                    }
                    if (cliente.efectivo > 0) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Efvo. $ ${"%.2f".format(cliente.efectivo)}") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF2E7D32),
                                labelColor = Color.White
                            )
                        )
                    }
                    if (cliente.cta_cte > 0) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Cta.Cte $ ${"%.2f".format(cliente.cta_cte)}") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF2E7D32),
                                labelColor = Color.White
                            )
                        )
                    }
                }
                if (cliente.rechazado || cliente.rechazado_parcial) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (cliente.rechazado) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Rechazado $ ${"%.2f".format(cliente.rechazo)}") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    labelColor = MaterialTheme.colorScheme.onError
                                )
                            )
                        }
                        if (cliente.rechazado_parcial) {
                            // IMPORTE_RECHAZO es un campo unico que guarda
                            // el monto del rechazo sea total o parcial;
                            // RECHAZADO/RECHAZADO_PARCIAL solo indican que
                            // tipo fue. Por eso comparte el mismo monto
                            // "rechazo" con el chip de arriba -- es correcto,
                            // no un error.
                            AssistChip(
                                onClick = {},
                                label = { Text("Rechazo Parcial $ ${"%.2f".format(cliente.rechazo)}") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFFE8A020),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAbrirCliente, modifier = Modifier.weight(1f)) {
                        Text("Rechazo / Entrega")
                    }
                    OutlinedButton(onClick = onMarcarFaltantes, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Faltantes")
                    }
                }
            }
        }
    }
}