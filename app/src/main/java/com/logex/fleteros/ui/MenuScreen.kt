package com.logex.fleteros.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class OpcionMenu(
    val titulo: String,
    val accion: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    nombreUsuario: String,
    numeroGuia: Int,
    planillasViewModel: PlanillasViewModel,
    onPlanillasDeCarga: () -> Unit,
    onTicketsDeGastos: () -> Unit,
    onTicketsDeDepositos: () -> Unit,
    onSalir: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val estadoPlanillas by planillasViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        planillasViewModel.cargarPlanillas()
    }

    val todosLosClientes = estadoPlanillas.planillas.flatMap { it.clientes }
    val totalClientes = todosLosClientes.size
    val pendientes = todosLosClientes.count { !it.tiene_registro && !it.en_liquidacion }

    val fechaBonita = try {
        val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val salida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        salida.format(entrada.parse(estadoPlanillas.fecha) ?: Date())
    } catch (e: Exception) {
        estadoPlanillas.fecha
    }

    val opciones = listOf(
        OpcionMenu("Planillas de Carga", onPlanillasDeCarga),
        OpcionMenu("Tickets de Gastos", onTicketsDeGastos),
        OpcionMenu("Tickets de Depositos", onTicketsDeDepositos),
        OpcionMenu("Salir", onSalir)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "$nombreUsuario - $numeroGuia",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                opciones.forEach { opcion ->
                    NavigationDrawerItem(
                        label = { Text(opcion.titulo) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            opcion.accion()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Logex Movil - Transporte") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menu")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "$nombreUsuario - $numeroGuia",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hoy, $fechaBonita",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- Resumen del dia ---
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (estadoPlanillas.cargando) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cargando resumen del dia...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else if (estadoPlanillas.error != null) {
                            Text(
                                text = "No se pudo cargar el resumen",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (estadoPlanillas.planillas.isEmpty()) {
                            Text(
                                text = "No tenes planillas asignadas hoy",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = "${estadoPlanillas.planillas.size} planilla(s) asignada(s) hoy",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (pendientes == 0)
                                    "Ya informaste los $totalClientes clientes"
                                else
                                    "$pendientes de $totalClientes clientes pendientes de informar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (pendientes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Accesos directos", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                AccesoDirecto(
                    icono = Icons.Filled.Assignment,
                    titulo = "Planillas de Carga",
                    subtitulo = "Rechazo/entrega, faltantes, cobranza",
                    onClick = onPlanillasDeCarga
                )
                Spacer(modifier = Modifier.height(8.dp))
                AccesoDirecto(
                    icono = Icons.Filled.ReceiptLong,
                    titulo = "Tickets de Gastos",
                    subtitulo = "Cargar un gasto de ruta",
                    onClick = onTicketsDeGastos
                )
                Spacer(modifier = Modifier.height(8.dp))
                AccesoDirecto(
                    icono = Icons.Filled.AccountBalance,
                    titulo = "Tickets de Depositos",
                    subtitulo = "Cargar un deposito bancario",
                    onClick = onTicketsDeDepositos
                )
            }
        }
    }
}

@Composable
private fun AccesoDirecto(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}