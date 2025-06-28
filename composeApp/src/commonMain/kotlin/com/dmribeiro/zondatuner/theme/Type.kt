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

/**
 * Função Composable privada que carrega os arquivos de fonte e os "lembra"
 * em uma instância de FontFamily. Esta é a maneira correta de lidar
 * com a chamada à função Font() que também é @Composable.
 */
@Composable
private fun rememberMontserratFontFamily(): FontFamily {
    // Carrega cada arquivo de fonte associando-o ao seu peso (FontWeight) correto.
    val regular = Font(Res.font.montserrat_regular, FontWeight.Normal)
    val medium = Font(Res.font.montserrat_medium, FontWeight.Medium)
    val semiBold = Font(Res.font.montserrat_semibold, FontWeight.SemiBold)
    val bold = Font(Res.font.montserrat_bold, FontWeight.Bold)

    // A função 'remember' garante que a FontFamily não seja recriada em cada recomposição,
    // otimizando a performance.
    return remember(regular, medium, semiBold, bold) {
        FontFamily(
            fonts = listOf(regular, medium, semiBold, bold)
        )
    }
}

/**
 * Esta é a função pública que você usará no seu MaterialTheme.
 * Ela obtém a família de fontes e constrói um objeto Typography completo.
 */
@Composable
fun AppTypography(): Typography {
    val montserratFamily = rememberMontserratFontFamily()

    // Usamos 'remember' novamente para que todos os objetos Typography seja cacheado.
    return remember(montserratFamily) {
        Typography(
            // Títulos de tela (ex: "Minhas Afinações")
            headlineLarge = TextStyle(
                fontFamily = montserratFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp
            ),
            // Títulos de seção (ex: "Editar Afinação")
            titleLarge = TextStyle(
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            // Texto principal do corpo
            bodyLarge = TextStyle(
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            // Texto em botões
            labelLarge = TextStyle(
                fontFamily = montserratFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
            // Texto de corpo um pouco menor ou secundário
            bodyMedium = TextStyle(
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            ),
            // Legendas e textos pequenos
            labelSmall = TextStyle(
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            )
        )
    }
}