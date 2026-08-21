package com.example.codise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codise.data.Ciudad
import com.example.codise.data.Evento
import com.example.codise.data.Usuario
import com.example.codise.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Codice路Theme {
                AplicacionPrincipal()
            }
        }
    }
}

@Composable
fun AplicacionPrincipal() {
    val viewModelLogin: ViewModelLogin = viewModel()
    val estadoUi by viewModelLogin.estadoUi.collectAsState()

    if (estadoUi is EstadoUiLogin.Exito) {
        val respuesta = (estadoUi as EstadoUiLogin.Exito).respuesta
        AplicacionAutenticada(
            usuario = respuesta.usuario,
            token = respuesta.tokens.access,
            alCerrarSesion = { viewModelLogin.cerrarSesion() }
        )
    } else {
        PantallaLogin(viewModelLogin)
    }
}

@Composable
fun AplicacionAutenticada(usuario: Usuario, token: String, alCerrarSesion: () -> Unit) {
    val viewModelPerfil: ViewModelPerfil = viewModel()
    val estadoUiPerfil by viewModelPerfil.estadoUi.collectAsState()
    val viewModelPrincipal: ViewModelPrincipal = viewModel()
    val viewModelEventos: ViewModelEventos = viewModel()
    val viewModelPublicaciones: ViewModelPublicaciones = viewModel()
    val idsPuntosVisitados by viewModelPrincipal.idsPuntosVisitados.collectAsState()
    val contexto = LocalContext.current
    val clienteUbicacion = remember { LocationServices.getFusedLocationProviderClient(contexto) }

    var pantallaActual by remember { mutableStateOf("main") }
    var eventoSeleccionado by remember { mutableStateOf<Evento?>(null) }
    val ciudadSeleccionada = viewModelPrincipal.ciudadSeleccionada
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }

    var idPuntoParaMarcarComoVisitado by remember { mutableStateOf<Int?>(null) }

    val lanzadorPermisosUbicacion = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            idPuntoParaMarcarComoVisitado?.let { puntoId ->
                try {
                    clienteUbicacion.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).addOnSuccessListener { ubicacion ->
                        viewModelPrincipal.alternarVisitado(puntoId, ubicacion?.latitude, ubicacion?.longitude)
                    }.addOnFailureListener {
                        viewModelPrincipal.alternarVisitado(puntoId)
                    }
                } catch (e: SecurityException) {
                    viewModelPrincipal.alternarVisitado(puntoId)
                }
            }
        } else {
            // Permiso denegado, marcar como visitado localmente sin GPS
            idPuntoParaMarcarComoVisitado?.let { viewModelPrincipal.alternarVisitado(it) }
        }
        idPuntoParaMarcarComoVisitado = null
    }

    val solicitarUbicacionYMarcar = { puntoId: Int ->
        if (idsPuntosVisitados.contains(puntoId)) {
            // Ya visitado, desmarcar localmente
            viewModelPrincipal.alternarVisitado(puntoId)
        } else {
            if (ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    clienteUbicacion.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).addOnSuccessListener { ubicacion ->
                        viewModelPrincipal.alternarVisitado(puntoId, ubicacion?.latitude, ubicacion?.longitude)
                    }.addOnFailureListener {
                        viewModelPrincipal.alternarVisitado(puntoId)
                    }
                } catch (e: SecurityException) {
                    viewModelPrincipal.alternarVisitado(puntoId)
                }
            } else {
                idPuntoParaMarcarComoVisitado = puntoId
                lanzadorPermisosUbicacion.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    // Auto-refrescar al volver al primer plano
    val propietarioCicloVida = LocalLifecycleOwner.current
    DisposableEffect(propietarioCicloVida) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                viewModelPrincipal.obtenerCiudades()
            }
        }
        propietarioCicloVida.lifecycle.addObserver(observador)
        onDispose {
            propietarioCicloVida.lifecycle.removeObserver(observador)
        }
    }

    var mostrarDialogoAsistente by remember { mutableStateOf(false) }

    if (mostrarDialogoAsistente) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAsistente = false },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.iconasistente),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            },
            title = {
                Text(
                    text = "Asistente Guardabarranco",
                    fontWeight = FontWeight.Bold,
                    color = AzulPetroleo
                )
            },
            text = {
                Text(
                    text = "¡Hola! Soy tu asistente Guardabarranco. Pronto podré ayudarte con recomendaciones turísticas personalizadas, rutas y eventos en Nicaragua.",
                    color = AzulPetroleo.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoAsistente = false }) {
                    Text("Entendido", color = GoldColor, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = BlancoBase,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            val tituloBarraSuperior = when (pantallaActual) {
                "upload_event" -> "Subir Nuevo Evento"
                else -> null
            }
            BarraSuperior(
                titulo = tituloBarraSuperior,
                alHacerClicEnPerfil = { pantallaActual = "profile" },
                alHacerClicEnLogo = { pantallaActual = "main" },
                alHacerClicEnAsistente = { mostrarDialogoAsistente = true }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                pantallaActual = pantallaActual,
                pestanaSeleccionada = pestanaSeleccionada,
                alHacerClicEnInicio = { pantallaActual = "main" },
                alSeleccionarPestana = { 
                    pestanaSeleccionada = it
                    if (it == 2 || it == 3) {
                        pantallaActual = "events"
                    }
                },
                alHacerClicEnExplorar = { pantallaActual = "publications" },
                alHacerClicEnSubirPublicacion = { pantallaActual = "upload_publication" },
                alHacerClicEnAtras = {
                    when (pantallaActual) {
                        "circuit_detail" -> pantallaActual = "circuits_and_poi"
                        "circuits_and_poi" -> {
                            if (pestanaSeleccionada == 1) {
                                pestanaSeleccionada = 0
                            } else {
                                pantallaActual = "main"
                            }
                        }
                        "city_detail" -> pantallaActual = "main"
                        "events" -> pantallaActual = "main"
                        "event_detail" -> pantallaActual = "events"
                        "publications" -> pantallaActual = "main"
                        "profile" -> pantallaActual = "main"
                        "upload_publication" -> {
                            pantallaActual = "publications"
                            viewModelPublicaciones.reiniciarEstadoSubida()
                        }
                        "upload_event" -> {
                            pantallaActual = "events"
                            viewModelEventos.reiniciarEstadoSubida()
                        }
                        else -> pantallaActual = "main"
                    }
                }
            )
        },
        containerColor = Celeste
    ) { innerPadding ->
        val paddingSuperior = innerPadding.calculateTopPadding()
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (pantallaActual) {
                "main" -> PantallaPrincipal(
                    viewModelPrincipal = viewModelPrincipal,
                    alHacerClicEnPinCiudad = { ciudad ->
                        viewModelPrincipal.seleccionarCiudad(ciudad.id)
                        pestanaSeleccionada = 0
                        pantallaActual = "circuits_and_poi"
                    },
                    alHacerClicEnCiudad = { ciudad ->
                        viewModelPrincipal.seleccionarCiudad(ciudad.id)
                        pantallaActual = "city_detail"
                    },
                    paddingSuperior = paddingSuperior
                )
                "profile" -> {
                    val estadoUiEmpresa by viewModelPerfil.estadoUiEmpresa.collectAsState()
                    val ciudades by viewModelPrincipal.ciudades
                    ContenidoPerfil(
                        usuario = usuario,
                        token = token,
                        alVolver = { pantallaActual = "main" },
                        alGuardar = { usuarioActualizado: Usuario ->
                            viewModelPerfil.actualizarPerfil(token, usuarioActualizado)
                        },
                        estadoUiPerfil = estadoUiPerfil,
                        estadoUiEmpresa = estadoUiEmpresa,
                        alRegistrarEmpresa = { t, emp -> viewModelPerfil.registrarEmpresa(t, emp) },
                        ciudades = ciudades,
                        alCerrarSesion = alCerrarSesion,
                        paddingSuperior = paddingSuperior
                    )
                }
                "circuits_and_poi" -> {
                    ciudadSeleccionada?.let { ciudad ->
                        PantallaCircuitosYPuntos(
                            ciudad = ciudad,
                            pestanaSeleccionada = pestanaSeleccionada,
                            alHacerClicEnVerMas = { circuito ->
                                viewModelPrincipal.seleccionarCircuito(circuito.id)
                                pantallaActual = "circuit_detail"
                            },
                            paddingSuperior = paddingSuperior
                        )
                    }
                }
                "circuit_detail" -> {
                    val circuito = viewModelPrincipal.circuitoSeleccionado
                    if (circuito != null) {
                        val puntosVisitados by viewModelPrincipal.puntosVisitados.collectAsState()
                        PantallaDetalleCircuito(
                            circuito = circuito,
                            puntosVisitados = puntosVisitados,
                            alAlternarVisitado = { puntoId -> solicitarUbicacionYMarcar(puntoId) }
                        )
                    } else {
                        pantallaActual = "circuits_and_poi"
                    }
                }
                "city_detail" -> {
                    ciudadSeleccionada?.let { ciudad ->
                        PantallaDetalleCiudad(
                            ciudad = ciudad,
                            alRegresar = { pantallaActual = "main" }
                        )
                    }
                }
                "events" -> {
                    PantallaEventos(
                        viewModel = viewModelEventos,
                        puedeSubir = usuario.esProtagonista,
                        alHacerClicEnSubir = { pantallaActual = "upload_event" },
                        alHacerClicEnEvento = { evento ->
                            eventoSeleccionado = evento
                            pantallaActual = "event_detail"
                        },
                        modoVista = pestanaSeleccionada,
                        paddingSuperior = paddingSuperior
                    )
                }
                "event_detail" -> {
                    eventoSeleccionado?.let { evento ->
                        PantallaDetalleEvento(
                            evento = evento,
                            viewModelEventos = viewModelEventos
                        )
                    }
                }
                "publications" -> {
                    val idCiudadSeleccionada = viewModelPrincipal.ciudadSeleccionada?.id
                    LaunchedEffect(idCiudadSeleccionada) {
                        viewModelPublicaciones.obtenerPublicaciones(idCiudad = idCiudadSeleccionada)
                    }
                    PantallaPublicaciones(
                        viewModel = viewModelPublicaciones,
                        alHacerClicEnSubir = { pantallaActual = "upload_publication" },
                        paddingSuperior = paddingSuperior
                    )
                }
                "upload_publication" -> {
                    val ciudades by viewModelPrincipal.ciudades
                    val estadoEventos by viewModelEventos.estadoUi
                    val eventos = (estadoEventos as? EstadoUiEventos.Exito)?.eventos ?: emptyList()
                    val estaSubiendo by viewModelPublicaciones.estaSubiendo
                    val subidaExitosa by viewModelPublicaciones.subidaExitosa
                    val mensajeError by viewModelPublicaciones.mensajeError

                    PantallaSubirPublicacion(
                        ciudades = ciudades,
                        eventos = eventos,
                        alVolver = {
                            pantallaActual = "publications"
                            viewModelPublicaciones.reiniciarEstadoSubida()
                        },
                        alSubir = { descripcion, idCiudad, idEmpresa, idEvento, uris ->
                            viewModelPublicaciones.subirPublicacion(descripcion, idCiudad, idEmpresa, idEvento, uris)
                        },
                        estaSubiendo = estaSubiendo,
                        subidaExitosa = subidaExitosa,
                        mensajeError = mensajeError,
                        paddingSuperior = paddingSuperior
                    )
                }
                "upload_event" -> {
                    val ciudades by viewModelPrincipal.ciudades
                    val estaSubiendo by viewModelEventos.estaSubiendo
                    val subidaExitosa by viewModelEventos.subidaExitosa
                    PantallaSubirEvento(
                        ciudades = ciudades,
                        alVolver = { 
                            pantallaActual = "events"
                            viewModelEventos.reiniciarEstadoSubida()
                        },
                        alSubir = { viewModelEventos.subirEvento(it) },
                        estaSubiendo = estaSubiendo,
                        subidaExitosa = subidaExitosa,
                        paddingSuperior = paddingSuperior
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    viewModelPrincipal: ViewModelPrincipal,
    alHacerClicEnPinCiudad: (Ciudad) -> Unit,
    alHacerClicEnCiudad: (Ciudad) -> Unit,
    paddingSuperior: Dp = 0.dp
) {
    val ciudades by viewModelPrincipal.ciudades
    val estaCargando by viewModelPrincipal.estaCargando
    val error by viewModelPrincipal.error

    PullToRefreshBox(
        isRefreshing = estaCargando,
        onRefresh = { viewModelPrincipal.obtenerCiudades(forzar = true) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = paddingSuperior + 8.dp, bottom = 76.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TarjetaPrincipal(
                ciudades = ciudades,
                estaCargando = estaCargando,
                error = error,
                alRefrescar = { viewModelPrincipal.obtenerCiudades(forzar = true) },
                alHacerClicEnPin = alHacerClicEnPinCiudad,
                alHacerClicEnCiudad = alHacerClicEnCiudad
            )
        }
    }
}

@Composable
fun BarraSuperior(
    titulo: String? = null,
    alHacerClicEnPerfil: () -> Unit,
    alHacerClicEnLogo: () -> Unit,
    alHacerClicEnAsistente: () -> Unit = {}
) {
    val formaBarraSuperior = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        
        val xCenter = w * (1.24f / 1.48f)
        val halfBase = w * 0.058f
        val xLeft = xCenter - halfBase
        val xRight = xCenter + halfBase
        val yPeak = h * 0.72f
        
        moveTo(0f, 0f)
        lineTo(w, 0f)
        lineTo(w, h)
        lineTo(xRight, h)
        cubicTo(
            xRight - halfBase * 0.45f, h,
            xCenter + halfBase * 0.35f, yPeak,
            xCenter, yPeak
        )
        cubicTo(
            xCenter - halfBase * 0.35f, yPeak,
            xLeft + halfBase * 0.45f, h,
            xLeft, h
        )
        lineTo(0f, h)
        close()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(formaBarraSuperior)
            .background(AzulPetroleo)
            .statusBarsPadding()
            .padding(top = 2.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lado Izquierdo: Logo (y título opcional)
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(start = 16.dp)
                    .clickable { alHacerClicEnLogo() },
                contentAlignment = Alignment.CenterStart
            ) {
                if (titulo != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = "Codice Logo",
                            modifier = Modifier.height(28.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = titulo,
                            color = GoldColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Codice Logo",
                        modifier = Modifier.height(34.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            // Icono del Asistente (Guardabarranco) a la izquierda del borde
            Box(
                modifier = Modifier
                    .weight(0.28f)
                    .clickable { alHacerClicEnAsistente() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconasistente),
                    contentDescription = "Asistente Guardabarranco",
                    modifier = Modifier
                        .height(34.dp)
                        .offset(y = 6.5.dp, x = -5.5.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Icono de Perfil (Usuario) a la derecha del borde
            Box(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(end = 8.dp)
                    .clickable { alHacerClicEnPerfil() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = GoldColor,
                    modifier = Modifier
                        .size(28.dp)
                        .offset(y = 3.0.dp)
                )
            }
        }
    }
}

@Composable
fun TarjetaPrincipal(
    ciudades: List<Ciudad>,
    estaCargando: Boolean,
    error: String?,
    alRefrescar: () -> Unit,
    alHacerClicEnPin: (Ciudad) -> Unit,
    alHacerClicEnCiudad: (Ciudad) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Área de mapa
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = Celeste.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxSize(0.8f)
                )
                Text(
                    "NICARAGUA",
                    color = AzulPetroleo.copy(alpha = 0.2f),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = GrisClaro)
            
            Box(modifier = Modifier.weight(0.65f)) {
                if (estaCargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldColor)
                } else if (error != null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                        Button(onClick = alRefrescar, colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo)) {
                            Text("Reintentar")
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(ciudades) { ciudad ->
                            ElementoUbicacion(
                                nombre = ciudad.nombre,
                                alHacerClicEnPin = { alHacerClicEnPin(ciudad) },
                                alHacerClicEnCiudad = { alHacerClicEnCiudad(ciudad) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = GrisClaro)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ElementoUbicacion(nombre: String, alHacerClicEnPin: () -> Unit, alHacerClicEnCiudad: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable { alHacerClicEnCiudad() }
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = GoldColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = nombre,
                fontSize = 22.sp,
                color = NegroPuro,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = alHacerClicEnPin) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Circuitos y Puntos de Interés",
                    tint = GoldColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun BarraNavegacionInferior(
    pantallaActual: String,
    pestanaSeleccionada: Int,
    alHacerClicEnInicio: () -> Unit,
    alSeleccionarPestana: (Int) -> Unit,
    alHacerClicEnExplorar: () -> Unit = {},
    alHacerClicEnSubirPublicacion: () -> Unit = {},
    alHacerClicEnAtras: () -> Unit = {}
) {
    val formaTresMonticulos = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val valleyY = h * 0.45f
        val peakSideControlY = -h * 0.15f
        val peakCenterControlY = -h * 0.25f
        
        moveTo(0f, valleyY)
        quadraticTo(w * 0.1667f, peakSideControlY, w * 0.3333f, valleyY)
        quadraticTo(w * 0.5000f, peakCenterControlY, w * 0.6667f, valleyY)
        quadraticTo(w * 0.8333f, peakSideControlY, w, valleyY)
        
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(formaTresMonticulos)
            .background(AzulPetroleo)
            .padding(bottom = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val esPantallaPrincipal = pantallaActual == "main"
            
            // Botón Izquierdo: Eventos en la pantalla principal, botón de retroceso en todas las demás vistas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        if (esPantallaPrincipal) {
                            alSeleccionarPestana(2) // Pestaña de eventos (Lista)
                        } else {
                            alHacerClicEnAtras()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (esPantallaPrincipal) {
                        Icons.Default.Event
                    } else {
                        Icons.AutoMirrored.Filled.ArrowBack
                    },
                    contentDescription = if (esPantallaPrincipal) "Eventos" else "Regresar",
                    tint = if (esPantallaPrincipal) GoldColor.copy(alpha = 0.5f) else GoldColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Botón Central: Inicio
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { alHacerClicEnInicio() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Inicio",
                    tint = GoldColor,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = 2.dp)
                )
            }

            // Botón Derecho: Alternar vista en eventos / circuitos y puntos de interés, o Publicaciones / Agregar Publicación
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        when (pantallaActual) {
                            "publications" -> {
                                alHacerClicEnSubirPublicacion()
                            }
                            "events" -> {
                                // Alternar entre Lista (2) y Calendario (3)
                                alSeleccionarPestana(if (pestanaSeleccionada == 2) 3 else 2)
                            }
                            "circuits_and_poi" -> {
                                // Alternar entre Circuitos (0) y Puntos de Interés (1)
                                alSeleccionarPestana(if (pestanaSeleccionada == 0) 1 else 0)
                            }
                            else -> {
                                alHacerClicEnExplorar()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (pantallaActual) {
                        "publications" -> Icons.Default.AddPhotoAlternate
                        "events" -> if (pestanaSeleccionada == 2) Icons.Default.CalendarMonth else Icons.AutoMirrored.Filled.List
                        "circuits_and_poi" -> if (pestanaSeleccionada == 0) Icons.Default.LocationOn else Icons.Default.Map
                        else -> Icons.Default.PhotoLibrary
                    },
                    contentDescription = when (pantallaActual) {
                        "publications" -> "Nueva Publicación"
                        "events" -> "Alternar vista"
                        "circuits_and_poi" -> if (pestanaSeleccionada == 0) "Puntos de Interés" else "Circuitos"
                        else -> "Publicaciones"
                    },
                    tint = if (pantallaActual == "publications" || pantallaActual == "circuits_and_poi") GoldColor else GoldColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaBarraNavegacionInferior() {
    Codice路Theme {
        BarraNavegacionInferior(
            pantallaActual = "main",
            pestanaSeleccionada = 0,
            alHacerClicEnInicio = {},
            alSeleccionarPestana = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaBarraSuperior() {
    Codice路Theme {
        BarraSuperior(alHacerClicEnPerfil = {}, alHacerClicEnLogo = {})
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaTarjetaPrincipal() {
    Codice路Theme {
        TarjetaPrincipal(
            ciudades = listOf(
                Ciudad(1, "León", "Ciudad universitaria", null, 0.0, 0.0, emptyList(), emptyList(), emptyList()),
                Ciudad(2, "Granada", "La Gran Sultana", null, 0.0, 0.0, emptyList(), emptyList(), emptyList())
            ),
            estaCargando = false,
            error = null,
            alRefrescar = {},
            alHacerClicEnPin = {},
            alHacerClicEnCiudad = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPorDefecto() {
    Codice路Theme {
        PantallaPrincipal(
            viewModelPrincipal = viewModel(),
            alHacerClicEnPinCiudad = {},
            alHacerClicEnCiudad = {}
        )
    }
}
