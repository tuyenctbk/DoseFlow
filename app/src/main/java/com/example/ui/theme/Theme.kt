package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
    background = RawOledBlack,
    onBackground = RawTextPrimary,
    surface = RawDarkSurface,
    onSurface = RawTextPrimary,
    surfaceVariant = RawDarkCard,
    onSurfaceVariant = RawTextSecondary,
    outline = RawDarkCardBorder,
    error = ErrorRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PillViolet,
    onPrimary = Color.White,
    primaryContainer = PillVioletLight,
    onPrimaryContainer = PillVioletDark,
    secondary = HydrationCyan,
    onSecondary = Color.White,
    secondaryContainer = HydrationCyanLight,
    onSecondaryContainer = HydrationCyanDark,
    tertiary = SuccessEmerald,
    onTertiary = Color.White,
    background = Color(0xFFFAFAFE),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF1F5F9),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    error = ErrorRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Set false to preserve DoseFlow brand aesthetic
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) OledDarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
