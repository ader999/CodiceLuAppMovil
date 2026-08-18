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
import com.example.codise.data.City
import com.example.codise.data.EventRequest
import com.example.codise.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadEventScreen(
    cities: List<City>,
    onBack: () -> Unit,
    onUpload: (EventRequest) -> Unit,
    isUploading: Boolean,
    uploadSuccess: Boolean
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var selectedCityId by remember { mutableStateOf<Int?>(null) }
    var esGratuito by remember { mutableStateOf(true) }
    var precioEntrada by remember { mutableStateOf("0.00") }

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            onBack()
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
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = cities.find { it.id == selectedCityId }?.nombre ?: "Seleccionar Ciudad",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ciudad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    cities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city.nombre) },
                            onClick = {
                                selectedCityId = city.id
                                expanded = false
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
                    selectedCityId?.let { cityId ->
                        onUpload(
                            EventRequest(
                                titulo = titulo,
                                descripcion = descripcion,
                                ciudad = cityId,
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
                enabled = !isUploading && titulo.isNotEmpty() && selectedCityId != null
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("PUBLICAR EVENTO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
