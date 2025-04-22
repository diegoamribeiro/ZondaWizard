package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun TunerMeter(
    detectedFrequency: Float,
    targetFrequency: Float,
    note: String
) {
    val tolerance = 2 // 🔹 Define um intervalo de tolerância (±2 Hz)

    // 🔹 Converte valores para `Int` para exibição e comparação
    val detectedInt = detectedFrequency.roundToInt()
    val targetInt = targetFrequency.roundToInt()

    // 🔹 Considera afinado se a frequência detectada estiver dentro da tolerância
    val isTuned = detectedInt in (targetInt - tolerance)..(targetInt + tolerance)

    // 🔹 Define a cor e o ícone baseado no estado de afinação
    val tuningIcon = if (isTuned) "✅" else "☑️"
    val tuningColor by animateColorAsState(
        targetValue = if (isTuned) Color(0xFF4CAF50) else Color.Gray,
        animationSpec = tween(durationMillis = 200)
    )

    // 🔹 Ajuste da posição da bolinha no medidor
    val frequencyOffset = detectedInt - targetInt
    val rawPosition = ((frequencyOffset / (targetInt * 0.1f)) * 180f)
    val ballPosition by animateFloatAsState(
        targetValue = rawPosition.coerceIn(-200f, 200f),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔹 Exibe a nota e a frequência alvo
        Text(
            text = "$note - $targetInt Hz",
            fontSize = 22.sp,
            color = Color.Green,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Indicação visual para apertar ou afrouxar a corda
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val tightenColor = if (detectedInt < targetInt) Color.Yellow else Color.Gray
            val tightenIcon = if (detectedInt > targetInt) "" else "🔺"
            val loosenIcon = if (detectedInt < targetInt) "" else "🔻"
            val loosenColor = if (detectedInt > targetInt) Color.Red else Color.Gray

            Text("Aperte $tightenIcon", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = tightenColor)
            Text(" $loosenIcon Afrouxe", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = loosenColor)
        }

        // 🔹 Medidor com a bolinha indicadora
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(40.dp)
        ) {
            val width = size.width
            val height = size.height

            // 🔹 Barra principal
            drawLine(
                color = Color.Black,
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )

            // 🔹 Indicador central verde para afinação correta
            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(width / 2 - 10f, height / 2),
                end = Offset(width / 2 + 10f, height / 2),
                strokeWidth = 30f,
                cap = StrokeCap.Round
            )

            // 🔹 Desenha a bolinha indicadora
            val ballX = width / 2 + ballPosition
            drawCircle(
                color = Color(0xFFFF5722),
                radius = 14f,
                center = Offset(ballX, height / 2)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Exibe o status "Afinado!" com o ícone correspondente
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tuningIcon,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = tuningColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Afinado!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = tuningColor
            )
        }
    }
}