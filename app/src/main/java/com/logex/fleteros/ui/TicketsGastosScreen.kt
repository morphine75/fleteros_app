package com.logex.fleteros.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.logex.fleteros.data.TicketGasto
import com.logex.fleteros.network.ApiClient
import com.logex.fleteros.util.descargarBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsGastosScreen(
    viewModel: TicketsGastosViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var ticketSeleccionado by remember { mutableStateOf<TicketGasto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarTickets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tickets de Gastos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.abrirDialogoNuevo() }) {
                Icon(Icons.Filled.Add, contentDescription = "Cargar ticket")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = estado.cargando,
            onRefresh = { viewModel.cargarTickets() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                estado.tickets.isEmpty() && !estado.cargando -> {
                    Text(
                        text = "No hay tickets cargados todavia",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(estado.tickets) { ticket ->
                            ListItem(
                                headlineContent = { Text("${ticket.fecha} - $ ${ticket.importe}") },
                                supportingContent = { Text(ticket.observaciones) },
                                trailingContent = {
                                    Row {
                                        if (ticket.imagenes.isNotEmpty()) {
                                            IconButton(onClick = { ticketSeleccionado = ticket }) {
                                                Icon(Icons.Filled.Image, contentDescription = "Ver comprobante")
                                            }
                                        }
                                        IconButton(onClick = { viewModel.eliminarTicket(ticket.id_ticket) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                        }
                                    }
                                }
                            )
                            HorizontalDivider()
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

    if (estado.mostrarDialogoNuevo) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarDialogoNuevo() },
            title = { Text("Cargar Ticket") },
            text = {
                Column {
                    var mostrarDatePicker by remember { mutableStateOf(false) }

                    Box {
                        OutlinedTextField(
                            value = estado.fecha,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fecha") },
                            trailingIcon = {
                                Icon(Icons.Filled.DateRange, contentDescription = "Elegir fecha")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Capa transparente encima: como el campo es readOnly,
                        // esto es lo que realmente detecta el toque y abre el calendario.
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { mostrarDatePicker = true }
                        )
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

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = estado.importe,
                        onValueChange = { nuevoValor ->
                            // Solo digitos y un punto decimal como maximo
                            val filtrado = nuevoValor.filter { it.isDigit() || it == '.' }
                            val partes = filtrado.split(".")
                            val valorFinal = if (partes.size > 2) {
                                partes[0] + "." + partes.drop(1).joinToString("")
                            } else {
                                filtrado
                            }
                            viewModel.onImporteChange(valorFinal)
                        },
                        label = { Text("Importe") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = estado.observaciones,
                        onValueChange = viewModel::onObservacionesChange,
                        label = { Text("Observaciones") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectorDeFotos(
                        imagenesSeleccionadas = estado.imagenesSeleccionadas,
                        onImagenesCambiadas = viewModel::onImagenesSeleccionadas
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.guardarTicket(context) },
                    enabled = !estado.guardando
                ) {
                    Text(if (estado.guardando) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cerrarDialogoNuevo() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val ticketAbierto = ticketSeleccionado
    if (ticketAbierto != null) {
        AlertDialog(
            onDismissRequest = { ticketSeleccionado = null },
            title = { Text("Comprobante") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ticketAbierto.imagenes.forEach { rutaRelativa ->
                        ImagenTicket(
                            url = ApiClient.urlImagen(rutaRelativa),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { ticketSeleccionado = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun ImagenTicket(url: String, modifier: Modifier = Modifier) {
    var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
    var cargando by remember(url) { mutableStateOf(true) }

    LaunchedEffect(url) {
        bitmap = descargarBitmap(url)
        cargando = false
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val bitmapActual = bitmap
        when {
            bitmapActual != null -> {
                Image(
                    bitmap = bitmapActual.asImageBitmap(),
                    contentDescription = "Comprobante",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            cargando -> CircularProgressIndicator()
            else -> Text("No se pudo cargar la imagen")
        }
    }
}