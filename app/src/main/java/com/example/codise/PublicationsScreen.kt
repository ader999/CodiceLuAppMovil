package com.example.codise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codise.data.Publication
import com.example.codise.ui.theme.*
import com.example.codise.utils.toFullUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicationsScreen(
    viewModel: PublicationsViewModel,
    onUploadClick: () -> Unit
) {
    val uiState by viewModel.uiState
    var previewImages by remember { mutableStateOf<List<String>?>(null) }
    var initialPreviewPage by remember { mutableIntStateOf(0) }
    val isRefreshing = uiState is PublicationsUiState.Loading

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onUploadClick,
                containerColor = GoldColor,
                contentColor = AzulPetroleo
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Nueva Publicación")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.fetchPublications() },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is PublicationsUiState.Loading -> {
                        // Only show indicator if we don't have data yet
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldColor)
                    }
                    is PublicationsUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = (uiState as PublicationsUiState.Error).message, color = Color.Red)
                            Button(onClick = { viewModel.fetchPublications() }, colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo)) {
                                Text("Reintentar")
                            }
                        }
                    }
                    is PublicationsUiState.Success -> {
                        val publications = (uiState as PublicationsUiState.Success).publications
                        if (publications.isEmpty()) {
                            EmptyPublicationsState()
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(publications) { publication ->
                                    PublicationCard(
                                        publication = publication,
                                        onLikeClick = { viewModel.toggleLike(publication.id) },
                                        onImageClick = { images, page ->
                                            previewImages = images
                                            initialPreviewPage = page
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    if (previewImages != null) {
        ImagePreviewDialog(
            images = previewImages!!,
            initialPage = initialPreviewPage,
            onDismiss = { previewImages = null }
        )
    }
}

@Composable
fun EmptyPublicationsState() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = AzulPetroleo.copy(alpha = 0.3f),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Aún no hay publicaciones",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AzulPetroleo
        )
        Text(
            text = "¡Sé el primero en compartir tu experiencia!",
            fontSize = 14.sp,
            color = GrisClaro
        )
    }
}

@Composable
fun PublicationCard(
    publication: Publication,
    onLikeClick: () -> Unit,
    onImageClick: (List<String>, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (publication.autorFotoPerfil != null) {
                    AsyncImage(
                        model = publication.autorFotoPerfil.toFullUrl(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GrisClaro.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.graphics.painter.ColorPainter(AzulPetroleo.copy(alpha = 0.2f))
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        null,
                        modifier = Modifier.size(40.dp),
                        tint = AzulPetroleo.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = publication.autorUsername,
                        fontWeight = FontWeight.Bold,
                        color = AzulPetroleo,
                        fontSize = 15.sp
                    )
                    if (publication.ciudadNombre != null) {
                        Text(
                            text = publication.ciudadNombre,
                            fontSize = 12.sp,
                            color = GrisClaro
                        )
                    }
                }
                if (publication.eventoTitulo != null) {
                    Surface(
                        color = GoldColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Event, null, tint = GoldColor, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(publication.eventoTitulo, fontSize = 10.sp, color = GoldColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Images Carousel
            val allImages = remember(publication) {
                val list = mutableListOf<String>()
                publication.imagenPrincipal?.let { list.add(it.toFullUrl()) }
                publication.imagenes.forEach { list.add(it.imagen.toFullUrl()) }
                list
            }

            if (allImages.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { allImages.size })
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(
                            model = allImages[page],
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onImageClick(allImages, page) },
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    if (allImages.size > 1) {
                        Row(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(allImages.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Actions
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (publication.userHaDadoLike) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Me gusta",
                        tint = if (publication.userHaDadoLike) Color.Red else AzulPetroleo
                    )
                }
                Text(
                    text = "${publication.totalLikes}",
                    fontSize = 14.sp,
                    color = AzulPetroleo,
                    fontWeight = FontWeight.Medium
                )
            }

            // Description
            if (publication.descripcion.isNotEmpty()) {
                Text(
                    text = publication.descripcion,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    fontSize = 14.sp,
                    color = NegroPuro.copy(alpha = 0.8f)
                )
            }
        }
    }
}
