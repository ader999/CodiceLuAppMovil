package com.example.codise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.codise.utils.aUrlCompleta
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
import com.example.codise.data.GestorIdioma
import com.example.codise.data.IdiomaApp
import com.example.codise.data.ServicioApi
import com.example.codise.data.Usuario
import com.example.codise.ui.theme.*
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import com.example.codise.utils.CadenasIdiomas
import com.example.codise.utils.LocalCadenas
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            Codice路Theme {
                AplicacionPrincipal()
            }
        }
    }
}

@Composable
fun AplicacionPrincipal() {
    val contexto = LocalContext.current
    val gestorIdioma = remember { GestorIdioma.obtenerInstancia(contexto) }
    val idiomaActual by gestorIdioma.idiomaActual.collectAsState()
    val cadenas = remember(idiomaActual) { CadenasIdiomas.obtener(idiomaActual) }

    CompositionLocalProvider(LocalCadenas provides cadenas) {
        val viewModelLogin: ViewModelLogin = viewModel()
        val estadoUi by viewModelLogin.estadoUi.collectAsState()

        if (estadoUi is EstadoUiLogin.Exito) {
            val respuesta = (estadoUi as EstadoUiLogin.Exito).respuesta
            AplicacionAutenticada(
                usuario = respuesta.usuario,
                token = respuesta.tokens.access,
                alCerrarSesion = { viewModelLogin.cerrarSesion() },
                gestorIdioma = gestorIdioma,
                idiomaActual = idiomaActual
            )
        } else {
            PantallaLogin(
                viewModel = viewModelLogin,
                gestorIdioma = gestorIdioma,
                idiomaActual = idiomaActual
            )
        }
    }
}

@Composable
fun AplicacionAutenticada(
    usuario: Usuario,
    token: String,
    alCerrarSesion: () -> Unit,
    gestorIdioma: GestorIdioma,
    idiomaActual: IdiomaApp
) {
    val viewModelPerfil: ViewModelPerfil = viewModel()
    val estadoUiPerfil by viewModelPerfil.estadoUi.collectAsState()
    val empresasUsuario by viewModelPerfil.empresasUsuario.collectAsState()
    val usuarioActual = when (val estado = estadoUiPerfil) {
        is EstadoUiPerfil.Exito -> estado.usuario
        else -> usuario
    }

    LaunchedEffect(usuario.id, usuario.nombreUsuario, token) {
        viewModelPerfil.cargarPerfilYEmpresas(token, usuario)
    }
    val viewModelPrincipal: ViewModelPrincipal = viewModel()
    val viewModelEventos: ViewModelEventos = viewModel()
    val viewModelPublicaciones: ViewModelPublicaciones = viewModel()
    val viewModelAsistente: ViewModelAsistente = viewModel()
    val idsPuntosVisitados by viewModelPrincipal.idsPuntosVisitados.collectAsState()
    val contexto = LocalContext.current
    val clienteUbicacion = remember { LocationServices.getFusedLocationProviderClient(contexto) }

    var pantallaActual by remember { mutableStateOf("main") }
    var mostrarFormularioPerfil by remember { mutableStateOf(false) }
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

    val cadenas = LocalCadenas.current

    // Obtener ubicación GPS para el Asistente
    val obtenerUbicacionActualAsistente = {
        Toast.makeText(contexto, cadenas.asistenteActivandoGps, Toast.LENGTH_SHORT).show()
        try {
            clienteUbicacion.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { ubicacion ->
                if (ubicacion != null) {
                    viewModelAsistente.actualizarUbicacion(ubicacion.latitude, ubicacion.longitude)
                    Toast.makeText(contexto, cadenas.asistenteUbicacionActiva, Toast.LENGTH_SHORT).show()
                } else {
                    clienteUbicacion.lastLocation.addOnSuccessListener { ultimaUbicacion ->
                        if (ultimaUbicacion != null) {
                            viewModelAsistente.actualizarUbicacion(ultimaUbicacion.latitude, ultimaUbicacion.longitude)
                            Toast.makeText(contexto, cadenas.asistenteUbicacionActiva, Toast.LENGTH_SHORT).show()
                        } else {
                            val singleRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                                .setMaxUpdates(1)
                                .build()
                            val callback = object : LocationCallback() {
                                override fun onLocationResult(result: LocationResult) {
                                    val loc = result.lastLocation
                                    if (loc != null) {
                                        viewModelAsistente.actualizarUbicacion(loc.latitude, loc.longitude)
                                        Toast.makeText(contexto, cadenas.asistenteUbicacionActiva, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(contexto, cadenas.asistenteGpsNoActivado, Toast.LENGTH_SHORT).show()
                                    }
                                    clienteUbicacion.removeLocationUpdates(this)
                                }
                            }
                            clienteUbicacion.requestLocationUpdates(singleRequest, callback, Looper.getMainLooper())
                        }
                    }.addOnFailureListener {
                        Toast.makeText(contexto, cadenas.asistenteGpsNoActivado, Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(contexto, cadenas.asistenteGpsNoActivado, Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(contexto, cadenas.asistentePermisoUbicacionRequerido, Toast.LENGTH_SHORT).show()
        }
    }

    // Lanzador para solicitar al usuario que encienda el GPS del dispositivo mediante el diálogo de Google Play Services
    val lanzadorAjustesGpsAsistente = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            obtenerUbicacionActualAsistente()
        } else {
            Toast.makeText(contexto, cadenas.asistenteGpsNoActivado, Toast.LENGTH_SHORT).show()
        }
    }

    // Verificar si el hardware/servicio GPS del dispositivo está encendido
    val verificarAjustesGpsYObtenerUbicacion = {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(contexto)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                obtenerUbicacionActualAsistente()
            }
            .addOnFailureListener { excepcion ->
                if (excepcion is ResolvableApiException) {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(excepcion.resolution).build()
                        lanzadorAjustesGpsAsistente.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        contexto.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    val lanzadorPermisosUbicacionAsistente = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            verificarAjustesGpsYObtenerUbicacion()
        } else {
            Toast.makeText(contexto, cadenas.asistentePermisoUbicacionRequerido, Toast.LENGTH_SHORT).show()
        }
    }

    val solicitarUbicacionAsistente = {
        if (viewModelAsistente.ubicacionGps.value != null) {
            // Alternar: si ya está activa, se desactiva
            viewModelAsistente.limpiarUbicacion()
            Toast.makeText(contexto, cadenas.asistenteUbicacionDesactivada, Toast.LENGTH_SHORT).show()
        } else {
            val tienePermiso = ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (tienePermiso) {
                verificarAjustesGpsYObtenerUbicacion()
            } else {
                lanzadorPermisosUbicacionAsistente.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
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
                viewModelPerfil.cargarPerfilYEmpresas(token, usuarioActual)
            }
        }
        propietarioCicloVida.lifecycle.addObserver(observador)
        onDispose {
            propietarioCicloVida.lifecycle.removeObserver(observador)
        }
    }

    var mostrarDialogoIdioma by remember { mutableStateOf(false) }

    // Interceptar botón atrás del sistema cuando se esté en el asistente
    BackHandler(enabled = pantallaActual == "assistant") {
        pantallaActual = "main"
    }

    val cambiarIdiomaApp: (IdiomaApp) -> Unit = { nuevoIdioma ->
        gestorIdioma.cambiarIdioma(nuevoIdioma)
        ServicioApi.limpiarCache()
        viewModelPrincipal.obtenerCiudades(forzar = true)
        viewModelEventos.obtenerEventos()
        viewModelPublicaciones.obtenerPublicaciones()
        val nuevasCadenas = CadenasIdiomas.obtener(nuevoIdioma)
        viewModelAsistente.inicializarBienvenida(nuevasCadenas.asistenteMensaje, nuevoIdioma.codigo)
    }

    if (mostrarDialogoIdioma) {
        DialogoSeleccionIdioma(
            idiomaActual = idiomaActual,
            alSeleccionarIdioma = cambiarIdiomaApp,
            alCerrar = { mostrarDialogoIdioma = false }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                fotoPerfil = usuarioActual.fotoPerfil,
                idiomaActual = idiomaActual,
                alHacerClicEnPerfil = {
                    pantallaActual = "profile"
                    mostrarFormularioPerfil = false
                },
                alHacerClicEnLogo = {
                    pantallaActual = "main"
                    mostrarFormularioPerfil = false
                },
                alHacerClicEnAsistente = {
                    pantallaActual = "assistant"
                    mostrarFormularioPerfil = false
                },
                alHacerClicEnIdioma = { mostrarDialogoIdioma = true }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                pantallaActual = pantallaActual,
                pestanaSeleccionada = pestanaSeleccionada,
                mostrarFormularioPerfil = mostrarFormularioPerfil,
                alHacerClicEnInicio = {
                    pantallaActual = "main"
                    mostrarFormularioPerfil = false
                },
                alSeleccionarPestana = { 
                    pestanaSeleccionada = it
                    if (it == 2 || it == 3) {
                        pantallaActual = "events"
                    }
                    mostrarFormularioPerfil = false
                },
                alHacerClicEnExplorar = {
                    pantallaActual = "publications"
                    mostrarFormularioPerfil = false
                },
                alHacerClicEnSubirPublicacion = { pantallaActual = "upload_publication" },
                alAlternarFormularioPerfil = { mostrarFormularioPerfil = !mostrarFormularioPerfil },
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
                        "profile" -> {
                            if (mostrarFormularioPerfil) {
                                mostrarFormularioPerfil = false
                            } else {
                                pantallaActual = "main"
                            }
                        }
                        "upload_publication" -> {
                            pantallaActual = "publications"
                            viewModelPublicaciones.reiniciarEstadoSubida()
                        }
                        "upload_event" -> {
                            pantallaActual = "events"
                            viewModelEventos.reiniciarEstadoSubida()
                        }
                        "assistant" -> pantallaActual = "main"
                        else -> pantallaActual = "main"
                    }
                }
            )
        },
        containerColor = Celeste
    ) { innerPadding ->
        val paddingSuperior = innerPadding.calculateTopPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
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
                        usuario = usuarioActual,
                        token = token,
                        alVolver = {
                            if (mostrarFormularioPerfil) {
                                mostrarFormularioPerfil = false
                            } else {
                                pantallaActual = "main"
                            }
                        },
                        alGuardar = { usuarioActualizado, uriFoto ->
                            viewModelPerfil.actualizarPerfil(token, usuarioActualizado, uriFoto)
                        },
                        alCambiarFoto = { uri ->
                            viewModelPerfil.actualizarFotoPerfil(token, uri)
                        },
                        estadoUiPerfil = estadoUiPerfil,
                        estadoUiEmpresa = estadoUiEmpresa,
                        alRegistrarEmpresa = { t, emp -> viewModelPerfil.registrarEmpresa(t, emp) },
                        ciudades = ciudades,
                        alCerrarSesion = alCerrarSesion,
                        mostrarFormulario = mostrarFormularioPerfil,
                        alAlternarFormulario = { mostrarFormularioPerfil = !mostrarFormularioPerfil },
                        empresasUsuario = empresasUsuario,
                        idiomaActual = idiomaActual,
                        alCambiarIdioma = { mostrarDialogoIdioma = true },
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
                            alAlternarVisitado = { puntoId -> solicitarUbicacionYMarcar(puntoId) },
                            paddingSuperior = paddingSuperior
                        )
                    } else {
                        pantallaActual = "circuits_and_poi"
                    }
                }
                "city_detail" -> {
                    ciudadSeleccionada?.let { ciudad ->
                        PantallaDetalleCiudad(
                            ciudad = ciudad,
                            alRegresar = { pantallaActual = "main" },
                            paddingSuperior = paddingSuperior
                        )
                    }
                }
                "events" -> {
                    PantallaEventos(
                        viewModel = viewModelEventos,
                        puedeSubir = usuarioActual.esProtagonista || empresasUsuario.isNotEmpty(),
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
                            viewModelEventos = viewModelEventos,
                            paddingSuperior = paddingSuperior
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
                        alSubir = { solicitud, uriImagen -> viewModelEventos.subirEvento(solicitud, uriImagen) },
                        estaSubiendo = estaSubiendo,
                        subidaExitosa = subidaExitosa,
                        paddingSuperior = paddingSuperior
                    )
                }
                "assistant" -> {
                    PantallaAsistente(
                        viewModel = viewModelAsistente,
                        idiomaActual = idiomaActual,
                        paddingSuperior = paddingSuperior,
                        alSolicitarUbicacion = { solicitarUbicacionAsistente() }
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
    fotoPerfil: String? = null,
    idiomaActual: IdiomaApp = IdiomaApp.ESPANOL,
    alHacerClicEnPerfil: () -> Unit,
    alHacerClicEnLogo: () -> Unit,
    alHacerClicEnAsistente: () -> Unit = {},
    alHacerClicEnIdioma: () -> Unit = {}
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
            // Lado Izquierdo: Logo
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(start = 16.dp)
                    .clickable { alHacerClicEnLogo() },
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Codice Logo",
                    modifier = Modifier.height(34.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Selector de Idioma (Español, English, 中文)
            BotonSelectorIdioma(
                idiomaActual = idiomaActual,
                alHacerClic = alHacerClicEnIdioma,
                modifier = Modifier
                    .offset(x = (-16).dp)
                    .padding(horizontal = 4.dp)
            )

            // Icono del Asistente (Guardabarranco) a la izquierda del borde
            Box(
                modifier = Modifier
                    .weight(0.28f)
                    .offset(x = (-14).dp)
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
                if (!fotoPerfil.isNullOrBlank()) {
                    AsyncImage(
                        model = fotoPerfil.aUrlCompleta(),
                        contentDescription = "Perfil",
                        modifier = Modifier
                            .size(28.dp)
                            .offset(y = 3.0.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, GoldColor, CircleShape),
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.graphics.painter.ColorPainter(GoldColor.copy(alpha = 0.3f))
                    )
                } else {
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
    val cadenas = LocalCadenas.current
    var ciudadSeleccionadaEnMapa by remember { mutableStateOf<Ciudad?>(null) }

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
            // Área de mapa croquis de Nicaragua con ciudades creativas
            Box(
                modifier = Modifier
                    .weight(0.40f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CroquisNicaragua(
                    ciudades = ciudades,
                    ciudadSeleccionada = ciudadSeleccionadaEnMapa,
                    alSeleccionarCiudad = { ciudadSeleccionadaEnMapa = it },
                    alHacerClicEnCiudad = alHacerClicEnCiudad,
                    alHacerClicEnPin = alHacerClicEnPin,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = GrisClaro)
            
            Box(modifier = Modifier.weight(0.60f)) {
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
                            Text(cadenas.reintentar)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(ciudades) { ciudad ->
                            ElementoUbicacion(
                                nombre = ciudad.nombre,
                                alHacerClicEnPin = { alHacerClicEnPin(ciudad) },
                                alHacerClicEnCiudad = {
                                    ciudadSeleccionadaEnMapa = ciudad
                                    alHacerClicEnCiudad(ciudad)
                                }
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
    val cadenas = LocalCadenas.current
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
                    contentDescription = cadenas.circuitosTuristicos,
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
    mostrarFormularioPerfil: Boolean = false,
    alHacerClicEnInicio: () -> Unit,
    alSeleccionarPestana: (Int) -> Unit,
    alHacerClicEnExplorar: () -> Unit = {},
    alHacerClicEnSubirPublicacion: () -> Unit = {},
    alAlternarFormularioPerfil: () -> Unit = {},
    alHacerClicEnAtras: () -> Unit = {}
) {
    val cadenas = LocalCadenas.current
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

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
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
                        contentDescription = if (esPantallaPrincipal) cadenas.eventos else cadenas.regresar,
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
                        contentDescription = cadenas.inicio,
                        tint = GoldColor,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(bottom = 2.dp)
                    )
                }

                // Botón Derecho: Alternar vista en eventos / circuitos y puntos de interés, o Publicaciones / Agregar Publicación / Formulario Perfil
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
                                "profile" -> {
                                    alAlternarFormularioPerfil()
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
                            "profile" -> if (mostrarFormularioPerfil) Icons.Default.Person else Icons.Default.EditNote
                            else -> Icons.Default.PhotoLibrary
                        },
                        contentDescription = when (pantallaActual) {
                            "publications" -> cadenas.nuevaPublicacion
                            "events" -> cadenas.eventos
                            "circuits_and_poi" -> if (pestanaSeleccionada == 0) cadenas.puntosDeInteres else cadenas.circuitos
                            "profile" -> if (mostrarFormularioPerfil) cadenas.perfil else cadenas.editarPerfil
                            else -> cadenas.publicaciones
                        },
                        tint = if (pantallaActual in listOf("publications", "circuits_and_poi", "profile")) GoldColor else GoldColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(AzulPetroleo)
        )
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
