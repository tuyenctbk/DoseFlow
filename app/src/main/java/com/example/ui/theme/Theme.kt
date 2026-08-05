package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OledDarkColorScheme = darkColorScheme(
    primary = PillViolet,
    onPrimary = Color.White,
    primaryContainer = PillVioletDark,
    onPrimaryContainer = PillVioletLight,
    secondary = HydrationCyan,
    onSecondary = Color.Black,
    secondaryContainer = HydrationCyanDark,
    onSecondaryContainer = HydrationCyanLight,
    tertiary = SuccessEmerald,
    onTertiary = Color.Black,
    background = OledBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = ErrorRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Set false to preserve DoseFlow brand OLED aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OledDarkColorScheme,
        typography = Typography,
        content = content
    )
}
