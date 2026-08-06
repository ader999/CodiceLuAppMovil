package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codise.data.User
import com.example.codise.ui.theme.*

@Composable
fun ProfileContent(
    user: User,
    onBack: () -> Unit,
    onSave: (User) -> Unit,
    profileUiState: ProfileUiState,
    onLogout: () -> Unit
) {
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var username by remember { mutableStateOf(user.username) }
    var email by remember { mutableStateOf(user.email) }
    var telefono by remember { mutableStateOf(user.telefono) }

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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onBack: () -> Unit,
    onSave: (User) -> Unit,
    profileUiState: ProfileUiState,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = GoldColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = GoldColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPetroleo)
            )
        },
        containerColor = Celeste
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ProfileContent(user, onBack, onSave, profileUiState, onLogout)
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
            onBack = {},
            onSave = {},
            profileUiState = ProfileUiState.Idle,
            onLogout = {}
        )
    }
}
