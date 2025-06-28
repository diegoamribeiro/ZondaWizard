package com.dmribeiro.zondatuner.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// PASSO 1: Definimos nossa paleta de cores diretamente neste arquivo.
// Isso resolve 100% dos erros de "Unresolved reference".

// Cor de Destaque Principal (Branding)
private val ZondaGold = Color(0xFFDAA520)
// Vermelho para ações destrutivas (Apagar) ou erros.
private val DestructiveRed = Color(0xFFFF3B30)

// Paleta para o Tema Escuro
private val DarkBackground = Color(0xFF121212) // Fundo principal
private val DarkSurface = Color(0xFF1E1E1E)    // Fundo de Cards e componentes
private val DarkOnSurface = Color(0xFFEAEAEA)   // Cor do texto principal
private val DarkOnSurfaceVariant = Color(0xFFA5A5A5) // Cor de texto secundário

// Paleta para o Tema Claro
private val LightBackground = Color(0xFFF7F7F7)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF1C1C1C)
private val LightOnSurfaceVariant = Color(0xFF616161)


// PASSO 2: Usamos as cores acima para criar os esquemas de cores.

private val DarkColorScheme = darkColorScheme(
    primary = ZondaGold,
    onPrimary = Color.Black,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DestructiveRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ZondaGold,
    onPrimary = Color.Black,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = DestructiveRed,
    onError = Color.White
)

/**
 * O Composable principal do Tema do App.
 * Ele aplica as cores e a tipografia que definimos.
 */
@Composable
fun ZondaTunerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = AppTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography, // <-- Passamos o objeto correto para o tema
        content = content
    )
}