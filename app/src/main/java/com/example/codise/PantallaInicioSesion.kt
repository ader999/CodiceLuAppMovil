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
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
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

                if (modoRegistro) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text(cadenas.nombre) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text(cadenas.apellido) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = usuario,
                        onValueChange = { usuario = it },
                        label = { Text(cadenas.nombreUsuario) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AzulPetroleo) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text(cadenas.telefono) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AzulPetroleo) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text(cadenas.correoElectronico) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AzulPetroleo) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = coloresCamposTexto()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text(cadenas.contrasena) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AzulPetroleo) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = coloresCamposTexto()
                )

                if (modoRegistro) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = esProtagonista, onCheckedChange = { esProtagonista = it })
                        Text(cadenas.soyProtagonista, color = AzulPetroleo)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = esTurista, onCheckedChange = { esTurista = it })
                        Text(cadenas.soyTurista, color = AzulPetroleo)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (estadoUi is EstadoUiLogin.Cargando) {
                    CircularProgressIndicator(color = AzulPetroleo)
                } else {
                    Button(
                        onClick = {
                            if (modoRegistro) {
                                viewModel.registrar(
                                    Usuario(
                                        nombreUsuario = usuario,
                                        correoElectronico = correo,
                                        nombre = nombre,
                                        apellido = apellido,
                                        esProtagonista = esProtagonista,
                                        esTurista = esTurista,
                                        telefono = telefono,
                                        contrasena = contrasena,
                                        confirmarContrasena = contrasena
                                    )
                                )
                            } else {
                                viewModel.iniciarSesion(correo, contrasena)
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlancoBase
                        )
                    }
                }

                if (estadoUi is EstadoUiLogin.Error) {
                    Text(
                        text = (estadoUi as EstadoUiLogin.Error).mensaje,
                        color = androidx.compose.ui.graphics.Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
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
                    onClick = { viewModel.iniciarSesionConGoogle(contexto) },
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AzulPetroleo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { modoRegistro = !modoRegistro }) {
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
    cursorColor = AzulPetroleo
)

@Preview(showBackground = true)
@Composable
fun VistaPreviaLogin() {
    Codice路Theme {
        PantallaLogin()
    }
}
