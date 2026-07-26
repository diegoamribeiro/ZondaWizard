package com.dmribeiro.zondatuner.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ZondaGold,
    onPrimary = Color.Black,
    primaryContainer = WarmSurfaceHigh,
    onPrimaryContainer = ZondaGold,
    background = DeepBlack,
    surface = WarmSurface,
    surfaceVariant = WarmSurfaceHigh,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF4A443A),
    error = SignalRed,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = ZondaGold,
    onPrimary = Color.Black,
    primaryContainer = LightSurfaceHigh,
    onPrimaryContainer = Color(0xFF8A6A10),
    background = WarmWhite,
    surface = LightSurface,
    surfaceVariant = LightSurfaceHigh,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFC9C3B7),
    error = SignalRed,
    onError = Color.White,
)

/** Acesso às cores semânticas fora do ColorScheme M3 (success, twelfthFret...). */
object ZondaTheme {
    val extended: ZondaExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalZondaExtendedColors.current
}

@Composable
fun ZondaTunerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalZondaExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography(),
            shapes = ZondaShapes,
            content = content
        )
    }
}
