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
import com.example.codise.data.Empresa
import com.example.codise.data.Ciudad
import com.example.codise.data.Usuario
import com.example.codise.ui.theme.*

@Composable
fun ContenidoPerfil(
    usuario: Usuario,
    token: String,
    alVolver: () -> Unit,
    alGuardar: (Usuario) -> Unit,
    estadoUiPerfil: EstadoUiPerfil,
    estadoUiEmpresa: EstadoUiEmpresa,
    alRegistrarEmpresa: (String, Empresa) -> Unit,
    ciudades: List<Ciudad>,
    alCerrarSesion: () -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var apellido by remember { mutableStateOf(usuario.apellido) }
    var nombreUsuario by remember { mutableStateOf(usuario.nombreUsuario) }
    var correo by remember { mutableStateOf(usuario.correoElectronico) }
    var telefono by remember { mutableStateOf(usuario.telefono) }

    var mostrarFormularioEmpresa by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icono de perfil / Marcador de posición de imagen
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
                CampoTextoPerfil(etiqueta = "Nombre", valor = nombre, alCambiarValor = { nombre = it })
                CampoTextoPerfil(etiqueta = "Apellido", valor = apellido, alCambiarValor = { apellido = it })
                CampoTextoPerfil(etiqueta = "Usuario", valor = nombreUsuario, alCambiarValor = { nombreUsuario = it })
                CampoTextoPerfil(etiqueta = "Correo Electrónico", valor = correo, alCambiarValor = { correo = it })
                CampoTextoPerfil(etiqueta = "Teléfono", valor = telefono, alCambiarValor = { telefono = it })
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
                text = "¡Perfil actualizado con éxito!",
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
                alGuardar(usuarioActualizado)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
            shape = RoundedCornerShape(12.dp),
            enabled = estadoUiPerfil !is EstadoUiPerfil.Cargando
        ) {
            if (estadoUiPerfil is EstadoUiPerfil.Cargando) {
                CircularProgressIndicator(color = GoldColor, modifier = Modifier.size(24.dp))
            } else {
                Text("Guardar Cambios", color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = alCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar Sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (!usuario.esProtagonista && !mostrarFormularioEmpresa) {
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
    var categoria by remember { mutableStateOf("") }
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
            CampoTextoPerfil(etiqueta = "Categoría (Ej: Taller, Restaurante)", valor = categoria, alCambiarValor = { categoria = it })
            
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
                ciudadSeleccionada?.let { ciudad ->
                    val empresa = Empresa(
                        nombre = nombre,
                        descripcion = descripcion,
                        categoria = categoria,
                        ciudad = ciudad.id,
                        direccion = direccion,
                        telefonoContacto = telefono,
                        emailContacto = email,
                        sitioWeb = sitioWeb.takeIf { it.isNotBlank() },
                        latitud = ciudad.latitudCentro,
                        longitud = ciudad.longitudCentro,
                        aceptaInversiones = aceptaInversiones
                    )
                    alRegistrar(token, empresa)
                }
            },
            modifier = Modifier.weight(1f).height(56.dp),
            enabled = estadoUiEmpresa !is EstadoUiEmpresa.Cargando && ciudadSeleccionada != null && nombre.isNotBlank(),
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
    estadoUiPerfil: EstadoUiPerfil,
    estadoUiEmpresa: EstadoUiEmpresa,
    alRegistrarEmpresa: (String, Empresa) -> Unit,
    ciudades: List<Ciudad>,
    alCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = GoldColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
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
                alGuardar = alGuardar,
                estadoUiPerfil = estadoUiPerfil,
                estadoUiEmpresa = estadoUiEmpresa,
                alRegistrarEmpresa = alRegistrarEmpresa,
                ciudades = ciudades,
                alCerrarSesion = alCerrarSesion
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
