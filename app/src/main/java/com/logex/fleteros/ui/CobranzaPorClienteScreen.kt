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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.logex.fleteros.data.ClienteCobranza

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CobranzaPorClienteScreen(
    viewModel: CobranzaPorClienteViewModel,
    numplanilla: Int,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cobranza - Planilla N\u00b0 $numplanilla") },
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
                estado.error != null -> {
                    Text(
                        text = estado.error ?: "",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            KpisResumen(estado)
                            HorizontalDivider()
                        }
                        items(estado.clientes) { cliente ->
                            ClienteCobranzaRow(cliente)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpisResumen(estado: CobranzaPorClienteUiState) {
    Column(modifier = Modifier.padding(16.dp)) {
        KpiCard("Clientes", "${estado.clientes.size}", Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard("Facturado", "$ ${"%.2f".format(estado.totalFacturado)}", Modifier.weight(1f))
            KpiCard("Cobrado", "$ ${"%.2f".format(estado.totalCobrado)}", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard("Diferencia", "$ ${"%.2f".format(estado.diferencia)}", Modifier.weight(1f))
            KpiCard("% Cobrado", "${"%.1f".format(estado.porcentajeCobrado)}%", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard("Entregados", "${estado.cantEntregados}", Modifier.weight(1f))
            KpiCard("Rechazados", "${estado.cantRechazados}", Modifier.weight(1f))
            KpiCard("Parcial", "${estado.cantParciales}", Modifier.weight(1f))
            KpiCard("S/Inf.", "${estado.cantSinInformar}", Modifier.weight(1f))
        }
    }
}

@Composable
private fun KpiCard(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(etiqueta, style = MaterialTheme.typography.labelSmall)
            Text(valor, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun ClienteCobranzaRow(cliente: ClienteCobranza) {
    val diferencia = cliente.total - (cliente.total_pagado + cliente.rechazo)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${cliente.cliente} - ${cliente.nombre}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            EstadoBadge(cliente.estado)
        }
        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (cliente.efectivo != 0.0) AssistChip(onClick = {}, label = { Text("Efectivo") })
            if (cliente.transferencia != 0.0) AssistChip(onClick = {}, label = { Text("Transf.") })
            if (cliente.cheque != 0.0) AssistChip(onClick = {}, label = { Text("Cheque") })
            if (cliente.cta_cte != 0.0) AssistChip(onClick = {}, label = { Text("Cta.Cte.") })
        }

        Spacer(modifier = Modifier.height(4.dp))
        Column {
            Text("Fact. $ ${"%.2f".format(cliente.total)}", style = MaterialTheme.typography.bodySmall)
            Text("Cob. $ ${"%.2f".format(cliente.total_pagado)}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Rech. $ ${"%.2f".format(cliente.rechazo)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE8A020)
            )
            Text(
                "Dif. $ ${"%.2f".format(diferencia)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (diferencia > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
private fun EstadoBadge(estado: String) {
    val (texto, color) = when (estado) {
        "ENTREGADO" -> "Entregado" to Color(0xFF2E7D32)
        "RECHAZADO" -> "Rechazado" to MaterialTheme.colorScheme.error
        "RECHAZADO_PARCIAL" -> "Rech. Parcial" to Color(0xFFE8A020)
        else -> "Sin Informar" to Color(0xFF6C757D)
    }
    AssistChip(onClick = {}, label = { Text(texto, color = color) })
}