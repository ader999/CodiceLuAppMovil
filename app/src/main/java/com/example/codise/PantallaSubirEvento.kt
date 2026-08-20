package com.example.codise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.codise.data.Ciudad
import com.example.codise.data.SolicitudEvento
import com.example.codise.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSubirEvento(
    ciudades: List<Ciudad>,
    alVolver: () -> Unit,
    alSubir: (SolicitudEvento) -> Unit,
    estaSubiendo: Boolean,
    subidaExitosa: Boolean
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var idCiudadSeleccionada by remember { mutableStateOf<Int?>(null) }
    var esGratuito by remember { mutableStateOf(true) }
    var precioEntrada by remember { mutableStateOf("0.00") }

    var menuCiudadExpandido by remember { mutableStateOf(false) }

    LaunchedEffect(subidaExitosa) {
        if (subidaExitosa) {
            alVolver()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del Evento") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulPetroleo,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = AzulPetroleo,
                    cursorColor = AzulPetroleo,
                    focusedTextColor = AzulPetroleo,
                    unfocusedTextColor = AzulPetroleo
                )
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulPetroleo,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = AzulPetroleo,
                    cursorColor = AzulPetroleo,
                    focusedTextColor = AzulPetroleo,
                    unfocusedTextColor = AzulPetroleo
                )
            )

            ExposedDropdownMenuBox(
                expanded = menuCiudadExpandido,
                onExpandedChange = { menuCiudadExpandido = !menuCiudadExpandido }
            ) {
                OutlinedTextField(
                    value = ciudades.find { it.id == idCiudadSeleccionada }?.nombre ?: "Seleccionar Ciudad",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ciudad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCiudadExpandido) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPetroleo,
                        unfocusedBorderColor = GrisClaro,
                        focusedLabelColor = AzulPetroleo,
                        focusedTextColor = AzulPetroleo,
                        unfocusedTextColor = AzulPetroleo
                    )
                )
                ExposedDropdownMenu(
                    expanded = menuCiudadExpandido,
                    onDismissRequest = { menuCiudadExpandido = false }
                ) {
                    ciudades.forEach { ciudad ->
                        DropdownMenuItem(
                            text = { Text(ciudad.nombre) },
                            onClick = {
                                idCiudadSeleccionada = ciudad.id
                                menuCiudadExpandido = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación Específica") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulPetroleo,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = AzulPetroleo,
                    cursorColor = AzulPetroleo,
                    focusedTextColor = AzulPetroleo,
                    unfocusedTextColor = AzulPetroleo
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fechaInicio,
                    onValueChange = { fechaInicio = it },
                    label = { Text("Inicio (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPetroleo,
                        unfocusedBorderColor = GrisClaro,
                        focusedLabelColor = AzulPetroleo,
                        cursorColor = AzulPetroleo
                    )
                )
                OutlinedTextField(
                    value = fechaFin,
                    onValueChange = { fechaFin = it },
                    label = { Text("Fin (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPetroleo,
                        unfocusedBorderColor = GrisClaro,
                        focusedLabelColor = AzulPetroleo,
                        cursorColor = AzulPetroleo
                    )
                )
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = esGratuito, 
                    onCheckedChange = { esGratuito = it },
                    colors = CheckboxDefaults.colors(checkedColor = AzulPetroleo)
                )
                Text("Es gratuito", color = AzulPetroleo, fontWeight = FontWeight.Medium)
            }

            if (!esGratuito) {
                OutlinedTextField(
                    value = precioEntrada,
                    onValueChange = { precioEntrada = it },
                    label = { Text("Precio de Entrada") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPetroleo,
                        unfocusedBorderColor = GrisClaro,
                        focusedLabelColor = AzulPetroleo,
                        cursorColor = AzulPetroleo
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    idCiudadSeleccionada?.let { idCiudad ->
                        alSubir(
                            SolicitudEvento(
                                titulo = titulo,
                                descripcion = descripcion,
                                ciudad = idCiudad,
                                fechaInicio = "${fechaInicio}T00:00:00Z",
                                fechaFin = "${fechaFin}T00:00:00Z",
                                ubicacion = ubicacion,
                                precioEntrada = precioEntrada,
                                esGratuito = esGratuito
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                enabled = !estaSubiendo && titulo.isNotEmpty() && idCiudadSeleccionada != null
            ) {
                if (estaSubiendo) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("PUBLICAR EVENTO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
