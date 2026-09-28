package com.logex.fleteros

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.logex.fleteros.data.AuthRepository
import com.logex.fleteros.data.ClientePlanilla
import com.logex.fleteros.data.FaltantesRepository
import com.logex.fleteros.data.PlanillaCarga
import com.logex.fleteros.data.PlanillasRepository
import com.logex.fleteros.data.SeguimientoRepository
import com.logex.fleteros.data.TicketsDepositoRepository
import com.logex.fleteros.data.TicketsGastosRepository
import com.logex.fleteros.data.TokenManager
import com.logex.fleteros.data.UbicacionRepository
import com.logex.fleteros.location.SolicitarPermisosUbicacion
import com.logex.fleteros.location.UbicacionService
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.logex.fleteros.ui.AgregarImagenScreen
import com.logex.fleteros.ui.AgregarImagenViewModel
import com.logex.fleteros.ui.CobranzaPorClienteScreen
import com.logex.fleteros.ui.CobranzaPorClienteViewModel
import com.logex.fleteros.ui.FaltantesScreen
import com.logex.fleteros.ui.FaltantesViewModel
import com.logex.fleteros.ui.LoginScreen
import com.logex.fleteros.ui.LoginViewModel
import com.logex.fleteros.ui.MenuScreen
import com.logex.fleteros.ui.PlanillasScreen
import com.logex.fleteros.ui.PlanillasViewModel
import com.logex.fleteros.ui.SeguimientoScreen
import com.logex.fleteros.ui.SeguimientoViewModel
import com.logex.fleteros.ui.TicketsDepositoScreen
import com.logex.fleteros.ui.TicketsDepositoViewModel
import com.logex.fleteros.ui.TicketsGastosScreen
import com.logex.fleteros.ui.TicketsGastosViewModel
import com.logex.fleteros.ui.theme.FleterosTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FleterosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}

private enum class Pantalla { LOGIN, MENU, TICKETS_GASTOS, TICKETS_DEPOSITO, PLANILLAS, SEGUIMIENTO, AGREGAR_IMAGEN, FALTANTES, COBRANZA_CLIENTE }

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val activity = context as? Activity
    val tokenManager = remember { TokenManager(context) }
    val repository = remember { AuthRepository(tokenManager) }
    val viewModel = remember { LoginViewModel(repository) }
    val ticketsGastosViewModel = remember { TicketsGastosViewModel(TicketsGastosRepository(tokenManager)) }
    val ticketsDepositoViewModel = remember { TicketsDepositoViewModel(TicketsDepositoRepository(tokenManager)) }
    val planillasViewModel = remember { PlanillasViewModel(PlanillasRepository(tokenManager)) }

    // Si ya habia una sesion guardada (login previo), arranca directo en el menu
    var pantalla by remember {
        mutableStateOf(if (tokenManager.haySesion()) Pantalla.MENU else Pantalla.LOGIN)
    }

    // En cuanto hay sesion activa (login nuevo o ya logueado al abrir la app),
    // chequea si el repartidor tiene ruta asignada hoy -- si no tiene, ni
    // siquiera pide el permiso de ubicacion.
    val ubicacionRepository = remember { UbicacionRepository(context, tokenManager) }
    val scopeUbicacion = rememberCoroutineScope()
    var pedirPermisosUbicacion by remember { mutableStateOf(false) }

    fun evaluarInicioTracking() {
        scopeUbicacion.launch {
            val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val tieneRuta = ubicacionRepository.tieneRutaHoy(fechaHoy)
            android.widget.Toast.makeText(
                context,
                "Chequeo ruta hoy ($fechaHoy): $tieneRuta",
                android.widget.Toast.LENGTH_LONG
            ).show()
            if (tieneRuta) {
                pedirPermisosUbicacion = true
            }
        }
    }

    LaunchedEffect(Unit) {
        if (tokenManager.haySesion()) {
            evaluarInicioTracking()
        }
    }
    if (pedirPermisosUbicacion) {
        SolicitarPermisosUbicacion {
            UbicacionService.iniciar(context)
            pedirPermisosUbicacion = false
        }
    }

    var clienteSeleccionado by remember { mutableStateOf<Pair<PlanillaCarga, ClientePlanilla>?>(null) }
    var planillaCobranza by remember { mutableStateOf<PlanillaCarga?>(null) }

    when (pantalla) {
        Pantalla.LOGIN -> {
            LoginScreen(
                viewModel = viewModel,
                onLoginExitoso = {
                    evaluarInicioTracking()
                    pantalla = Pantalla.MENU
                }
            )
        }
        Pantalla.MENU -> {
            MenuScreen(
                nombreUsuario = tokenManager.nombreUsuario() ?: "",
                numeroGuia = tokenManager.numeroGuia(),
                planillasViewModel = planillasViewModel,
                onPlanillasDeCarga = { pantalla = Pantalla.PLANILLAS },
                onTicketsDeGastos = { pantalla = Pantalla.TICKETS_GASTOS },
                onTicketsDeDepositos = { pantalla = Pantalla.TICKETS_DEPOSITO },
                onSalir = {
                    UbicacionService.detener(context)
                    activity?.finishAndRemoveTask()
                }
            )
        }
        Pantalla.TICKETS_GASTOS -> {
            TicketsGastosScreen(
                viewModel = ticketsGastosViewModel,
                onVolver = { pantalla = Pantalla.MENU }
            )
        }
        Pantalla.TICKETS_DEPOSITO -> {
            TicketsDepositoScreen(
                viewModel = ticketsDepositoViewModel,
                onVolver = { pantalla = Pantalla.MENU }
            )
        }
        Pantalla.PLANILLAS -> {
            PlanillasScreen(
                viewModel = planillasViewModel,
                onVolver = { pantalla = Pantalla.MENU },
                onAbrirCliente = { planilla, cliente ->
                    clienteSeleccionado = planilla to cliente
                    pantalla = if (cliente.en_liquidacion) Pantalla.AGREGAR_IMAGEN else Pantalla.SEGUIMIENTO
                },
                onMarcarFaltantes = { planilla, cliente ->
                    clienteSeleccionado = planilla to cliente
                    pantalla = Pantalla.FALTANTES
                },
                onVerCobranzaPorCliente = { planilla ->
                    planillaCobranza = planilla
                    pantalla = Pantalla.COBRANZA_CLIENTE
                }
            )
        }
        Pantalla.SEGUIMIENTO -> {
            val seleccion = clienteSeleccionado
            if (seleccion != null) {
                val (planilla, cliente) = seleccion
                val seguimientoViewModel = remember(planilla.planillac, cliente.cliente) {
                    SeguimientoViewModel(
                        repository = SeguimientoRepository(tokenManager),
                        numplanilla = planilla.nplanilla,
                        idPlanillac = planilla.planillac,
                        idCliente = cliente.cliente,
                        nombreCliente = cliente.nombre,
                        totalCobrar = cliente.total
                    )
                }
                SeguimientoScreen(
                    viewModel = seguimientoViewModel,
                    nombreCliente = cliente.nombre,
                    onVolver = { pantalla = Pantalla.PLANILLAS },
                    onGuardadoExitoso = {
                        planillasViewModel.cargarPlanillas()
                        pantalla = Pantalla.PLANILLAS
                    }
                )
            }
        }
        Pantalla.AGREGAR_IMAGEN -> {
            val seleccion = clienteSeleccionado
            if (seleccion != null) {
                val (planilla, cliente) = seleccion
                val agregarImagenViewModel = remember(planilla.planillac, cliente.cliente) {
                    AgregarImagenViewModel(
                        repository = SeguimientoRepository(tokenManager),
                        numplanilla = planilla.nplanilla,
                        idPlanillac = planilla.planillac,
                        idCliente = cliente.cliente,
                        nombreCliente = cliente.nombre
                    )
                }
                AgregarImagenScreen(
                    viewModel = agregarImagenViewModel,
                    nombreCliente = cliente.nombre,
                    liquidacion = cliente.liquidacion,
                    onVolver = { pantalla = Pantalla.PLANILLAS },
                    onGuardadoExitoso = {
                        planillasViewModel.cargarPlanillas()
                        pantalla = Pantalla.PLANILLAS
                    }
                )
            }
        }
        Pantalla.FALTANTES -> {
            val seleccion = clienteSeleccionado
            if (seleccion != null) {
                val (planilla, cliente) = seleccion
                val faltantesViewModel = remember(planilla.planillac, cliente.cliente) {
                    FaltantesViewModel(
                        repository = FaltantesRepository(tokenManager),
                        numplanilla = planilla.nplanilla,
                        idPlanillac = planilla.planillac,
                        idCliente = cliente.cliente,
                        nombreCliente = cliente.nombre,
                        fletero = planilla.fletero
                    )
                }
                FaltantesScreen(
                    viewModel = faltantesViewModel,
                    nombreCliente = cliente.nombre,
                    onVolver = { pantalla = Pantalla.PLANILLAS }
                )
            }
        }
        Pantalla.COBRANZA_CLIENTE -> {
            val planilla = planillaCobranza
            if (planilla != null) {
                val cobranzaViewModel = remember(planilla.planillac) {
                    CobranzaPorClienteViewModel(
                        repository = PlanillasRepository(tokenManager),
                        numplanilla = planilla.nplanilla
                    )
                }
                CobranzaPorClienteScreen(
                    viewModel = cobranzaViewModel,
                    numplanilla = planilla.nplanilla,
                    onVolver = { pantalla = Pantalla.PLANILLAS }
                )
            }
        }
    }
}