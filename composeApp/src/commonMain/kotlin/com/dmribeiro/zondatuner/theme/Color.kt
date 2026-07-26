package com.dmribeiro.zondatuner.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ---------- Tokens da marca ----------

/** Dourado Zonda — primary, marca, corda selecionada, ponteiro do medidor. */
val ZondaGold = Color(0xFFDAA520)

/** Único verde de "afinado" em todo o app. */
val SuccessGreen = Color(0xFF4CD964)

/** Vermelho para muito desafinado, erros e ações destrutivas. */
val SignalRed = Color(0xFFFF453A)

/** Âmbar do modo 12ª casa — harmoniza com o dourado da marca. */
val TwelfthAmber = Color(0xFFFFB340)

// ---------- Paleta dark (preto quente) ----------

val DeepBlack = Color(0xFF0D0B08)
val WarmSurface = Color(0xFF1A1712)
val WarmSurfaceHigh = Color(0xFF262119)
val TextPrimaryDark = Color(0xFFF2EFE9)
val TextSecondaryDark = Color(0xFFA5A093)

// ---------- Paleta light (derivada dos mesmos tons quentes) ----------

val WarmWhite = Color(0xFFF7F5F0)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceHigh = Color(0xFFEFEBE3)
val TextPrimaryLight = Color(0xFF1C1913)
val TextSecondaryLight = Color(0xFF6B665C)

/**
 * Cores semânticas que não têm slot no ColorScheme do Material3.
 * Acesse via [ZondaTheme.extended].
 */
@Immutable
data class ZondaExtendedColors(
    val success: Color,
    val twelfthFret: Color,
    val meterTrack: Color,
)

val DarkExtendedColors = ZondaExtendedColors(
    success = SuccessGreen,
    twelfthFret = TwelfthAmber,
    meterTrack = Color(0xFF3A352C),
)

val LightExtendedColors = ZondaExtendedColors(
    success = Color(0xFF2FB350),
    twelfthFret = Color(0xFFE09A1F),
    meterTrack = Color(0xFFD8D2C6),
)

val LocalZondaExtendedColors = staticCompositionLocalOf { DarkExtendedColors }
