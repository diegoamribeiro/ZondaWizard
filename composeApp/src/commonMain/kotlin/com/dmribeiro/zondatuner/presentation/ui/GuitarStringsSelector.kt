package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.theme.ZondaTheme
import com.dmribeiro.zondatuner.utils.playTone
import com.dmribeiro.zondatuner.utils.runAudio
import com.dmribeiro.zondatuner.utils.vibrate
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import zondawizard.composeapp.generated.resources.Res
import zondawizard.composeapp.generated.resources.pick_filled_upside_down
import kotlin.math.PI
import kotlin.math.sin

private val StringThicknesses = listOf(5.dp, 4.dp, 3.dp, 2.5.dp, 2.dp, 1.5.dp)
private val NutBarHeight = 28.dp
private val PickSize = 56.dp
/** Elevação compartilhada: cordas + barra do nut sobem juntas no layout invertido. */
private val InvertedLift = 17.dp
/** Distância do topo do canvas até onde a corda encosta na ponta da palheta (abaixo das letras). */
private val InvertedPickTipY = 16.dp

@Composable
fun GuitarStringsSelector(
    tuning: TuningDataUi,
    selectedString: Int,
    isTwelfthFretMode: Boolean,
    onStringSelected: (Int) -> Unit,
    onToggleTwelfthFretMode: () -> Unit,
    modifier: Modifier = Modifier,
    inverted: Boolean = true,
) {
    val strings = tuning.getGuitarStrings()
    val barColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 4.dp)
            .then(if (inverted) Modifier.padding(bottom = 12.dp) else Modifier),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(NutBarHeight)
                .align(if (inverted) Alignment.BottomCenter else Alignment.TopCenter)
                .then(if (inverted) Modifier.offset(y = -InvertedLift) else Modifier)
                .background(barColor, RoundedCornerShape(6.dp)),
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = if (inverted) Alignment.Top else Alignment.Bottom,
        ) {
            strings.forEachIndexed { index, guitarString ->
                StringColumn(
                    guitarString = guitarString,
                    stringIndex = index,
                    isSelected = guitarString.number == selectedString,
                    isTwelfthFretMode = isTwelfthFretMode,
                    inverted = inverted,
                    onStringSelected = { onStringSelected(guitarString.number) },
                    onToggleTwelfthFretMode = onToggleTwelfthFretMode,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun StringColumn(
    guitarString: GuitarString,
    stringIndex: Int,
    isSelected: Boolean,
    isTwelfthFretMode: Boolean,
    inverted: Boolean,
    onStringSelected: () -> Unit,
    onToggleTwelfthFretMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val twelfthColor = ZondaTheme.extended.twelfthFret
    val primaryColor = MaterialTheme.colorScheme.primary
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val baseColor = when {
        isSelected && isTwelfthFretMode -> twelfthColor
        isSelected -> primaryColor
        else -> mutedColor
    }
    val ghostColor = baseColor.copy(alpha = if (isSelected) 0.25f else 0.12f)

    val waveOffset = if (isSelected) animateWave(4f, 100) else 0f
    val delayedWave = if (isSelected) animateWave(-5f, 550) else 0f
    val tWave = if (isSelected) animateWave(-6f, 350) else 0f
    val thickness = StringThicknesses.getOrElse(stringIndex) { 2.dp }

    val holeFill = MaterialTheme.colorScheme.background
    val holeRim = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    val selectedRim = MaterialTheme.colorScheme.primary
    val rimColor = when {
        isSelected && isTwelfthFretMode -> twelfthColor
        isSelected -> selectedRim
        else -> holeRim
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (inverted) Arrangement.Top else Arrangement.Bottom,
    ) {
        if (inverted) {
            PickButton(
                stringNumber = guitarString.number,
                note = guitarString.note,
                frequency = guitarString.frequency,
                isSelected = isSelected,
                twelfthFretModeEnabled = isTwelfthFretMode,
                showTwelfthBadge = isSelected && isTwelfthFretMode,
                pickPointsDown = true,
                onClick = { freq ->
                    onStringSelected()
                    runAudio { playTone(freq, 600) }
                },
                onLongClick = onToggleTwelfthFretMode,
                modifier = Modifier
                    .zIndex(1f)
                    .offset(y = 6.dp),
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(if (inverted) Modifier.offset(y = -InvertedLift) else Modifier),
            contentAlignment = if (inverted) Alignment.BottomCenter else Alignment.TopCenter,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val anchorY = nutHoleAnchorY(NutBarHeight, inverted)
                drawCurvedString(size, ghostColor, -tWave, thickness.toPx(), 1, anchorY, inverted)
            }
            Canvas(Modifier.fillMaxSize()) {
                val anchorY = nutHoleAnchorY(NutBarHeight, inverted)
                drawCurvedString(size, ghostColor, tWave, thickness.toPx(), 2, anchorY, inverted)
            }
            Canvas(Modifier.fillMaxSize()) {
                val anchorY = nutHoleAnchorY(NutBarHeight, inverted)
                drawCurvedString(
                    size,
                    ghostColor.copy(alpha = 0.15f),
                    delayedWave * 1.5f,
                    thickness.toPx(),
                    1,
                    anchorY,
                    inverted,
                )
            }
            Canvas(Modifier.fillMaxSize()) {
                val anchorY = nutHoleAnchorY(NutBarHeight, inverted)
                drawCurvedString(
                    size = size,
                    color = baseColor,
                    waveOffset = waveOffset,
                    strokeWidth = thickness.toPx(),
                    curves = 1,
                    anchorY = anchorY,
                    inverted = inverted,
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NutBarHeight)
                    .align(if (inverted) Alignment.BottomCenter else Alignment.TopCenter),
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val holeRadius = 7.dp.toPx()
                val ballRadius = (thickness.toPx() * 1.3f).coerceAtLeast(2.5.dp.toPx())

                drawCircle(color = holeFill, radius = holeRadius, center = center)
                drawCircle(
                    color = rimColor,
                    radius = holeRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
                drawCircle(color = baseColor, radius = ballRadius, center = center)
            }
        }

        if (!inverted) {
            PickButton(
                stringNumber = guitarString.number,
                note = guitarString.note,
                frequency = guitarString.frequency,
                isSelected = isSelected,
                twelfthFretModeEnabled = isTwelfthFretMode,
                showTwelfthBadge = isSelected && isTwelfthFretMode,
                pickPointsDown = false,
                onClick = { freq ->
                    onStringSelected()
                    runAudio { playTone(freq, 600) }
                },
                onLongClick = onToggleTwelfthFretMode,
                modifier = Modifier
                    .zIndex(1f)
                    .offset(y = (-12).dp),
            )
        }
    }
}

/** Y do centro do furo — topo da barra (normal) ou base da barra (invertido). */
private fun DrawScope.nutHoleAnchorY(nutBarHeight: Dp, inverted: Boolean): Float {
    val halfBar = nutBarHeight.toPx() / 2f
    return if (inverted) size.height - halfBar else halfBar
}

private fun DrawScope.drawCurvedString(
    size: Size,
    color: Color,
    waveOffset: Float,
    strokeWidth: Float,
    curves: Int,
    anchorY: Float,
    inverted: Boolean,
) {
    val pickEndY = if (inverted) {
        InvertedPickTipY.toPx()
    } else {
        size.height - PickSize.toPx() - 8.dp.toPx()
    }

    val path = Path().apply {
        moveTo(size.width / 2f, anchorY)
        for (i in 1..12) {
            val progress = i / 12f
            val xOffset = sin(progress * PI * curves).toFloat() * waveOffset
            val y = if (inverted) {
                anchorY - (anchorY - pickEndY) * progress
            } else {
                anchorY + (pickEndY - anchorY) * progress
            }
            lineTo(size.width / 2f + xOffset, y)
        }
    }
    drawPath(path, color, style = Stroke(strokeWidth, cap = StrokeCap.Round))
}

@Composable
fun animateWave(amplitude: Float, duration: Int): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val animatedValue by infiniteTransition.animateFloat(
        initialValue = -amplitude,
        targetValue = amplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "waveOffset",
    )
    return animatedValue
}

@Composable
fun PickButton(
    stringNumber: Int,
    note: String,
    frequency: Float,
    isSelected: Boolean,
    twelfthFretModeEnabled: Boolean,
    showTwelfthBadge: Boolean,
    pickPointsDown: Boolean,
    onClick: (Float) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vibrateAction = vibrate()
    val twelfthColor = ZondaTheme.extended.twelfthFret
    val pickTint = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    val labelColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showTwelfthBadge) {
            Text(
                text = "12ª",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black,
                modifier = Modifier
                    .background(twelfthColor, RoundedCornerShape(50))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            )
        }

        Box(
            modifier = Modifier
                .size(PickSize)
                .pointerInput(twelfthFretModeEnabled, frequency) {
                    detectTapGestures(
                        onLongPress = {
                            vibrateAction()
                            val targetFrequency =
                                if (!twelfthFretModeEnabled) frequency * 2 else frequency
                            onClick(targetFrequency)
                            onLongClick()
                        },
                        onTap = {
                            val targetFrequency =
                                if (twelfthFretModeEnabled) frequency * 2 else frequency
                            onClick(targetFrequency)
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.pick_filled_upside_down),
                contentDescription = "Palheta da corda $stringNumber",
                modifier = Modifier
                    .size(PickSize)
                    .graphicsLayer { rotationZ = if (pickPointsDown) 180f else 0f },
                tint = pickTint,
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringNumber.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
                Text(
                    text = note,
                    style = MaterialTheme.typography.labelLarge,
                    color = labelColor,
                )
            }
        }
    }
}

fun TuningDataUi.getGuitarStrings(): List<GuitarString> = listOf(
    GuitarString(6, strings[0].frequency, strings[0].note, strings[0].octaveShift),
    GuitarString(5, strings[1].frequency, strings[1].note, strings[1].octaveShift),
    GuitarString(4, strings[2].frequency, strings[2].note, strings[2].octaveShift),
    GuitarString(3, strings[3].frequency, strings[3].note, strings[3].octaveShift),
    GuitarString(2, strings[4].frequency, strings[4].note, strings[4].octaveShift),
    GuitarString(1, strings[5].frequency, strings[5].note, strings[5].octaveShift),
)

@Preview
@Composable
fun GuitarStringsSelectorPreview() {
    val sampleTuning = TuningDataUi(
        id = 0L,
        name = "Standard",
        description = "EADGBE",
        strings = listOf(
            GuitarString(6, 82.41f, "E", 0),
            GuitarString(5, 110.00f, "A", 0),
            GuitarString(4, 146.83f, "D", 0),
            GuitarString(3, 196.00f, "G", 0),
            GuitarString(2, 246.94f, "B", 0),
            GuitarString(1, 329.63f, "E", 0),
        ),
    )

    MaterialTheme {
        GuitarStringsSelector(
            tuning = sampleTuning,
            selectedString = 4,
            isTwelfthFretMode = false,
            onStringSelected = {},
            onToggleTwelfthFretMode = {},
            inverted = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
        )
    }
}
