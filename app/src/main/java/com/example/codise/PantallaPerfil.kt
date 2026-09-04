package com.example.codise

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.CATEGORIAS_EMPRESA
import com.example.codise.data.Ciudad
import com.example.codise.data.Empresa
import com.example.codise.data.IdiomaApp
import com.example.codise.data.OpcionCategoriaEmpresa
import com.example.codise.data.Usuario
import com.example.codise.ui.theme.*
import com.example.codise.utils.LocalCadenas
import com.example.codise.utils.aUrlCompleta

@Composable
fun ContenidoPerfil(
    usuario: Usuario,
    token: String,
    alVolver: () -> Unit,
    alGuardar: (Usuario, Uri?) -> Unit,
    alCambiarFoto: (Uri) -> Unit = {},
    estadoUiPerfil: EstadoUiPerfil,
    estadoUiEmpresa: EstadoUiEmpresa,
    alRegistrarEmpresa: (String, Empresa) -> Unit,
    ciudades: List<Ciudad>,
    alCerrarSesion: () -> Unit,
    mostrarFormulario: Boolean = false,
    alAlternarFormulario: () -> Unit = {},
    empresasUsuario: List<Empresa> = emptyList(),
    idiomaActual: IdiomaApp = IdiomaApp.ESPANOL,
    alCambiarIdioma: () -> Unit = {},
    paddingSuperior: Dp = 0.dp
) {
    val cadenas = LocalCadenas.current
    var nombre by remember(usuario) { mutableStateOf(usuario.nombre.orEmpty()) }
    var apellido by remember(usuario) { mutableStateOf(usuario.apellido.orEmpty()) }
    var nombreUsuario by remember(usuario) { mutableStateOf(usuario.nombreUsuario.orEmpty()) }
    var correo by remember(usuario) { mutableStateOf(usuario.correoElectronico.orEmpty()) }
    var telefono by remember(usuario) { mutableStateOf(usuario.telefono.orEmpty()) }
    var uriFotoSeleccionada by remember { mutableStateOf<Uri?>(null) }

    val lanzadorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            uriFotoSeleccionada = uri
            if (!mostrarFormulario) {
                alCambiarFoto(uri)
            }
        }
    }

    LaunchedEffect(usuario) {
        nombre = usuario.nombre.orEmpty()
        apellido = usuario.apellido.orEmpty()
        nombreUsuario = usuario.nombreUsuario.orEmpty()
        correo = usuario.correoElectronico.orEmpty()
        telefono = usuario.telefono.orEmpty()
    }

    var mostrarFormularioEmpresa by remember { mutableStateOf(false) }
    val estaCargandoPerfil = estadoUiPerfil is EstadoUiPerfil.Cargando

    LaunchedEffect(estadoUiEmpresa) {
        if (estadoUiEmpresa is EstadoUiEmpresa.Exito) {
            mostrarFormularioEmpresa = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, top = paddingSuperior + 16.dp, bottom = 80.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icono de perfil con foto de usuario y opción para cambiarla
        IconoPerfilUsuario(
            fotoPerfil = usuario.fotoPerfil,
            uriFotoLocal = uriFotoSeleccionada,
            alHacerClicEnCambiarFoto = {
                lanzadorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            estaCargando = estaCargandoPerfil,
            tamano = if (mostrarFormulario) 100.dp else 110.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = cadenas.cambiarFoto,
            color = AzulPetroleo.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable {
                lanzadorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (mostrarFormulario) {
            // VISTA FORMULARIO DE EDICIÓN
            Text(
                text = cadenas.editarPerfil,
                color = AzulPetroleo,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoBase),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CampoTextoPerfil(etiqueta = cadenas.nombre, valor = nombre, alCambiarValor = { nombre = it })
                    CampoTextoPerfil(etiqueta = cadenas.apellido, valor = apellido, alCambiarValor = { apellido = it })
                    CampoTextoPerfil(etiqueta = cadenas.nombreUsuario, valor = nombreUsuario, alCambiarValor = { nombreUsuario = it })
                    CampoTextoPerfil(etiqueta = cadenas.correoElectronico, valor = correo, alCambiarValor = { correo = it })
                    CampoTextoPerfil(etiqueta = cadenas.telefono, valor = telefono, alCambiarValor = { telefono = it })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (estadoUiPerfil is EstadoUiPerfil.Error) {
                Text(
                    text = estadoUiPerfil.mensaje,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (estadoUiPerfil is EstadoUiPerfil.Exito) {
                Text(
                    text = cadenas.perfilActualizado,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    val usuarioActualizado = usuario.copy(
                        nombre = nombre,
                        apellido = apellido,
                        nombreUsuario = nombreUsuario,
                        correoElectronico = correo,
                        telefono = telefono
                    )
                    alGuardar(usuarioActualizado, uriFotoSeleccionada)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                shape = RoundedCornerShape(12.dp),
                enabled = !estaCargandoPerfil
            ) {
                if (estaCargandoPerfil) {
                    CircularProgressIndicator(color = GoldColor, modifier = Modifier.size(24.dp))
                } else {
                    Text(cadenas.guardar, color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = alAlternarFormulario,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulPetroleo),
                border = androidx.compose.foundation.BorderStroke(1.dp, AzulPetroleo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(cadenas.cancelar, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            // VISTA INFORMACIÓN DE PERFIL (VISTA PRINCIPAL)
            val nombreCompleto = "${usuario.nombre.orEmpty()} ${usuario.apellido.orEmpty()}".trim()
            Text(
                text = nombreCompleto.ifBlank { usuario.nombreUsuario.orEmpty() },
                color = AzulPetroleo,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "@${usuario.nombreUsuario.orEmpty()}",
                color = NegroPuro.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            val esProtagonistaEfectivo = usuario.esProtagonista || empresasUsuario.isNotEmpty()

            // Etiquetas de rol
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (usuario.esStaff) {
                    InsigniaRol(texto = cadenas.rolStaff, colorFondo = AzulPetroleo, colorTexto = GoldColor)
                }
                if (esProtagonistaEfectivo) {
                    InsigniaRol(texto = cadenas.rolProtagonista, colorFondo = GoldColor, colorTexto = AzulPetroleo)
                } else if (usuario.esTurista) {
                    InsigniaRol(texto = cadenas.rolTurista, colorFondo = Celeste.copy(alpha = 0.8f), colorTexto = AzulPetroleo)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta con información detallada
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoBase),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ElementoDetallePerfil(
                        icono = Icons.Default.Email,
                        titulo = cadenas.correoElectronico,
                        valor = usuario.correoElectronico.orEmpty()
                    )
                    HorizontalDivider(color = GrisClaro.copy(alpha = 0.4f), thickness = 0.5.dp)
                    ElementoDetallePerfil(
                        icono = Icons.Default.Phone,
                        titulo = cadenas.telefono,
                        valor = if (usuario.telefono.isNullOrBlank()) cadenas.noRegistrado else usuario.telefono
                    )
                    HorizontalDivider(color = GrisClaro.copy(alpha = 0.4f), thickness = 0.5.dp)
                    ElementoDetallePerfil(
                        icono = Icons.Default.Badge,
                        titulo = cadenas.nombreUsuario,
                        valor = usuario.nombreUsuario.orEmpty()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de Selección de Idioma
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { alCambiarIdioma() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoBase),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = AzulPetroleo,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = cadenas.idioma,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AzulPetroleo
                            )
                            Text(
                                text = "${idiomaActual.bandera} ${idiomaActual.etiquetaNativa}",
                                fontSize = 13.sp,
                                color = NegroPuro.copy(alpha = 0.7f)
                            )
                        }
                    }
                    BotonSelectorIdioma(
                        idiomaActual = idiomaActual,
                        alHacerClic = alCambiarIdioma
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (estadoUiPerfil is EstadoUiPerfil.Error) {
                Text(
                    text = estadoUiPerfil.mensaje,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (estadoUiPerfil is EstadoUiPerfil.Exito) {
                Text(
                    text = cadenas.perfilActualizado,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = alCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(cadenas.cerrarSesion, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            if (estadoUiEmpresa is EstadoUiEmpresa.Exito) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldColor.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AzulPetroleo,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "¡Empresa registrada con éxito!",
                                color = AzulPetroleo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Ahora eres Protagonista. Ya puedes crear eventos y publicaciones.",
                                color = NegroPuro.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            if (esProtagonistaEfectivo && !mostrarFormularioEmpresa) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = GrisClaro.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AzulPetroleo.copy(alpha = 0.06f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AzulPetroleo.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = GoldColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Perfil de Protagonista",
                                color = AzulPetroleo,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tu cuenta tiene permisos activos para publicar eventos, ofertas y contenido de tus negocios.",
                            color = NegroPuro.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }

                if (empresasUsuario.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (empresasUsuario.size > 1) "Mis Empresas Registradas" else "Mi Empresa Registrada",
                        color = AzulPetroleo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    empresasUsuario.forEach { empresa ->
                        TarjetaEmpresaUsuario(empresa = empresa)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { mostrarFormularioEmpresa = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulPetroleo),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AzulPetroleo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar otra empresa", color = AzulPetroleo, fontWeight = FontWeight.Bold)
                }
            }

            if (!esProtagonistaEfectivo && !mostrarFormularioEmpresa) {
                Spacer(modifier = Modifier.height(28.dp))
                HorizontalDivider(color = GrisClaro.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    "¿Eres dueño de un negocio?",
                    color = AzulPetroleo,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Regístrate como protagonista para publicar tus eventos y atraer más visitantes.",
                    color = NegroPuro.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Button(
                    onClick = { mostrarFormularioEmpresa = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = AzulPetroleo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Convertirse en Protagonista", color = AzulPetroleo, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (mostrarFormularioEmpresa) {
                FormularioRegistroEmpresa(
                    token = token,
                    ciudades = ciudades,
                    estadoUiEmpresa = estadoUiEmpresa,
                    alRegistrar = alRegistrarEmpresa,
                    alCancelar = { mostrarFormularioEmpresa = false }
                )
            }
        }
    }
}

@Composable
fun IconoPerfilUsuario(
    fotoPerfil: String?,
    uriFotoLocal: Uri?,
    alHacerClicEnCambiarFoto: () -> Unit,
    estaCargando: Boolean = false,
    tamano: Dp = 100.dp
) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clickable(onClick = alHacerClicEnCambiarFoto),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(AzulPetroleo)
                .border(2.5.dp, GoldColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (uriFotoLocal != null) {
                AsyncImage(
                    model = uriFotoLocal,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (!fotoPerfil.isNullOrBlank()) {
                AsyncImage(
                    model = fotoPerfil.aUrlCompleta(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.graphics.painter.ColorPainter(AzulPetroleo)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = GoldColor,
                    modifier = Modifier.size(tamano * 0.6f)
                )
            }

            if (estaCargando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GoldColor,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
        }

        // Insignia con icono de cámara
        Box(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(GoldColor)
                .border(1.5.dp, BlancoBase, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Cambiar foto",
                tint = AzulPetroleo,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun InsigniaRol(texto: String, colorFondo: Color, colorTexto: Color) {
    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = texto,
            color = colorTexto,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TarjetaEmpresaUsuario(empresa: Empresa) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = AzulPetroleo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = empresa.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AzulPetroleo,
                    modifier = Modifier.weight(1f)
                )
                InsigniaRol(
                    texto = empresa.categoria,
                    colorFondo = GoldColor.copy(alpha = 0.25f),
                    colorTexto = AzulPetroleo
                )
            }
            if (empresa.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = empresa.descripcion,
                    color = NegroPuro.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
            if (empresa.direccion.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = GoldColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val ubicacionTexto = buildString {
                        append(empresa.direccion)
                        if (!empresa.ciudadNombre.isNullOrBlank()) {
                            append(" (${empresa.ciudadNombre})")
                        }
                    }
                    Text(
                        text = ubicacionTexto,
                        color = NegroPuro.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
            if (empresa.telefonoContacto.isNotBlank() || empresa.emailContacto.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (empresa.telefonoContacto.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = NegroPuro.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = empresa.telefonoContacto,
                            color = NegroPuro.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                    if (empresa.telefonoContacto.isNotBlank() && empresa.emailContacto.isNotBlank()) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    if (empresa.emailContacto.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = NegroPuro.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = empresa.emailContacto,
                            color = NegroPuro.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElementoDetallePerfil(
    icono: ImageVector,
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Celeste.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = AzulPetroleo,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                fontSize = 12.sp,
                color = NegroPuro.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valor.ifBlank { "No registrado" },
                fontSize = 16.sp,
                color = AzulPetroleo,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioRegistroEmpresa(
    token: String,
    ciudades: List<Ciudad>,
    estadoUiEmpresa: EstadoUiEmpresa,
    alRegistrar: (String, Empresa) -> Unit,
    alCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<OpcionCategoriaEmpresa?>(null) }
    var categoriasExpandidas by remember { mutableStateOf(false) }
    var ciudadSeleccionada by remember { mutableStateOf<Ciudad?>(null) }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var sitioWeb by remember { mutableStateOf("") }
    var aceptaInversiones by remember { mutableStateOf(false) }

    var ciudadesExpandidas by remember { mutableStateOf(false) }

    Spacer(modifier = Modifier.height(32.dp))
    HorizontalDivider(color = GrisClaro.copy(alpha = 0.5f))
    Spacer(modifier = Modifier.height(24.dp))

    Text(
        "Registro de Empresa",
        color = AzulPetroleo,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CampoTextoPerfil(etiqueta = "Nombre de la Empresa", valor = nombre, alCambiarValor = { nombre = it })
            CampoTextoPerfil(etiqueta = "Descripción", valor = descripcion, alCambiarValor = { descripcion = it })

            // Selector de categoría
            Column {
                Text(text = "Categoría", color = AzulPetroleo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = categoriasExpandidas,
                    onExpandedChange = { categoriasExpandidas = !categoriasExpandidas }
                ) {
                    OutlinedTextField(
                        value = categoriaSeleccionada?.etiqueta ?: "Seleccionar categoría",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriasExpandidas) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AzulPetroleo,
                            unfocusedTextColor = AzulPetroleo,
                            focusedBorderColor = AzulPetroleo,
                            unfocusedBorderColor = GrisClaro
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoriasExpandidas,
                        onDismissRequest = { categoriasExpandidas = false }
                    ) {
                        CATEGORIAS_EMPRESA.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.etiqueta) },
                                onClick = {
                                    categoriaSeleccionada = cat
                                    categoriasExpandidas = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Selector de ciudad
            Column {
                Text(text = "Ciudad", color = AzulPetroleo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = ciudadesExpandidas,
                    onExpandedChange = { ciudadesExpandidas = !ciudadesExpandidas }
                ) {
                    OutlinedTextField(
                        value = ciudadSeleccionada?.nombre ?: "Seleccionar ciudad",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ciudadesExpandidas) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AzulPetroleo,
                            unfocusedTextColor = AzulPetroleo,
                            focusedBorderColor = AzulPetroleo,
                            unfocusedBorderColor = GrisClaro
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = ciudadesExpandidas,
                        onDismissRequest = { ciudadesExpandidas = false }
                    ) {
                        ciudades.forEach { ciudad ->
                            DropdownMenuItem(
                                text = { Text(ciudad.nombre) },
                                onClick = {
                                    ciudadSeleccionada = ciudad
                                    ciudadesExpandidas = false
                                }
                            )
                        }
                    }
                }
            }

            CampoTextoPerfil(etiqueta = "Dirección", valor = direccion, alCambiarValor = { direccion = it })
            CampoTextoPerfil(etiqueta = "Teléfono de Contacto", valor = telefono, alCambiarValor = { telefono = it })
            CampoTextoPerfil(etiqueta = "Correo de Contacto", valor = email, alCambiarValor = { email = it })
            CampoTextoPerfil(etiqueta = "Sitio Web (Opcional)", valor = sitioWeb, alCambiarValor = { sitioWeb = it })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { aceptaInversiones = !aceptaInversiones }
            ) {
                Checkbox(
                    checked = aceptaInversiones,
                    onCheckedChange = { aceptaInversiones = it },
                    colors = CheckboxDefaults.colors(checkedColor = AzulPetroleo)
                )
                Text("¿Acepta inversiones?", color = AzulPetroleo, fontSize = 16.sp)
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (estadoUiEmpresa is EstadoUiEmpresa.Error) {
        Text(
            text = estadoUiEmpresa.mensaje,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    if (estadoUiEmpresa is EstadoUiEmpresa.Exito) {
        Text(
            text = "¡Empresa registrada con éxito! Ahora eres protagonista.",
            color = AzulPetroleo,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = alCancelar,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulPetroleo)
        ) {
            Text("Cancelar")
        }

        Button(
            onClick = {
                val cat = categoriaSeleccionada
                val ciudad = ciudadSeleccionada
                if (ciudad != null && cat != null) {
                    val urlWebFormateada = sitioWeb.trim().takeIf { it.isNotBlank() }?.let { url ->
                        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
                            "https://$url"
                        } else {
                            url
                        }
                    }
                    val empresa = Empresa(
                        nombre = nombre.trim(),
                        descripcion = descripcion.trim(),
                        categoria = cat.clave,
                        ciudad = ciudad.id,
                        direccion = direccion.trim(),
                        telefonoContacto = telefono.trim(),
                        emailContacto = email.trim(),
                        sitioWeb = urlWebFormateada,
                        latitud = ciudad.latitudCentro,
                        longitud = ciudad.longitudCentro,
                        aceptaInversiones = aceptaInversiones
                    )
                    alRegistrar(token, empresa)
                }
            },
            modifier = Modifier.weight(1f).height(56.dp),
            enabled = estadoUiEmpresa !is EstadoUiEmpresa.Cargando && ciudadSeleccionada != null && categoriaSeleccionada != null && nombre.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo)
        ) {
            if (estadoUiEmpresa is EstadoUiEmpresa.Cargando) {
                CircularProgressIndicator(color = GoldColor, modifier = Modifier.size(24.dp))
            } else {
                Text("Registrar", color = GoldColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    usuario: Usuario,
    token: String,
    alVolver: () -> Unit,
    alGuardar: (Usuario) -> Unit,
    alCambiarFoto: (Uri) -> Unit = {},
    estadoUiPerfil: EstadoUiPerfil,
    estadoUiEmpresa: EstadoUiEmpresa,
    alRegistrarEmpresa: (String, Empresa) -> Unit,
    ciudades: List<Ciudad>,
    alCerrarSesion: () -> Unit,
    empresasUsuario: List<Empresa> = emptyList()
) {
    var mostrarFormulario by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mostrarFormulario) "Editar Perfil" else "Mi Perfil", color = GoldColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (mostrarFormulario) {
                            mostrarFormulario = false
                        } else {
                            alVolver()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = GoldColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPetroleo)
            )
        },
        containerColor = Celeste
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ContenidoPerfil(
                usuario = usuario,
                token = token,
                alVolver = alVolver,
                alGuardar = { u, _ -> alGuardar(u) },
                alCambiarFoto = alCambiarFoto,
                estadoUiPerfil = estadoUiPerfil,
                estadoUiEmpresa = estadoUiEmpresa,
                alRegistrarEmpresa = alRegistrarEmpresa,
                ciudades = ciudades,
                alCerrarSesion = alCerrarSesion,
                mostrarFormulario = mostrarFormulario,
                alAlternarFormulario = { mostrarFormulario = !mostrarFormulario },
                empresasUsuario = empresasUsuario
            )
        }
    }
}

@Composable
fun CampoTextoPerfil(etiqueta: String, valor: String, alCambiarValor: (String) -> Unit) {
    Column {
        Text(text = etiqueta, color = AzulPetroleo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiarValor,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AzulPetroleo,
                unfocusedTextColor = AzulPetroleo,
                focusedBorderColor = AzulPetroleo,
                unfocusedBorderColor = GrisClaro,
                cursorColor = AzulPetroleo,
                focusedLabelColor = AzulPetroleo,
                unfocusedLabelColor = GrisClaro
            ),
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaPerfil() {
    Codice路Theme {
        PantallaPerfil(
            usuario = Usuario(
                nombreUsuario = "jdoe",
                correoElectronico = "jdoe@example.com",
                nombre = "John",
                apellido = "Doe",
                esProtagonista = false,
                esTurista = true,
                telefono = "12345678"
            ),
            token = "fake_token",
            alVolver = {},
            alGuardar = {},
            estadoUiPerfil = EstadoUiPerfil.Inactivo,
            estadoUiEmpresa = EstadoUiEmpresa.Inactivo,
            alRegistrarEmpresa = { _, _ -> },
            ciudades = emptyList(),
            alCerrarSesion = {}
        )
    }
}
