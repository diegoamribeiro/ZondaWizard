package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.utils.vibrate
import io.ktor.http.ContentType
import org.jetbrains.compose.resources.painterResource
import zondawizard.composeapp.generated.resources.Res
import zondawizard.composeapp.generated.resources.pick_filled_upside_down
import kotlin.math.sin

@Composable
fun GuitarStringsSelector(
    tuning: TuningDataUi,
    selectedString: Int,
    isTwelfthFretMode: Boolean,
    onStringSelected: (Int) -> Unit,
    onToggleTwelfthFretMode: () -> Unit
) {
    val stringThicknesses = listOf(6.dp, 5.dp, 4.dp, 3.dp, 2.dp, 1.dp)
    val strings = tuning.getGuitarStrings()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        strings.forEachIndexed { index, guitarString ->
            val isSelected = guitarString.number == selectedString
            val baseColor = if (isSelected) Color(0xFFDAA520) else Color(0xFFA0A0A0)
            val secondaryColor = baseColor.copy(alpha = 0.3f)

            val waveOffset = if (isSelected) animateWave(3f, 100) else 0f
            val delayedWaveOffset = if (isSelected) animateWave(-4f, 550) else 0f
            val tWaveOffset = if (isSelected) animateWave(-7f, 350) else 0f

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                // 🎸 Corda vibrando...
                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(stringThicknesses.getOrElse(index) { 3.dp })
                ) {
                    drawCurvedString(size, secondaryColor.copy(alpha = 0.2f), -tWaveOffset, stringThicknesses[index].toPx(), 1)
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(stringThicknesses.getOrElse(index) { 3.dp })
                ) {
                    drawCurvedString(size, secondaryColor.copy(alpha = 0.2f), tWaveOffset, stringThicknesses[index].toPx(), 2)
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(stringThicknesses.getOrElse(index) { 1.dp })
                ) {
                    drawCurvedString(size, secondaryColor.copy(alpha = 0.1f), delayedWaveOffset * 2f, stringThicknesses[index].toPx(), 1)
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(stringThicknesses.getOrElse(index) { 1.dp })
                ) {
                    drawCurvedString(size, secondaryColor, -delayedWaveOffset * 2f, stringThicknesses[index].toPx(), 1)
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(stringThicknesses.getOrElse(index) { 3.dp })
                ) {
                    drawCurvedString(size, secondaryColor.copy(alpha = 0.1f), -delayedWaveOffset * 2, stringThicknesses[index].toPx(), 2)
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(stringThicknesses.getOrElse(index) { 3.dp })
                ) {
                    drawCurvedString(size, baseColor, waveOffset, stringThicknesses[index].toPx(), 1)
                }

                // 🎸 PickButton atualizado
                PickButton(
                    stringNumber = guitarString.number,
                    note = guitarString.note,
                    isSelected = isSelected,
                    isTwelfthFretMode = if (isSelected) isTwelfthFretMode else false,
                    onClick = {
                        onStringSelected(guitarString.number)
                    },
                    onLongClick = {
                        onToggleTwelfthFretMode()
                    },
                    modifier = Modifier
                        .zIndex(1f)
                        .offset(y = (160).dp)
                )
            }
        }
    }
}

// 🔹 Função para desenhar a corda com curvatura
fun DrawScope.drawCurvedString(size: Size, color: Color, waveOffset: Float, strokeWidth: Float, curves: Int) {
    val path = Path().apply {
        moveTo(size.width / 2, 0f) // 🔹 Começa do topo
        for (i in 1..10) { // 🔹 Divide a corda em 10 segmentos para criar a onda
            val progress = i / 10f
            val xOffset = sin(progress * 3.14159 * curves).toFloat() * waveOffset // 🔹 Movimento senoidal
            val y = size.height * progress
            lineTo(size.width / 2 + xOffset, y)
        }
        lineTo(size.width / 2, size.height) // 🔹 Termina na base
    }
    drawPath(path, color, style = Stroke(strokeWidth, cap = StrokeCap.Round))
}

// 🔹 Animação de onda senoidal para a vibração da corda
@Composable
fun animateWave(amplitude: Float, duration: Int): Float {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = amplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Wave Animation"
    )
    return animatedValue
}

@Composable
fun PickButton(
    stringNumber: Int,
    note: String,
    isSelected: Boolean,
    isTwelfthFretMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val vibrateAction = vibrate()

    Box(
        modifier = modifier
            .size(60.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongClick() // ✅ Agora certinho!
                        vibrateAction()// 🧨 Vibra quando clicar longo
                    },
                    onTap = {
                        onClick() // ✅ Tap normal
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.pick_filled_upside_down),
            contentDescription = "Palheta",
            modifier = Modifier.size(60.dp),
            tint = when {
                isTwelfthFretMode -> Color(0xFFFFA500) // 🧡 Laranja no modo 12ª casa
                isSelected -> Color.Blue
                else -> Color.Gray
            }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = stringNumber.toString(),
                fontSize = 12.sp,
                color = Color.White
            )
            Text(
                text = note,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

// 🔹 Converte `TuningDataUi` para uma lista de `GuitarString`
fun TuningDataUi.getGuitarStrings(): List<GuitarString> {
    return listOf(
        GuitarString(6, strings[0].frequency, strings[0].note, strings[0].octaveShift),
        GuitarString(5, strings[1].frequency, strings[1].note, strings[1].octaveShift),
        GuitarString(4, strings[2].frequency, strings[2].note, strings[2].octaveShift),
        GuitarString(3, strings[3].frequency, strings[3].note, strings[3].octaveShift),
        GuitarString(2, strings[4].frequency, strings[4].note, strings[4].octaveShift),
        GuitarString(1, strings[5].frequency, strings[5].note, strings[5].octaveShift)
    )
}