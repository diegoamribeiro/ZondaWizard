package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.dmribeiro.zondatuner.audio.MicrophoneCapture
import com.dmribeiro.zondatuner.audio.TunerReadingHold
import com.dmribeiro.zondatuner.audio.createPitchSmoother
import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.navigation.AppDestination
import com.dmribeiro.zondatuner.navigation.LocalTopBarMenuActions
import com.dmribeiro.zondatuner.permissions.getPermissionHandler
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel
import com.dmribeiro.zondatuner.theme.ZondaTheme
import com.dmribeiro.zondatuner.utils.runAudio
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.TimeSource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TunerScreenContent(
    onBack: () -> Unit,
    tuning: TuningDataUi,
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
    viewModel: HomeScreenModel = koinInject()
) {
    var currentTuning by remember { mutableStateOf(tuning) }
    var detectedFrequency by remember { mutableStateOf(0f) }
    var signalActive by remember { mutableStateOf(false) }
    var isTwelfthFretMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedStringIndex by remember { mutableStateOf(6) }

    val navigator = LocalNavigator.current
    val menuActions = LocalTopBarMenuActions.current

    DisposableEffect(currentTuning) {
        menuActions.onEdit = {
            navigator?.push(AppDestination.CreateTuningScreen(existingTuning = currentTuning))
        }
        menuActions.onDelete = { showDeleteDialog = true }
        onDispose { menuActions.clear() }
    }

    // Registra o uso para abrir direto nesta afinação na próxima sessão.
    LaunchedEffect(currentTuning.id) {
        viewModel.markTuningUsed(currentTuning.id)
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

    val selectedString = currentTuning.getGuitarStrings().find { it.number == selectedStringIndex }
        ?: currentTuning.getGuitarStrings().first()
    val targetFrequency =
        if (isTwelfthFretMode) selectedString.frequency * 2 else selectedString.frequency
    val targetNote = selectedString.note

    // Troca de corda/modo/afinação: descarta histórico para não arrastar valores antigos.
    LaunchedEffect(currentTuning, selectedStringIndex, isTwelfthFretMode) {
        pitchSmoother.reset()
        readingHold.reset()
        detectedFrequency = 0f
        signalActive = false
    }

    val cents = if (detectedFrequency > 0f && targetFrequency > 0f) {
        (1200 * log2(detectedFrequency / targetFrequency)).toFloat()
    } else {
        0f
    }
    val isInTune = signalActive && detectedFrequency > 0f && abs(cents) < 5f

    val inTuneBorderColor by animateColorAsState(
        targetValue = if (isInTune) ZondaTheme.extended.success else Color.Transparent,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "inTuneBorder",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(width = 3.dp, color = inTuneBorderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                val allTunings by viewModel.tuningState.listState.collectAsState()
                TuningSelectorDropdown(
                    current = currentTuning,
                    tunings = allTunings,
                    onSelected = { selected ->
                        currentTuning = selected
                        selectedStringIndex = 6
                        isTwelfthFretMode = false
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                if (isTwelfthFretMode) {
                    Text(
                        text = "Modo 12ª casa — todas as cordas na oitava",
                        style = MaterialTheme.typography.labelMedium,
                        color = ZondaTheme.extended.twelfthFret,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = "Segure a palheta para afinar na 12ª casa",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TunerDisplay(
                detectedFrequency = detectedFrequency,
                targetFrequency = targetFrequency,
                targetNote = targetNote,
                signalActive = signalActive,
                isInTune = isInTune,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .padding(bottom = 8.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cordas invertidas: palhetas perto do afinador, nut na base da tela.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            ) {
                GuitarStringsSelector(
                    tuning = currentTuning,
                    selectedString = selectedStringIndex,
                    isTwelfthFretMode = isTwelfthFretMode,
                    onStringSelected = { selectedStringIndex = it },
                    onToggleTwelfthFretMode = { isTwelfthFretMode = !isTwelfthFretMode },
                    inverted = true,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Apagar Afinação", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Tem certeza que deseja apagar '${currentTuning.name}'?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeTuning(currentTuning.id)
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

/** Nome da afinação com dropdown para trocar sem voltar à Home. */
@Composable
private fun TuningSelectorDropdown(
    current: TuningDataUi,
    tunings: List<TuningDataUi>,
    onSelected: (TuningDataUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = current.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Trocar afinação",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            tunings.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(item.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = item.strings.joinToString(" ") { it.note },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        if (item.id != current.id) onSelected(item)
                    }
                )
            }
        }
    }
}

/** Gauge em arco compacto na base + nota no centro do arco + status. */
@Composable
fun TunerDisplay(
    detectedFrequency: Float,
    targetFrequency: Float,
    targetNote: String,
    signalActive: Boolean,
    isInTune: Boolean,
    modifier: Modifier = Modifier,
) {
    val cents = if (detectedFrequency > 0f && targetFrequency > 0f) {
        (1200 * log2(detectedFrequency / targetFrequency)).toFloat()
    } else {
        0f
    }
    val clampedCents = cents.coerceIn(-50f, 50f)

    val animatedCents by animateFloatAsState(
        targetValue = clampedCents,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
    )

    val noteColor = if (isInTune) ZondaTheme.extended.success else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            CentsGauge(
                cents = animatedCents,
                isInTune = isInTune,
                hasSignal = detectedFrequency > 0f,
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 28.dp),
            ) {
                Text(
                    text = targetNote,
                    style = MaterialTheme.typography.displayMedium,
                    color = noteColor,
                )

                val detectedText = if (detectedFrequency > 0f) {
                    "${detectedFrequency.roundToInt()} Hz"
                } else {
                    "--- Hz"
                }
                Text(
                    text = "$detectedText / ${targetFrequency.roundToInt()} Hz",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val statusText = when {
            detectedFrequency <= 0f -> "Aguardando som..."
            !signalActive -> "..."
            isInTune -> "Afinado!"
            cents < -5f -> "Aperte ↑"
            cents > 5f -> "Afrouxe ↓"
            else -> "..."
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            color = if (isInTune) {
                ZondaTheme.extended.success
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

/**
 * Arco de -50 a +50 cents: trilha discreta, zona central de "afinado",
 * traços de escala e ponteiro dourado.
 */
@Composable
private fun CentsGauge(
    cents: Float,
    isInTune: Boolean,
    hasSignal: Boolean,
    modifier: Modifier = Modifier,
) {
    val trackColor = ZondaTheme.extended.meterTrack
    val successColor = ZondaTheme.extended.success
    val needleColor = MaterialTheme.colorScheme.primary
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height * 0.92f
        val radius = min(size.width / 2f - 2.dp.toPx(), size.height * 0.90f)

        // -50 cents → 180° (esquerda), 0 → 270° (topo), +50 → 360° (direita)
        fun angleDegrees(c: Float) = 180f + (c + 50f) / 100f * 180f
        fun pointAt(angleDeg: Float, r: Float): Offset {
            val rad = angleDeg * PI.toFloat() / 180f
            return Offset(cx + r * cos(rad), cy + r * sin(rad))
        }

        val arcStroke = 6.dp.toPx()
        val arcRect = androidx.compose.ui.geometry.Rect(
            left = cx - radius, top = cy - radius,
            right = cx + radius, bottom = cy + radius
        )

        drawArc(
            color = trackColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = arcStroke, cap = StrokeCap.Round)
        )

        // Zona central (±5 cents)
        drawArc(
            color = if (isInTune) successColor else successColor.copy(alpha = 0.35f),
            startAngle = angleDegrees(-5f),
            sweepAngle = angleDegrees(5f) - angleDegrees(-5f),
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = arcStroke, cap = StrokeCap.Round)
        )

        // Escala: traço a cada 10 cents, maiores a cada 25
        for (c in -50..50 step 5) {
            val isMajor = c % 25 == 0
            val angle = angleDegrees(c.toFloat())
            val outer = radius - arcStroke
            val inner = outer - (if (isMajor) 12.dp.toPx() else 6.dp.toPx())
            drawLine(
                color = tickColor.copy(alpha = if (isMajor) 0.7f else 0.35f),
                start = pointAt(angle, inner),
                end = pointAt(angle, outer),
                strokeWidth = if (isMajor) 2.5f else 1.5f,
                cap = StrokeCap.Round
            )
        }

        // Ponteiro — origem no centro focal (perto da nota)
        if (hasSignal) {
            val needleAngle = angleDegrees(cents)
            val needleStart = radius * 0.22f
            val needleEnd = radius - arcStroke - 10.dp.toPx()
            drawLine(
                color = if (isInTune) successColor else needleColor,
                start = pointAt(needleAngle, needleStart),
                end = pointAt(needleAngle, needleEnd),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // Marcador central (0 cents)
        drawCircle(
            color = if (isInTune) successColor else needleColor.copy(alpha = 0.6f),
            radius = 3.dp.toPx(),
            center = Offset(cx, cy),
        )
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
