package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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
    val tolerance = 3 // 🔹 Define um intervalo de tolerância (±2 Hz)

    // 🔹 Buffer de frequências detectadas para suavização
    val frequencyBuffer = remember { mutableStateListOf<Float>() }

    // 🔹 Atualiza o buffer
    LaunchedEffect(detectedFrequency) {
        if (frequencyBuffer.size > 5) { // 🔹 Mantém as últimas 5 medições
            frequencyBuffer.removeFirst()
        }
        frequencyBuffer.add(detectedFrequency)
    }

    // 🔹 Faz a média das últimas leituras
// 🔹 Faz a média das últimas leituras, mas evita NaN
    val smoothedFrequency = if (frequencyBuffer.isNotEmpty()) {
        frequencyBuffer.average().toFloat()
    } else {
        0f // Ou targetFrequency, ou detectedFrequency, como quiser
    }
    val detectedInt = smoothedFrequency.roundToInt()
    val targetInt = targetFrequency.roundToInt()

    val isTuned = detectedInt in (targetInt - tolerance)..(targetInt + tolerance)

    val tuningIcon = if (isTuned) "✅" else "☑️"
    val tuningColor by animateColorAsState(
        targetValue = if (isTuned) Color(0xFF4CAF50) else Color.Gray,
        animationSpec = tween(durationMillis = 200)
    )

    val frequencyOffset = detectedInt - targetInt
    val rawPosition = ((frequencyOffset / (targetInt * 0.1f)) * 180f)
    val ballPosition by animateFloatAsState(
        targetValue = rawPosition.coerceIn(-200f, 200f),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )

    // (o resto do seu código permanece igual a partir daqui)
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$note - $targetInt Hz",
            fontSize = 22.sp,
            color = Color.Green,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(40.dp)
        ) {
            val width = size.width
            val height = size.height

            drawLine(
                color = Color.Black,
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )

            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(width / 2 - 10f, height / 2),
                end = Offset(width / 2 + 10f, height / 2),
                strokeWidth = 30f,
                cap = StrokeCap.Round
            )

            val ballX = width / 2 + ballPosition
            drawCircle(
                color = Color(0xFFFF5722),
                radius = 14f,
                center = Offset(ballX, height / 2)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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