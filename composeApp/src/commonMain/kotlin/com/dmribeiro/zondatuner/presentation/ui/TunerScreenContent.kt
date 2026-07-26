package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.dmribeiro.zondatuner.audio.MicrophoneCapture
import com.dmribeiro.zondatuner.audio.StringAutoDetector
import com.dmribeiro.zondatuner.audio.TunerDiagnostics
import com.dmribeiro.zondatuner.audio.TunerReadingHold
import com.dmribeiro.zondatuner.audio.createPitchSmoother
import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.navigation.AppDestination
import com.dmribeiro.zondatuner.navigation.LocalTopBarMenuActions
import com.dmribeiro.zondatuner.permissions.getPermissionHandler
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel
import com.dmribeiro.zondatuner.theme.ZondaTheme
import com.dmribeiro.zondatuner.utils.GAUGE_CENTS_RANGE
import com.dmribeiro.zondatuner.utils.alignFrequencyToTargetOctave
import com.dmribeiro.zondatuner.utils.formatTunerHz
import com.dmribeiro.zondatuner.utils.frequencyDeltaCents
import com.dmribeiro.zondatuner.utils.isPlausibleTunerReading
import com.dmribeiro.zondatuner.utils.findBestStringMatch
import com.dmribeiro.zondatuner.utils.targetHzForMatch
import com.dmribeiro.zondatuner.utils.centsToGaugeAngleDegrees
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.math.PI
import kotlin.time.Duration.Companion.milliseconds
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
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
    var manualStringLock by remember { mutableStateOf(false) }

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

    val timeSource = TimeSource.Monotonic
    val pitchSmoother = remember { createPitchSmoother() }
    val readingHold = remember { TunerReadingHold() }
    val tunerDiagnostics = remember { TunerDiagnostics(timeSource = timeSource) }
    val stringAutoDetector = remember { StringAutoDetector() }
    // CONFLATED: mantém só a leitura mais recente, evita fila e corrida na Main.
    val frequencyUpdates = remember { Channel<Float>(Channel.CONFLATED) }

    val audioProcessor = remember(frequencyUpdates) {
        MicrophoneCapture { freq ->
            frequencyUpdates.trySend(freq)
        }
    }

    DisposableEffect(audioProcessor, frequencyUpdates) {
        audioProcessor.start()
        onDispose {
            audioProcessor.stop()
            frequencyUpdates.close()
        }
    }

    val selectedString = currentTuning.getGuitarStrings().find { it.number == selectedStringIndex }
        ?: currentTuning.getGuitarStrings().first()
    val targetFrequency =
        if (isTwelfthFretMode) selectedString.frequency * 2 else selectedString.frequency
    val targetNote = selectedString.note
    val currentTarget by rememberUpdatedState(targetFrequency)
    val currentStrings by rememberUpdatedState(currentTuning.getGuitarStrings())
    val manualLock by rememberUpdatedState(manualStringLock)
    val currentSelectedString by rememberUpdatedState(selectedStringIndex)
    val currentTwelfthMode by rememberUpdatedState(isTwelfthFretMode)

    // Pipeline serializado: evita corrida entre callbacks concorrentes (Android ~40/s).
    LaunchedEffect(frequencyUpdates) {
        for (freq in frequencyUpdates) {
            val now = timeSource.markNow()

            if (freq > 0f) {
                val strings = currentStrings
                val processingTargetHz = if (manualLock) {
                    val string = strings.find { it.number == currentSelectedString } ?: strings.first()
                    if (currentTwelfthMode) string.frequency * 2f else string.frequency
                } else {
                    findBestStringMatch(freq, strings)?.let { match ->
                        targetHzForMatch(match, strings)
                    } ?: run {
                        val string = strings.find { it.number == currentSelectedString } ?: strings.first()
                        if (currentTwelfthMode) string.frequency * 2f else string.frequency
                    }
                }
                val alignedRaw = alignFrequencyToTargetOctave(freq, processingTargetHz)
                if (isPlausibleTunerReading(alignedRaw, processingTargetHz)) {
                    val smoothed = pitchSmoother.add(alignedRaw)
                    if (isPlausibleTunerReading(smoothed, processingTargetHz)) {
                        readingHold.update(smoothed, now)
                    }
                }
            }
        }
    }

    // Atualiza UI em intervalo fixo — independente do loop de áudio (que pode bloquear no Channel).
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            val now = timeSource.markNow()
            signalActive = readingHold.isReceivingSignal(now)
            val uiHz = if (signalActive) readingHold.heldFrequency else 0f
            detectedFrequency = uiHz

            if (!manualStringLock && uiHz > 0f) {
                stringAutoDetector.evaluate(uiHz, currentTuning.getGuitarStrings())?.let { match ->
                    if (match.stringNumber != selectedStringIndex) {
                        selectedStringIndex = match.stringNumber
                    }
                }
            }

            if (TunerDiagnostics.isEnabled && uiHz > 0f) {
                val aligned = alignFrequencyToTargetOctave(uiHz, currentTarget)
                val uiCents = if (aligned > 0f && currentTarget > 0f) {
                    frequencyDeltaCents(aligned, currentTarget)
                } else {
                    0f
                }
                tunerDiagnostics.onPipeline(
                    rawHz = uiHz,
                    smoothedHz = uiHz,
                    heldHz = uiHz,
                    uiHz = uiHz,
                    targetHz = currentTarget,
                    signalActive = signalActive,
                    inTune = abs(uiCents) < 5f,
                )
            }
        }
    }

    // Só limpa depois de silêncio prolongado; falhas curtas mantêm o ponteiro estável.
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            val now = timeSource.markNow()
            signalActive = readingHold.isReceivingSignal(now)
            if (!signalActive && detectedFrequency > 0f) {
                detectedFrequency = 0f
            }
            if (readingHold.shouldClear(now)) {
                readingHold.reset()
                pitchSmoother.reset()
                stringAutoDetector.reset()
                manualStringLock = false
                detectedFrequency = 0f
                signalActive = false
            }
        }
    }

    // Troca de corda/modo/afinação: descarta histórico para não arrastar valores antigos.
    LaunchedEffect(currentTuning, selectedStringIndex, isTwelfthFretMode) {
        pitchSmoother.reset()
        readingHold.reset()
        detectedFrequency = 0f
        signalActive = false
    }

    val alignedFrequency = alignFrequencyToTargetOctave(detectedFrequency, targetFrequency)
    val cents = if (alignedFrequency > 0f && targetFrequency > 0f) {
        frequencyDeltaCents(alignedFrequency, targetFrequency)
    } else {
        0f
    }
    val isInTune = signalActive && detectedFrequency > 0f && abs(cents) < 5f

    val inTuneBorderColor by animateColorAsState(
        targetValue = if (isInTune) ZondaTheme.extended.success else Color.Transparent,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "inTuneBorder",
    )

    val allTunings by viewModel.tuningState.listState.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .border(
                width = 5.dp,
                color = inTuneBorderColor,
                shape = MaterialTheme.shapes.medium,
            ),
    ) {
        val isLandscape = maxWidth > maxHeight
        val landscapeGaugeHeight = minOf(maxHeight * 0.52f, maxWidth * 0.36f).coerceIn(130.dp, 200.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
        ) {
            TunerScreenHeader(
                currentTuning = currentTuning,
                allTunings = allTunings,
                isTwelfthFretMode = isTwelfthFretMode,
                manualStringLock = manualStringLock,
                onTuningSelected = { selected ->
                    currentTuning = selected
                    selectedStringIndex = 6
                    isTwelfthFretMode = false
                    manualStringLock = false
                    stringAutoDetector.reset()
                },
            )

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        TunerDisplay(
                            detectedFrequency = detectedFrequency,
                            targetFrequency = targetFrequency,
                            targetNote = targetNote,
                            signalActive = signalActive,
                            isInTune = isInTune,
                            diagnostics = tunerDiagnostics,
                            gaugeHeight = landscapeGaugeHeight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 320.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight(),
                    ) {
                        GuitarStringsSelector(
                            tuning = currentTuning,
                            selectedString = selectedStringIndex,
                            isTwelfthFretMode = isTwelfthFretMode,
                            onStringSelected = {
                                manualStringLock = true
                                selectedStringIndex = it
                            },
                            onToggleTwelfthFretMode = {
                                manualStringLock = true
                                isTwelfthFretMode = !isTwelfthFretMode
                            },
                            inverted = true,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))

                TunerDisplay(
                    detectedFrequency = detectedFrequency,
                    targetFrequency = targetFrequency,
                    targetNote = targetNote,
                    signalActive = signalActive,
                    isInTune = isInTune,
                    diagnostics = tunerDiagnostics,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .padding(bottom = 8.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                        onStringSelected = {
                            manualStringLock = true
                            selectedStringIndex = it
                        },
                        onToggleTwelfthFretMode = {
                            manualStringLock = true
                            isTwelfthFretMode = !isTwelfthFretMode
                        },
                        inverted = true,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
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

/** Dropdown da afinação + dica do modo 12ª casa. */
@Composable
private fun TunerScreenHeader(
    currentTuning: TuningDataUi,
    allTunings: List<TuningDataUi>,
    isTwelfthFretMode: Boolean,
    manualStringLock: Boolean,
    onTuningSelected: (TuningDataUi) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        TuningSelectorDropdown(
            current = currentTuning,
            tunings = allTunings,
            onSelected = onTuningSelected,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        when {
            isTwelfthFretMode -> {
                Text(
                    text = "Modo 12ª casa — todas as cordas na oitava",
                    style = MaterialTheme.typography.labelMedium,
                    color = ZondaTheme.extended.twelfthFret,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            manualStringLock -> {
                Text(
                    text = "Segure a palheta para afinar na 12ª casa",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            else -> {
                Text(
                    text = "Toque uma corda — detecção automática · segure a palheta para 12ª casa",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
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
    gaugeHeight: Dp = 200.dp,
    diagnostics: TunerDiagnostics? = null,
) {
    val alignedFrequency = alignFrequencyToTargetOctave(detectedFrequency, targetFrequency)
    val cents = if (alignedFrequency > 0f && targetFrequency > 0f) {
        frequencyDeltaCents(alignedFrequency, targetFrequency)
    } else {
        0f
    }
    val needleTargetCents = cents.coerceIn(-GAUGE_CENTS_RANGE, GAUGE_CENTS_RANGE)

    val needleCents = remember { Animatable(0f) }
    LaunchedEffect(needleTargetCents, detectedFrequency > 0f) {
        if (detectedFrequency <= 0f) {
            needleCents.animateTo(
                0f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
            )
            diagnostics?.onNeedleTarget(0f, snapped = false)
        } else {
            needleCents.animateTo(
                needleTargetCents,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            )
            diagnostics?.onNeedleTarget(needleTargetCents, snapped = false)
        }
    }

    LaunchedEffect(needleCents.value, diagnostics) {
        diagnostics?.onNeedleFrame(needleCents.value)
    }

    val noteColor = if (isInTune) ZondaTheme.extended.success else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gaugeHeight),
            contentAlignment = Alignment.BottomCenter,
        ) {
            CentsGauge(
                cents = needleCents.value,
                isInTune = isInTune,
                hasSignal = detectedFrequency > 0f,
                modifier = Modifier.fillMaxSize(),
            )

            if (TunerDiagnostics.isEnabled && diagnostics != null) {
                diagnostics.snapshot // observa atualizações do diagnóstico
                Text(
                    text = diagnostics.formatOverlay(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 28.dp),
            ) {
                Text(
                    text = targetNote,
                    style = MaterialTheme.typography.displayMedium,
                    color = noteColor,
                )

                val detectedText = if (signalActive && detectedFrequency > 0f) {
                    detectedFrequency.formatTunerHz()
                } else {
                    "--- Hz"
                }
                Text(
                    text = "$detectedText / ${targetFrequency.formatTunerHz()}",
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
 * traços de escala e ponteiro com haste fina, losango na ponta e hub dourado/branco.
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
        fun angleDegrees(c: Float) = centsToGaugeAngleDegrees(c)
        fun pointAt(angleDeg: Float, r: Float): Offset {
            val rad = angleDeg * PI.toFloat() / 180f
            return Offset(cx + r * cos(rad), cy + r * sin(rad))
        }

        val arcStroke = 11.25.dp.toPx()
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

        // Ponteiro: haste fina + losango na ponta, preso ao hub central.
        val hubGoldRadius = 7.dp.toPx()
        val hubWhiteRadius = 4.5.dp.toPx()
        val hubCenter = Offset(cx, cy)

        drawCircle(
            color = needleColor,
            radius = hubGoldRadius,
            center = hubCenter,
        )

        if (hasSignal) {
            val needleAngle = angleDegrees(cents)
            val needleColorActive = if (isInTune) successColor else needleColor
            val tipRadius = radius - arcStroke - 10.dp.toPx()
            val diamondLength = 7.dp.toPx()
            val shaftEndRadius = tipRadius - diamondLength
            val wingSpreadDeg = 2.1f

            drawLine(
                color = needleColorActive,
                start = pointAt(needleAngle, hubWhiteRadius),
                end = pointAt(needleAngle, shaftEndRadius),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Butt,
            )

            val tip = pointAt(needleAngle, tipRadius)
            val wingLeft = pointAt(needleAngle - wingSpreadDeg, shaftEndRadius)
            val wingRight = pointAt(needleAngle + wingSpreadDeg, shaftEndRadius)
            val diamondPath = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(wingLeft.x, wingLeft.y)
                lineTo(wingRight.x, wingRight.y)
                close()
            }
            drawPath(diamondPath, needleColorActive)
        }

        drawCircle(
            color = Color.White,
            radius = hubWhiteRadius,
            center = hubCenter,
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
