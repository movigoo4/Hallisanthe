package com.example.hallisanthe.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = Saffron,
    onPrimary = CreamWhite,
    primaryContainer = SaffronLight,
    onPrimaryContainer = SaffronDark,
    secondary = IndiaGreen,
    onSecondary = CreamWhite,
    secondaryContainer = IndiaGreenLight,
    background = CreamWhite,
    onBackground = SaffronDark,
    surface = WarmSurface,
    onSurface = SaffronDark,
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronLight,
    onPrimary = DarkBackground,
    primaryContainer = SaffronDark,
    onPrimaryContainer = SaffronLight,
    secondary = IndiaGreenLight,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = CreamWhite,
    surface = DarkSurface,
    onSurface = CreamWhite,
)

@Composable
fun HalliSantheTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Saffron.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}