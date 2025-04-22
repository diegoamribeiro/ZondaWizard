package com.dmribeiro.zondatuner.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Typography
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val DarkColorScheme = darkColorScheme(
    primary = LightGreen,
    onPrimary = Gray900,
    background = Gray700,
    surface = Gray900,
    onBackground = Gray100,
    onSurface = Gray100
)

private val LightColorScheme = lightColorScheme(
    primary = LightGreen,
    onPrimary = Gray900,
    background = Gray700,
    surface = Gray900,
    onBackground = Gray100,
    onSurface = Gray100

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ZondaTunerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val myColorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = myColorScheme,
        typography = Typography,
        content = content
    )
}