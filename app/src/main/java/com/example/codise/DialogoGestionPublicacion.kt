package com.example.codise

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Publicacion
import com.example.codise.ui.theme.*
import com.example.codise.utils.LocalCadenas
import com.example.codise.utils.aUrlCompleta

@Composable
fun DialogoEditarPublicacion(
    publicacion: Publicacion,
    alCerrar: () -> Unit,
    alConfirmar: (nuevaDescripcion: String, nuevasImagenes: List<Uri>?) -> Unit,
    estaGuardando: Boolean = false
) {
    val contexto = LocalContext.current
    val cadenas = LocalCadenas.current

    var descripcion by remember(publicacion.id) { mutableStateOf(publicacion.descripcion) }
    var urisNuevasImagenes by remember(publicacion.id) { mutableStateOf<List<Uri>>(emptyList()) }

    val imagenesActuales = remember(publicacion) {
        val lista = mutableListOf<String>()
        publicacion.imagenPrincipal?.let { lista.add(it.aUrlCompleta()) }
        publicacion.imagenes.forEach { lista.add(it.imagen.aUrlCompleta()) }
        lista
    }

    val lanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.size > 10) {
            Toast.makeText(contexto, "Máximo 10 imágenes permitidas", Toast.LENGTH_SHORT).show()
            urisNuevasImagenes = uris.take(10)
        } else {
            urisNuevasImagenes = uris
        }
    }

    AlertDialog(
        onDismissRequest = { if (!estaGuardando) alCerrar() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = AzulPetroleo,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cadenas.actualizarPublicacion,
                    style = TitularPrincipal.copy(fontSize = 18.sp),
                    color = AzulPetroleo
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // SECCIÓN DE IMÁGENES
                if (urisNuevasImagenes.isNotEmpty()) {
                    // Mostrar preview de las nuevas imágenes seleccionadas
                    val estadoPaginador = rememberPagerState(pageCount = { urisNuevasImagenes.size })
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.05f))
                    ) {
                        HorizontalPager(state = estadoPaginador, modifier = Modifier.fillMaxSize()) { pagina ->
                            AsyncImage(
                                model = urisNuevasImagenes[pagina],
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Badge "Nueva imagen"
                        Surface(
                            color = GoldColor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (urisNuevasImagenes.size > 1) "${urisNuevasImagenes.size} nuevas fotos" else "Nueva foto",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzulPetroleo,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Botón para cancelar el cambio de imagen
                        IconButton(
                            onClick = { urisNuevasImagenes = emptyList() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(30.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        // Botón para volver a cambiar
                        Button(
                            onClick = { lanzador.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("Cambiar", fontSize = 11.sp, color = Color.White)
                        }

                        // Indicadores paginador
                        if (urisNuevasImagenes.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(urisNuevasImagenes.size) { iteracion ->
                                    val color = if (estadoPaginador.currentPage == iteracion) Color.White else Color.White.copy(alpha = 0.5f)
                                    Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(5.dp))
                                }
                            }
                        }
                    }
                } else if (imagenesActuales.isNotEmpty()) {
                    // Mostrar preview de las imágenes actuales con botón para reemplazarlas
                    val estadoPaginador = rememberPagerState(pageCount = { imagenesActuales.size })
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.05f))
                    ) {
                        HorizontalPager(state = estadoPaginador, modifier = Modifier.fillMaxSize()) { pagina ->
                            AsyncImage(
                                model = imagenesActuales[pagina],
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Botón flotante para cambiar imagen
                        Button(
                            onClick = { lanzador.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo.copy(alpha = 0.88f)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(15.dp), tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text("Cambiar foto", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        // Indicadores paginador
                        if (imagenesActuales.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(imagenesActuales.size) { iteracion ->
                                    val color = if (estadoPaginador.currentPage == iteracion) Color.White else Color.White.copy(alpha = 0.5f)
                                    Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(5.dp))
                                }
                            }
                        }
                    }
                } else {
                    // La publicación original no tenía imagen, permitir agregar una
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AzulPetroleo.copy(alpha = 0.06f))
                            .clickable { lanzador.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = AzulPetroleo, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar imagen", color = AzulPetroleo, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }

                // CAMPO DE DESCRIPCIÓN
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text(cadenas.descripcion) },
                    placeholder = { Text("Escribe una descripción...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 200.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPetroleo,
                        unfocusedBorderColor = GrisClaro.copy(alpha = 0.5f),
                        focusedLabelColor = AzulPetroleo
                    ),
                    maxLines = 8,
                    enabled = !estaGuardando
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { alConfirmar(descripcion.trim(), urisNuevasImagenes.ifEmpty { null }) },
                enabled = !estaGuardando && (descripcion.isNotBlank() || urisNuevasImagenes.isNotEmpty()),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (estaGuardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(cadenas.guardar, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = alCerrar,
                enabled = !estaGuardando
            ) {
                Text(cadenas.cancelar, color = GrisClaro, fontWeight = FontWeight.Medium)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DialogoConfirmarEliminarPublicacion(
    publicacion: Publicacion,
    alCerrar: () -> Unit,
    alConfirmar: () -> Unit,
    estaEliminando: Boolean = false
) {
    val cadenas = LocalCadenas.current

    AlertDialog(
        onDismissRequest = { if (!estaEliminando) alCerrar() },
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFEBEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = cadenas.eliminarPublicacion,
                style = TitularPrincipal.copy(fontSize = 18.sp),
                color = NegroPuro
            )
        },
        text = {
            Text(
                text = cadenas.confirmarEliminarPublicacion,
                fontSize = 14.sp,
                color = NegroPuro.copy(alpha = 0.75f),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = alConfirmar,
                enabled = !estaEliminando,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (estaEliminando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(cadenas.eliminar, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = alCerrar,
                enabled = !estaEliminando
            ) {
                Text(cadenas.cancelar, color = GrisClaro, fontWeight = FontWeight.Medium)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
