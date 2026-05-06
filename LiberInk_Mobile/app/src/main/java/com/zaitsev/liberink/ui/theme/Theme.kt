package com.zaitsev.liberink.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

object ThemeManager {
    var isDarkTheme by mutableStateOf(false)

    fun initialize(context: android.content.Context) {
        val prefs = context.getSharedPreferences("liberink_prefs", android.content.Context.MODE_PRIVATE)
        isDarkTheme = prefs.getBoolean("is_dark_theme", false)
    }

    fun toggleTheme(context: android.content.Context) {
        isDarkTheme = !isDarkTheme
        val prefs = context.getSharedPreferences("liberink_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_dark_theme", isDarkTheme).apply()
    }
}

// 1. Custom class to hold all your specific UI roles
@Immutable
data class LiberInkColors(
    val mainInk: Color,
    val secondaryInk: Color,
    val paperMain: Color,
    val paperElevated: Color,
    val paperTexture: Color,
    val accentGold: Color,
    val accentTerracotta: Color,
    val dividerStrong: Color,
    val dividerSoft: Color,
    val shadow: Color,
    val glassSurface: Color,
    val onSurface: Color
)

// 2. Define the Light (Classic Paper) Palette
private val LightCustomPalette = LiberInkColors(
    mainInk = DarkWine,
    secondaryInk = Gray66,
    paperMain = AntiqueCream,
    paperElevated = PaperOffWhite,
    paperTexture = SoftGrayPaper,
    accentGold = AmberGold,
    accentTerracotta = Terracotta,
    dividerStrong = DarkWine20,
    dividerSoft = White20,
    shadow = Black25,
    glassSurface = White50,
    onSurface = RichBlack
)

// 3. Define the Dark (Night) Palette
private val DarkCustomPalette = LiberInkColors(
    mainInk = PaleGold,
    secondaryInk = MutedRose,
    paperMain = Charcoal,
    paperElevated = RichBlack,
    paperTexture = PureBlack,
    accentGold = AmberGold,
    accentTerracotta = Terracotta,
    dividerStrong = White20,
    dividerSoft = Black25,
    shadow = PureBlack,
    glassSurface = Black60,
    onSurface = PureWhite
)

// Local Provider for the theme
val LocalLiberInkColors = staticCompositionLocalOf { LightCustomPalette }

@Composable
fun LiberInkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val customColors = if (darkTheme) DarkCustomPalette else LightCustomPalette

    // Map to Material3 ColorScheme for standard components (Buttons, etc.)
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = customColors.mainInk,
            background = customColors.paperMain,
            surface = customColors.paperElevated
        )
    } else {
        lightColorScheme(
            primary = customColors.mainInk,
            background = customColors.paperMain,
            surface = customColors.paperElevated
        )
    }

    // Status bar & Edge-to-edge logic
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = customColors.paperMain.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalLiberInkColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography, // Your existing Typography.kt
            content = content
        )
    }
}

object LiberInkTheme {
    val colors: LiberInkColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLiberInkColors.current
}