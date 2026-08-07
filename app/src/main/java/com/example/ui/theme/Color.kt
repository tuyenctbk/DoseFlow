package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Raw static values (used in non-composable contexts like static theme initialization)
val RawOledBlack = Color(0xFF000000)
val RawDarkSurface = Color(0xFF101018)
val RawDarkCard = Color(0xFF1B1B26)
val RawDarkCardBorder = Color(0xFF2C2C3E)
val RawTextPrimary = Color(0xFFF8FAFC)
val RawTextSecondary = Color(0xFF94A3B8)
val RawTextMuted = Color(0xFF64748B)

// AppColors structure for dynamic selection
data class AppColors(
    val oledBlack: Color,
    val darkSurface: Color,
    val darkCard: Color,
    val darkCardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

val DarkAppColors = AppColors(
    oledBlack = RawOledBlack,
    darkSurface = RawDarkSurface,
    darkCard = RawDarkCard,
    darkCardBorder = RawDarkCardBorder,
    textPrimary = RawTextPrimary,
    textSecondary = RawTextSecondary,
    textMuted = RawTextMuted
)

val LightAppColors = AppColors(
    oledBlack = Color(0xFFFAFAFE), // dynamic light background
    darkSurface = Color(0xFFF1F5F9), // light surface
    darkCard = Color(0xFFFFFFFF), // pure white card
    darkCardBorder = Color(0xFFE2E8F0), // light card border
    textPrimary = Color(0xFF0F172A), // dark slate
    textSecondary = Color(0xFF475569), // slate 600
    textMuted = Color(0xFF64748B) // slate 500
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

// Public dynamic properties (used in Composable contexts)
val OledBlack: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.oledBlack

val DarkSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.darkSurface

val DarkCard: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.darkCard

val DarkCardBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.darkCardBorder

val PillViolet = Color(0xFF8B5CF6)
val PillVioletLight = Color(0xFFA78BFA)
val PillVioletDark = Color(0xFF6D28D9)

val HydrationCyan = Color(0xFF06B6D4)
val HydrationCyanLight = Color(0xFF22D3EE)
val HydrationCyanDark = Color(0xFF0891B2)

val SuccessEmerald = Color(0xFF10B981)
val SuccessEmeraldLight = Color(0xFF34D399)

val WarningAmber = Color(0xFFF59E0B)
val ErrorRose = Color(0xFFF43F5E)

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textMuted
