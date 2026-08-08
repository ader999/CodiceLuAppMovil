package com.example.codise

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.codise.data.GalleryItem
import com.example.codise.utils.extractYoutubeVideoId
import com.example.codise.utils.getYoutubeThumbnailUrl
import com.example.codise.utils.toFullUrl

@Composable
fun GalleryCarousel(
    gallery: List<GalleryItem>,
    onItemClick: ((GalleryItem) -> Unit)? = null
) {
    if (gallery.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { gallery.size })
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = gallery[page]
            val displayImage = item.imagen?.toFullUrl()
                ?: if (item.tipo == "Video" && item.videoUrl != null) {
                    extractYoutubeVideoId(item.videoUrl)?.let { getYoutubeThumbnailUrl(it) }
                } else {
                    null
                }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (onItemClick != null) {
                            onItemClick(item)
                        } else if (item.tipo == "Video" && item.videoUrl != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.videoUrl))
                            context.startActivity(intent)
                        }
                    }
            ) {
                if (displayImage != null) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = item.titulo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder if no image/thumbnail
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                if (item.tipo == "Video") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }

        // Indicators
        if (gallery.size > 1) {
            Row(
                Modifier
                    .height(32.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(gallery.size) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) Color.White else Color.White.copy(
                            alpha = 0.5f
                        )
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
