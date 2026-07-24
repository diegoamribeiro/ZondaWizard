package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmribeiro.zondatuner.audio.MicrophoneCapture
import com.dmribeiro.zondatuner.audio.TunerReadingHold
import com.dmribeiro.zondatuner.audio.createPitchSmoother
import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.permissions.getPermissionHandler
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel
import com.dmribeiro.zondatuner.utils.runAudio
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.time.TimeSource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TunerScreenContent(
    onBack: () -> Unit,
    tuning: TuningDataUi,
    deleteMenuClicked: Boolean = false,
    onDeleteMenuHandled: () -> Unit = {},
) {
    var permissionGranted by remember { mutableStateOf<Boolean?>(null) }
    val permissionHandler = getPermissionHandler()

    LaunchedEffect(Unit) {
        permissionHandler.hasAudioPermission { granted ->
            permissionGranted = granted
            if (!granted) {
                permissionHandler.requestAudioPermission { newGranted ->
                    permissionGranted = newGranted
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (permissionGranted) {
            true -> TunerScreenWithAudio(
                onBack = onBack,
                tuning = tuning,
                deleteMenuClicked = deleteMenuClicked,
                onDeleteMenuHandled = onDeleteMenuHandled,
            )
            false -> PermissionRequestScreen(onRequestPermission = {
                permissionHandler.requestAudioPermission { granted ->
                    permissionGranted = granted
                }
            })

            null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// Tela de permissão estilizada com o tema
@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Acesso ao Microfone",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Precisamos da sua permissão para usar o microfone e detectar o som do seu instrumento.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Conceder Permissão")
        }
    }
}

@Composable
fun TunerScreenWithAudio(
    onBack: () -> Unit,
    tuning: TuningDataUi,
    deleteMenuClicked: Boolean = false,
    onDeleteMenuHandled: () -> Unit = {},
    viewModel: HomeScreenModel = koinInject()
) {
    var detectedFrequency by remember { mutableStateOf(0f) }
    var signalActive by remember { mutableStateOf(false) }
    var isTwelfthFretMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedStringIndex by remember { mutableStateOf(6) }

    LaunchedEffect(deleteMenuClicked) {
        if (deleteMenuClicked) {
            showDeleteDialog = true
            onDeleteMenuHandled()
        }
    }

    val pitchSmoother = remember { createPitchSmoother() }
    val readingHold = remember { TunerReadingHold() }
    val timeSource = TimeSource.Monotonic

    val audioProcessor = remember {
        MicrophoneCapture { freq ->
            runAudio {
                val now = timeSource.markNow()
                val smoothed = pitchSmoother.add(freq)
                readingHold.update(smoothed, now)
                detectedFrequency = readingHold.heldFrequency
                signalActive = readingHold.isReceivingSignal(now)
            }
        }
    }

    DisposableEffect(Unit) {
        audioProcessor.start()
        onDispose { audioProcessor.stop() }
    }

    // Só limpa depois de silêncio prolongado; falhas curtas mantêm o ponteiro estável.
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            val now = timeSource.markNow()
            signalActive = readingHold.isReceivingSignal(now)
            if (readingHold.shouldClear(now) && detectedFrequency > 0f) {
                readingHold.reset()
                pitchSmoother.reset()
                detectedFrequency = 0f
                signalActive = false
            }
        }
    }

    val selectedString = tuning.getGuitarStrings().find { it.number == selectedStringIndex }
        ?: tuning.getGuitarStrings().first()
    val targetFrequency =
        if (isTwelfthFretMode) selectedString.frequency * 2 else selectedString.frequency
    val targetNote = selectedString.note

    // Troca de corda/modo: descarta o histórico para não arrastar valores antigos.
    LaunchedEffect(selectedStringIndex, isTwelfthFretMode) {
        pitchSmoother.reset()
        readingHold.reset()
        detectedFrequency = 0f
        signalActive = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (tuning.displaySubtitle().isNotBlank()) {
            Text(
                text = tuning.displaySubtitle(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!isTwelfthFretMode) {
            Text(
                text = "Segure a palheta para afinar na 12ª casa",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Text(
                text = "Modo 12ª casa ativo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GuitarStringsSelector(
                tuning = tuning,
                selectedString = selectedStringIndex,
                isTwelfthFretMode = isTwelfthFretMode,
                onStringSelected = { selectedStringIndex = it },
                onToggleTwelfthFretMode = {
                    isTwelfthFretMode = !isTwelfthFretMode
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            TuningMeterBar(
                detectedFrequency = detectedFrequency,
                targetFrequency = targetFrequency,
                targetNote = targetNote,
                signalActive = signalActive,
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Apagar Afinação", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Tem certeza que deseja apagar '${tuning.name}'?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeTuning(tuning.id)
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sim, Apagar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TuningMeterBar(
    detectedFrequency: Float,
    targetFrequency: Float,
    targetNote: String,
    signalActive: Boolean = true,
) {
    val cents = if (detectedFrequency > 0f && targetFrequency > 0f) {
        (1200 * log2(detectedFrequency / targetFrequency)).toFloat()
    } else {
        0f
    }
    val clampedCents = cents.coerceIn(-50f, 50f)
    val isInTune = signalActive && abs(cents) < 5f
    val successGreen = Color(0xFF34C759)

    val indicatorColor = if (isInTune) successGreen else MaterialTheme.colorScheme.onSurfaceVariant

    val offsetRatio by animateFloatAsState(
        targetValue = clampedCents / 50f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
    )

    // Layout principal do novo medidor: painel de texto e a barra abaixo
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Painel de Texto (similar ao anterior)
        Text(
            text = targetNote,
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 60.sp),
            color = indicatorColor
        )

        // Exibe as frequências de forma limpa
        val detectedText = if (detectedFrequency > 0f) {
            "${detectedFrequency.roundToInt()} Hz"
        } else {
            "--- Hz"
        }
        val targetText = "${targetFrequency.roundToInt()} Hz"

        Text(
            text = "$detectedText / $targetText",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFFDAA520)
        )
        Spacer(modifier = Modifier.height(8.dp))
        val statusText = when {
            detectedFrequency <= 0f -> "Aguardando som..."
            !signalActive -> "..."
            isInTune -> "Afinado!"
            cents < -5f -> "Aperte"
            cents > 5f -> "Afrouxe"
            else -> "..."
        }
        Text(statusText, style = MaterialTheme.typography.titleLarge, color = indicatorColor)

        Spacer(modifier = Modifier.height(24.dp))

        // Canvas para desenhar a barra horizontal
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)) {
            val barHeight = size.height * 0.5f
            val barWidth = size.width * 0.9f
            val barTopLeft = Offset((size.width - barWidth) / 2, (size.height - barHeight) / 2)

            // --- Barra de Gradiente de Fundo ---
            val brush = Brush.horizontalGradient(
                // Os 'colorStops' garantem que o verde fique exatamente no centro
                colorStops = arrayOf(
                    0.0f to Color.Red,
                    0.4f to Color.Yellow,
                    0.5f to successGreen, // Verde no meio
                    0.6f to Color.Yellow,
                    1.0f to Color.Red
                )
            )
            drawRoundRect(
                brush = brush,
                topLeft = barTopLeft,
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2)
            )

            // --- Linha Indicadora Central (para referência) ---
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(center.x, barTopLeft.y - 10.dp.toPx()),
                end = Offset(center.x, barTopLeft.y + barHeight + 10.dp.toPx()),
                strokeWidth = 2f
            )

            // --- Ponteiro/Indicador que se move ---
            val indicatorPositionX = center.x + (offsetRatio * barWidth / 2)
            drawLine(
                color = indicatorColor,
                start = Offset(indicatorPositionX, barTopLeft.y - 10.dp.toPx()),
                end = Offset(indicatorPositionX, barTopLeft.y + barHeight + 10.dp.toPx()),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Preview
@Composable
fun TunerScreenContentPreview() {
    val sampleTuning = TuningDataUi(
        id = 0L,
        name = "Standard",
        description = "EADGBE",
        strings = listOf(
            GuitarString(6, 82.41f, "E", -2),
            GuitarString(5, 110.00f, "A", 0),
            GuitarString(4, 146.83f, "D", 0),
            GuitarString(3, 196.00f, "G", 0),
            GuitarString(2, 246.94f, "B", 0),
            GuitarString(1, 329.63f, "E", 0)
        )
    )

    MaterialTheme {
        TunerScreenContent(onBack = {}, tuning = sampleTuning)
    }
}