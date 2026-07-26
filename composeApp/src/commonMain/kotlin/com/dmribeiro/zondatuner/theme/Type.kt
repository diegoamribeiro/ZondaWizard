package com.dmribeiro.zondatuner.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import zondawizard.composeapp.generated.resources.*

@Composable
private fun rememberMontserratFontFamily(): FontFamily {
    val light = Font(Res.font.montserrat_light, FontWeight.Light)
    val regular = Font(Res.font.montserrat_regular, FontWeight.Normal)
    val medium = Font(Res.font.montserrat_medium, FontWeight.Medium)
    val semiBold = Font(Res.font.montserrat_semibold, FontWeight.SemiBold)
    val bold = Font(Res.font.montserrat_bold, FontWeight.Bold)
    val extraBold = Font(Res.font.montserrat_extrabold, FontWeight.ExtraBold)

    return remember(light, regular, medium, semiBold, bold, extraBold) {
        FontFamily(fonts = listOf(light, regular, medium, semiBold, bold, extraBold))
    }
}

/** Numerais tabulares: frequências não "pulam" quando o valor muda. */
private const val TABULAR_NUMBERS = "tnum"

@Composable
fun AppTypography(): Typography {
    val montserrat = rememberMontserratFontFamily()

    return remember(montserrat) {
        Typography(
            // Nota gigante do afinador
            displayLarge = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 96.sp,
                lineHeight = 100.sp,
                letterSpacing = (-1).sp,
            ),
            // Nota em contextos menores (preview, seleção)
            displayMedium = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 56.sp,
                lineHeight = 60.sp,
                letterSpacing = 0.sp,
            ),
            // Títulos de tela
            headlineLarge = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp,
            ),
            // Títulos da top bar e seções
            titleLarge = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
            // Nome de card, subtítulos importantes
            titleMedium = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp,
            ),
            // Frequências (Hz) — numerais tabulares
            titleSmall = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.5.sp,
                fontFeatureSettings = TABULAR_NUMBERS,
            ),
            // Corpo principal
            bodyLarge = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
            // Corpo secundário
            bodyMedium = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
            ),
            // Botões
            labelLarge = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
            // Dicas, hints, chips
            labelMedium = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
            // Legendas mínimas, badges
            labelSmall = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
        )
    }
}
