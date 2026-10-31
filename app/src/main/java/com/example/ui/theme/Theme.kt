package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MovixaDarkColorScheme = darkColorScheme(
    primary = MovixaRed,
    onPrimary = Color.White,
    primaryContainer = MovixaRedDark,
    onPrimaryContainer = Color.White,
    secondary = MovixaRedBright,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = Color.White,
    tertiary = AccentGold,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor
)

@Composable
fun MovixaTheme(
    content: @Composable () -> Unit
) {
    // Cinematic streaming experience uses an immersive dark theme
    MaterialTheme(
        colorScheme = MovixaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
