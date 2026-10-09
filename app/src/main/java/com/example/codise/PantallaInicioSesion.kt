package com.example.codise

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codise.data.GestorIdioma
import com.example.codise.data.IdiomaApp
import com.example.codise.data.ServicioApi
import com.example.codise.data.Usuario
import com.example.codise.ui.theme.*
import com.example.codise.utils.LocalCadenas

@Composable
fun PantallaLogin(
    viewModel: ViewModelLogin = viewModel(),
    gestorIdioma: GestorIdioma? = null,
    idiomaActual: IdiomaApp = IdiomaApp.ESPANOL
) {
    val contexto = LocalContext.current
    val cadenas = LocalCadenas.current
    var modoRegistro by remember { mutableStateOf(false) }
    val estadoUi by viewModel.estadoUi.collectAsState()
    var mostrarDialogoIdioma by remember { mutableStateOf(false) }

    if (mostrarDialogoIdioma && gestorIdioma != null) {
        DialogoSeleccionIdioma(
            idiomaActual = idiomaActual,
            alSeleccionarIdioma = { nuevoIdioma ->
                gestorIdioma.cambiarIdioma(nuevoIdioma)
                ServicioApi.limpiarCache()
            },
            alCerrar = { mostrarDialogoIdioma = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Celeste)
            .safeDrawingPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (gestorIdioma != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                BotonSelectorIdioma(
                    idiomaActual = idiomaActual,
                    alHacerClic = { mostrarDialogoIdioma = true },
                    colorFondo = BlancoBase.copy(alpha = 0.9f),
                    colorTexto = AzulPetroleo
                )
            }
        }
        // Sección de Logo
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Codice Logo",
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(0.7f),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Tarjeta de Login / Registro
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BlancoBase),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (modoRegistro) cadenas.crearCuenta else cadenas.iniciarSesion,
                    style = TitularPrincipal,
                    color = AzulPetroleo
                )

                Spacer(modifier = Modifier.height(24.dp))

                var usuario by remember { mutableStateOf("") }
                var correo by remember { mutableStateOf("") }
                var contrasena by remember { mutableStateOf("") }
                var nombre by remember { mutableStateOf("") }
                var apellido by remember { mutableStateOf("") }
                var telefono by remember { mutableStateOf("") }
                var esProtagonista by remember { mutableStateOf(false) }
                var esTurista by remember { mutableStateOf(false) }
                var mostrarContrasena by remember { mutableStateOf(false) }

                var errorUsuario by remember { mutableStateOf<String?>(null) }
                var errorCorreo by remember { mutableStateOf<String?>(null) }
                var errorContrasena by remember { mutableStateOf<String?>(null) }
                var errorNombre by remember { mutableStateOf<String?>(null) }
                var errorApellido by remember { mutableStateOf<String?>(null) }
                var errorTelefono by remember { mutableStateOf<String?>(null) }
                var errorRol by remember { mutableStateOf<String?>(null) }

                fun limpiarErroresLocales() {
                    errorUsuario = null
                    errorCorreo = null
                    errorContrasena = null
                    errorNombre = null
                    errorApellido = null
                    errorTelefono = null
                    errorRol = null
                    viewModel.limpiarError()
                }

                if (modoRegistro) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            errorNombre = null
                            viewModel.limpiarError()
                        },
                        label = { Text(cadenas.nombre) },
                        isError = errorNombre != null,
                        supportingText = errorNombre?.let { msg -> { Text(text = msg) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = {
                            apellido = it
                            errorApellido = null
                            viewModel.limpiarError()
                        },
                        label = { Text(cadenas.apellido) },
                        isError = errorApellido != null,
                        supportingText = errorApellido?.let { msg -> { Text(text = msg) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = usuario,
                        onValueChange = {
                            usuario = it
                            errorUsuario = null
                            viewModel.limpiarError()
                        },
                        label = { Text(cadenas.nombreUsuario) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AzulPetroleo) },
                        isError = errorUsuario != null,
                        supportingText = errorUsuario?.let { msg -> { Text(text = msg) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = {
                            telefono = it
                            errorTelefono = null
                            viewModel.limpiarError()
                        },
                        label = { Text(cadenas.telefono) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AzulPetroleo) },
                        isError = errorTelefono != null,
                        supportingText = errorTelefono?.let { msg -> { Text(text = msg) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        errorCorreo = null
                        viewModel.limpiarError()
                    },
                    label = { Text(if (modoRegistro) cadenas.correoElectronico else cadenas.correoOusuario) },
                    leadingIcon = {
                        Icon(
                            if (modoRegistro) Icons.Default.Email else Icons.Default.Person,
                            contentDescription = null,
                            tint = AzulPetroleo
                        )
                    },
                    isError = errorCorreo != null,
                    supportingText = errorCorreo?.let { msg -> { Text(text = msg) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = coloresCamposTexto(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = {
                        contrasena = it
                        errorContrasena = null
                        viewModel.limpiarError()
                    },
                    label = { Text(cadenas.contrasena) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AzulPetroleo) },
                    trailingIcon = {
                        val icono = if (mostrarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        val desc = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                        IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                            Icon(imageVector = icono, contentDescription = desc, tint = AzulPetroleo)
                        }
                    },
                    visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = errorContrasena != null,
                    supportingText = errorContrasena?.let { msg -> { Text(text = msg) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = coloresCamposTexto(),
                    singleLine = true
                )

                if (modoRegistro) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = esProtagonista,
                            onCheckedChange = {
                                esProtagonista = it
                                errorRol = null
                                viewModel.limpiarError()
                            }
                        )
                        Text(cadenas.soyProtagonista, color = AzulPetroleo)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = esTurista,
                            onCheckedChange = {
                                esTurista = it
                                errorRol = null
                                viewModel.limpiarError()
                            }
                        )
                        Text(cadenas.soyTurista, color = AzulPetroleo)
                    }

                    if (errorRol != null) {
                        Text(
                            text = errorRol!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (estadoUi is EstadoUiLogin.Cargando) {
                    CircularProgressIndicator(color = AzulPetroleo)
                } else {
                    Button(
                        onClick = {
                            if (modoRegistro) {
                                var hayErrores = false
                                if (nombre.isBlank()) {
                                    errorNombre = cadenas.errorCampoRequerido
                                    hayErrores = true
                                }
                                if (apellido.isBlank()) {
                                    errorApellido = cadenas.errorCampoRequerido
                                    hayErrores = true
                                }
                                if (usuario.isBlank()) {
                                    errorUsuario = cadenas.errorCampoRequerido
                                    hayErrores = true
                                } else if (usuario.trim().length < 3) {
                                    errorUsuario = "Mínimo 3 caracteres"
                                    hayErrores = true
                                } else if (usuario.contains(" ")) {
                                    errorUsuario = "No debe contener espacios"
                                    hayErrores = true
                                }
                                if (correo.isBlank()) {
                                    errorCorreo = cadenas.errorCampoRequerido
                                    hayErrores = true
                                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
                                    errorCorreo = cadenas.errorCorreoInvalido
                                    hayErrores = true
                                }
                                if (telefono.isBlank()) {
                                    errorTelefono = cadenas.errorCampoRequerido
                                    hayErrores = true
                                }
                                if (contrasena.isBlank()) {
                                    errorContrasena = cadenas.errorCampoRequerido
                                    hayErrores = true
                                } else if (contrasena.length < 6) {
                                    errorContrasena = cadenas.errorContrasenaCorta
                                    hayErrores = true
                                }
                                if (!esProtagonista && !esTurista) {
                                    errorRol = cadenas.errorSeleccionarRol
                                    hayErrores = true
                                }

                                if (!hayErrores) {
                                    viewModel.registrar(
                                        Usuario(
                                            nombreUsuario = usuario.trim(),
                                            correoElectronico = correo.trim(),
                                            nombre = nombre.trim(),
                                            apellido = apellido.trim(),
                                            esProtagonista = esProtagonista,
                                            esTurista = esTurista,
                                            telefono = telefono.trim(),
                                            contrasena = contrasena,
                                            confirmarContrasena = contrasena
                                        )
                                    )
                                }
                            } else {
                                var hayErrores = false
                                if (correo.isBlank()) {
                                    errorCorreo = cadenas.errorCampoRequerido
                                    hayErrores = true
                                }
                                if (contrasena.isBlank()) {
                                    errorContrasena = cadenas.errorCampoRequerido
                                    hayErrores = true
                                }
                                if (!hayErrores) {
                                    viewModel.iniciarSesion(correo.trim(), contrasena)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (modoRegistro) cadenas.crearCuenta.uppercase() else cadenas.iniciarSesion.uppercase(),
                            style = TextoBoton,
                            color = BlancoBase
                        )
                    }
                }

                if (estadoUi is EstadoUiLogin.Error) {
                    val mensajeError = (estadoUi as EstadoUiLogin.Error).mensaje
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = mensajeError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = GrisClaro.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "  o  ",
                        color = AzulPetroleo.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = GrisClaro.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        limpiarErroresLocales()
                        viewModel.iniciarSesionConGoogle(contexto)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GrisClaro),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = BlancoBase),
                    enabled = estadoUi !is EstadoUiLogin.Cargando
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = cadenas.continuarConGoogle,
                            style = TextoBoton,
                            color = AzulPetroleo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    modoRegistro = !modoRegistro
                    limpiarErroresLocales()
                }) {
                    Text(
                        text = if (modoRegistro) cadenas.yaTienesCuenta else cadenas.noTienesCuenta,
                        color = AzulPetroleo
                    )
                }
            }
        }
    }
}

@Composable
fun coloresCamposTexto() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AzulPetroleo,
    unfocusedTextColor = AzulPetroleo,
    focusedBorderColor = AzulPetroleo,
    unfocusedBorderColor = GrisClaro,
    focusedLabelColor = AzulPetroleo,
    unfocusedLabelColor = GrisClaro,
    cursorColor = AzulPetroleo,
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorLabelColor = MaterialTheme.colorScheme.error,
    errorLeadingIconColor = MaterialTheme.colorScheme.error,
    errorTrailingIconColor = MaterialTheme.colorScheme.error,
    errorSupportingTextColor = MaterialTheme.colorScheme.error
)

@Preview(showBackground = true)
@Composable
fun VistaPreviaLogin() {
    Codice路Theme {
        PantallaLogin()
    }
}
