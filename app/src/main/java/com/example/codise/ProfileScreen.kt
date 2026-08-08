package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codise.data.Business
import com.example.codise.data.City
import com.example.codise.data.User
import com.example.codise.ui.theme.*

@Composable
fun ProfileContent(
    user: User,
    token: String,
    onBack: () -> Unit,
    onSave: (User) -> Unit,
    profileUiState: ProfileUiState,
    businessUiState: BusinessUiState,
    onRegisterBusiness: (String, Business) -> Unit,
    cities: List<City>,
    onLogout: () -> Unit
) {
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var username by remember { mutableStateOf(user.username) }
    var email by remember { mutableStateOf(user.email) }
    var telefono by remember { mutableStateOf(user.telefono) }

    var showBusinessForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Icon / Image Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(AzulPetroleo)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = GoldColor,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                ProfileTextField(label = "Nombre", value = firstName, onValueChange = { firstName = it })
                ProfileTextField(label = "Apellido", value = lastName, onValueChange = { lastName = it })
                ProfileTextField(label = "Usuario", value = username, onValueChange = { username = it })
                ProfileTextField(label = "Correo Electrónico", value = email, onValueChange = { email = it })
                ProfileTextField(label = "Teléfono", value = telefono, onValueChange = { telefono = it })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (profileUiState is ProfileUiState.Error) {
            Text(
                text = profileUiState.message,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                val updatedUser = user.copy(
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    email = email,
                    telefono = telefono
                )
                onSave(updatedUser)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
            shape = RoundedCornerShape(12.dp),
            enabled = profileUiState !is ProfileUiState.Loading
        ) {
            if (profileUiState is ProfileUiState.Loading) {
                CircularProgressIndicator(color = GoldColor, modifier = Modifier.size(24.dp))
            } else {
                Text("Guardar Cambios", color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar Sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (!user.esProtagonista && !showBusinessForm) {
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = GrisClaro.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))
            
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
                onClick = { showBusinessForm = true },
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

        if (showBusinessForm) {
            BusinessRegistrationForm(
                token = token,
                cities = cities,
                businessUiState = businessUiState,
                onRegister = onRegisterBusiness,
                onCancel = { showBusinessForm = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessRegistrationForm(
    token: String,
    cities: List<City>,
    businessUiState: BusinessUiState,
    onRegister: (String, Business) -> Unit,
    onCancel: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf<City?>(null) }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var sitioWeb by remember { mutableStateOf("") }
    var aceptaInversiones by remember { mutableStateOf(false) }

    var expandedCities by remember { mutableStateOf(false) }

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
            ProfileTextField(label = "Nombre de la Empresa", value = nombre, onValueChange = { nombre = it })
            ProfileTextField(label = "Descripción", value = descripcion, onValueChange = { descripcion = it })
            ProfileTextField(label = "Categoría (Ej: Taller, Restaurante)", value = categoria, onValueChange = { categoria = it })
            
            // City Selector
            Column {
                Text(text = "Ciudad", color = AzulPetroleo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedCities,
                    onExpandedChange = { expandedCities = !expandedCities }
                ) {
                    OutlinedTextField(
                        value = selectedCity?.nombre ?: "Seleccionar ciudad",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCities) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AzulPetroleo,
                            unfocusedTextColor = AzulPetroleo,
                            focusedBorderColor = AzulPetroleo,
                            unfocusedBorderColor = GrisClaro
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCities,
                        onDismissRequest = { expandedCities = false }
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city.nombre) },
                                onClick = {
                                    selectedCity = city
                                    expandedCities = false
                                }
                            )
                        }
                    }
                }
            }

            ProfileTextField(label = "Dirección", value = direccion, onValueChange = { direccion = it })
            ProfileTextField(label = "Teléfono de Contacto", value = telefono, onValueChange = { telefono = it })
            ProfileTextField(label = "Correo de Contacto", value = email, onValueChange = { email = it })
            ProfileTextField(label = "Sitio Web (Opcional)", value = sitioWeb, onValueChange = { sitioWeb = it })

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

    if (businessUiState is BusinessUiState.Error) {
        Text(
            text = businessUiState.message,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    if (businessUiState is BusinessUiState.Success) {
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
            onClick = onCancel,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulPetroleo)
        ) {
            Text("Cancelar")
        }

        Button(
            onClick = {
                selectedCity?.let { city ->
                    val business = Business(
                        nombre = nombre,
                        descripcion = descripcion,
                        categoria = categoria,
                        ciudad = city.id,
                        direccion = direccion,
                        telefonoContacto = telefono,
                        emailContacto = email,
                        sitioWeb = sitioWeb.takeIf { it.isNotBlank() },
                        latitud = city.latitudCentro, // Default to city center
                        longitud = city.longitudCentro,
                        aceptaInversiones = aceptaInversiones
                    )
                    onRegister(token, business)
                }
            },
            modifier = Modifier.weight(1f).height(56.dp),
            enabled = businessUiState !is BusinessUiState.Loading && selectedCity != null && nombre.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo)
        ) {
            if (businessUiState is BusinessUiState.Loading) {
                CircularProgressIndicator(color = GoldColor, modifier = Modifier.size(24.dp))
            } else {
                Text("Registrar", color = GoldColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    token: String,
    onBack: () -> Unit,
    onSave: (User) -> Unit,
    profileUiState: ProfileUiState,
    businessUiState: BusinessUiState,
    onRegisterBusiness: (String, Business) -> Unit,
    cities: List<City>,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = GoldColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = GoldColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPetroleo)
            )
        },
        containerColor = Celeste
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ProfileContent(
                user = user,
                token = token,
                onBack = onBack,
                onSave = onSave,
                profileUiState = profileUiState,
                businessUiState = businessUiState,
                onRegisterBusiness = onRegisterBusiness,
                cities = cities,
                onLogout = onLogout
            )
        }
    }
}

@Composable
fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(text = label, color = AzulPetroleo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
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
fun ProfileScreenPreview() {
    Codise路Theme {
        ProfileScreen(
            user = User(
                username = "jdoe",
                email = "jdoe@example.com",
                firstName = "John",
                lastName = "Doe",
                esProtagonista = false,
                esTurista = true,
                telefono = "12345678"
            ),
            token = "fake_token",
            onBack = {},
            onSave = {},
            profileUiState = ProfileUiState.Idle,
            businessUiState = BusinessUiState.Idle,
            onRegisterBusiness = { _, _ -> },
            cities = emptyList(),
            onLogout = {}
        )
    }
}
