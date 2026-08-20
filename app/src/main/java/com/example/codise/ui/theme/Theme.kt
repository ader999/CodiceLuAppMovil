package com.example.codise.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AzulPetroleo,
    secondary = Celeste,
    tertiary = GrisClaro,
    background = AzulPetroleo,
    surface = AzulPetroleo,
    onPrimary = BlancoBase,
    onSecondary = NegroPuro,
    onTertiary = NegroPuro,
    onBackground = BlancoBase,
    onSurface = BlancoBase,
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPetroleo,
    secondary = Celeste,
    tertiary = GrisClaro,
    background = Celeste,
    surface = BlancoBase,
    onPrimary = BlancoBase,
    onSecondary = NegroPuro,
    onTertiary = NegroPuro,
    onBackground = NegroPuro,
    onSurface = NegroPuro,
)

@Composable
fun Codice路Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
