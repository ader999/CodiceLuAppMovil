package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codise.data.User
import com.example.codise.ui.theme.*

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel()) {
    var isRegisterMode by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Celeste)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo Section
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(GenericShape { size, _ ->
                        moveTo(size.width * 0.2f, size.height)
                        lineTo(size.width, size.height * 0.1f)
                        lineTo(0f, size.height * 0.5f)
                        close()
                    })
                    .background(GoldColor)
            )
            Text(
                text = "Codice路",
                color = AzulPetroleo,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Login/Register Card
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
                    text = if (isRegisterMode) "Crear Cuenta" else "Iniciar Sesión",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulPetroleo
                )

                Spacer(modifier = Modifier.height(24.dp))

                var username by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var firstName by remember { mutableStateOf("") }
                var lastName by remember { mutableStateOf("") }
                var telefono by remember { mutableStateOf("") }
                var esProtagonista by remember { mutableStateOf(false) }
                var esTurista by remember { mutableStateOf(false) }

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Apellido") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuario") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AzulPetroleo) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AzulPetroleo) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Usuario o Correo") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AzulPetroleo) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AzulPetroleo) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )

                if (isRegisterMode) {
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

                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(color = AzulPetroleo)
                } else {
                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                viewModel.register(
                                    User(
                                        username = username,
                                        email = email,
                                        firstName = firstName,
                                        lastName = lastName,
                                        esProtagonista = esProtagonista,
                                        esTurista = esTurista,
                                        telefono = telefono,
                                        password = password,
                                        passwordConfirm = password // Assuming confirmation is same as password for now
                                    )
                                )
                            } else {
                                viewModel.login(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isRegisterMode) "REGISTRARSE" else "ENTRAR",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlancoBase
                        )
                    }
                }

                if (uiState is LoginUiState.Error) {
                    Text(
                        text = (uiState as LoginUiState.Error).message,
                        color = androidx.compose.ui.graphics.Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                    Text(
                        text = if (isRegisterMode) "¿Ya tienes cuenta? Inicia Sesión" else "¿No tienes cuenta? Regístrate",
                        color = AzulPetroleo
                    )
                }
            }
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
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
fun LoginPreview() {
    Codise路Theme {
        LoginScreen()
    }
}
