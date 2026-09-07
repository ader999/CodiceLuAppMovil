package com.example.codise

import android.net.Uri
import android.view.View
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R
import com.example.codise.data.ItemGaleria
import com.example.codise.utils.extraerIdVideoYoutube
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun ReproductorYouTube(
    idVideo: String,
    modifier: Modifier = Modifier
) {
    val propietarioCicloVida = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            YouTubePlayerView(ctx).apply {
                propietarioCicloVida.lifecycle.addObserver(this)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(idVideo, 0f)
                    }
                })
            }
        }
    )
}

@OptIn(UnstableApi::class)
@Composable
fun ReproductorVideoExo(
    urlVideo: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT
) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember(urlVideo) {
        ExoPlayer.Builder(contexto).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(urlVideo))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = autoPlay
        }
    }

    LaunchedEffect(autoPlay) {
        exoPlayer.playWhenReady = autoPlay
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> {
                    if (autoPlay) exoPlayer.play()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                this.resizeMode = resizeMode
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                findViewById<View>(R.id.exo_settings)?.visibility = View.GONE
                setControllerVisibilityListener(PlayerView.ControllerVisibilityListener {
                    findViewById<View>(R.id.exo_settings)?.visibility = View.GONE
                })
            }
        },
        update = { playerView ->
            if (playerView.player != exoPlayer) {
                playerView.player = exoPlayer
            }
            if (playerView.resizeMode != resizeMode) {
                playerView.resizeMode = resizeMode
            }
            playerView.findViewById<View>(R.id.exo_settings)?.visibility = View.GONE
        },
        modifier = modifier
    )
}

@OptIn(UnstableApi::class)
@Composable
fun ReproductorMultimedia(
    elemento: ItemGaleria,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT
) {
    val idYoutube = elemento.videoUrl?.let { extraerIdVideoYoutube(it) }
    val urlVideoArchivo = elemento.urlVideo

    when {
        idYoutube != null -> {
            ReproductorYouTube(
                idVideo = idYoutube,
                modifier = modifier
            )
        }
        !urlVideoArchivo.isNullOrBlank() -> {
            ReproductorVideoExo(
                urlVideo = urlVideoArchivo,
                modifier = modifier,
                autoPlay = autoPlay,
                resizeMode = resizeMode
            )
        }
        else -> {
            Box(
                modifier = modifier.background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Video no disponible",
                    color = Color.White
                )
            }
        }
    }
}
