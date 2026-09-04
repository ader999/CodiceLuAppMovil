package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codise.data.IdiomaApp
import com.example.codise.ui.theme.AzulPetroleo
import com.example.codise.ui.theme.BlancoBase
import com.example.codise.ui.theme.GoldColor
import com.example.codise.ui.theme.NegroPuro
import com.example.codise.utils.LocalCadenas

@Composable
fun DialogoSeleccionIdioma(
    idiomaActual: IdiomaApp,
    alSeleccionarIdioma: (IdiomaApp) -> Unit,
    alCerrar: () -> Unit
) {
    val cadenas = LocalCadenas.current

    AlertDialog(
        onDismissRequest = alCerrar,
        icon = {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = GoldColor,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = cadenas.seleccionarIdioma,
                fontWeight = FontWeight.Bold,
                color = AzulPetroleo,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IdiomaApp.entries.forEach { idioma ->
                    val esSeleccionado = idioma == idiomaActual
                    val bordeColor = if (esSeleccionado) GoldColor else Color.LightGray.copy(alpha = 0.5f)
                    val fondoColor = if (esSeleccionado) GoldColor.copy(alpha = 0.12f) else BlancoBase

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(fondoColor)
                            .border(
                                width = if (esSeleccionado) 2.dp else 1.dp,
                                color = bordeColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                alSeleccionarIdioma(idioma)
                                alCerrar()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = idioma.bandera,
                                fontSize = 24.sp
                            )
                            Column {
                                Text(
                                    text = idioma.etiquetaNativa,
                                    fontSize = 16.sp,
                                    fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Medium,
                                    color = if (esSeleccionado) AzulPetroleo else NegroPuro
                                )
                                if (idioma != IdiomaApp.ESPANOL) {
                                    Text(
                                        text = idioma.etiqueta,
                                        fontSize = 12.sp,
                                        color = NegroPuro.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        if (esSeleccionado) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GoldColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = alCerrar) {
                Text(
                    text = cadenas.cerrar,
                    color = AzulPetroleo,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = BlancoBase,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun BotonSelectorIdioma(
    idiomaActual: IdiomaApp,
    alHacerClic: () -> Unit,
    modifier: Modifier = Modifier,
    colorFondo: Color = GoldColor.copy(alpha = 0.2f),
    colorTexto: Color = GoldColor
) {
    Surface(
        onClick = alHacerClic,
        shape = RoundedCornerShape(16.dp),
        color = colorFondo,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = idiomaActual.bandera,
                fontSize = 14.sp
            )
            Text(
                text = idiomaActual.codigo.uppercase(),
                color = colorTexto,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

