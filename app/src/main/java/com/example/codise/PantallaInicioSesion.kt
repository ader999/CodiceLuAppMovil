package com.example.codise

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codise.data.Usuario
import com.example.codise.ui.theme.*

@Composable
fun PantallaLogin(viewModel: ViewModelLogin = viewModel()) {
    var modoRegistro by remember { mutableStateOf(false) }
    val estadoUi by viewModel.estadoUi.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Celeste)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
                    text = if (modoRegistro) "Crear Cuenta" else "Iniciar Sesión",
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
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text("Apellido") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = usuario,
                        onValueChange = { usuario = it },
                        label = { Text("Usuario") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AzulPetroleo) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresCamposTexto()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
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
                    label = { Text("Usuario o Correo") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AzulPetroleo) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = coloresCamposTexto()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña") },
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
                        Text("Es Protagonista", color = AzulPetroleo)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = esTurista, onCheckedChange = { esTurista = it })
                        Text("Es Turista", color = AzulPetroleo)
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
                            text = if (modoRegistro) "REGISTRARSE" else "ENTRAR",
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

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { modoRegistro = !modoRegistro }) {
                    Text(
                        text = if (modoRegistro) "¿Ya tienes cuenta? Inicia Sesión" else "¿No tienes cuenta? Regístrate",
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
