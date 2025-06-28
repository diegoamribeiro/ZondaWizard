package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmribeiro.zondatuner.audio.MicrophoneCapture
import com.dmribeiro.zondatuner.permissions.getPermissionHandler
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt

@Composable
fun TunerScreenContent(
    onBack: () -> Unit,
    tuning: TuningDataUi
) {
    var permissionGranted by remember { mutableStateOf<Boolean?>(null) }
    val permissionHandler = getPermissionHandler()

    // Este LaunchedEffect verifica e solicita a permissão uma vez
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

    // O fundo geral agora é controlado pelo tema
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (permissionGranted) {
            true -> TunerScreenWithAudio(onBack, tuning)
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
    viewModel: HomeScreenModel = koinInject()
) {
    var detectedFrequency by remember { mutableStateOf(0f) }
    var isTwelfthFretMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedStringIndex by remember { mutableStateOf(6) }

    // CORREÇÃO: Usamos o CoroutineScope para garantir a atualização do estado na Main Thread
    val scope = rememberCoroutineScope()
    val audioProcessor = remember {
        MicrophoneCapture { freq ->
            scope.launch {
                detectedFrequency = freq
            }
        }
    }

    DisposableEffect(Unit) {
        audioProcessor.start()
        onDispose { audioProcessor.stop() }
    }

    val selectedString = tuning.getGuitarStrings().find { it.number == selectedStringIndex }
        ?: tuning.getGuitarStrings().first()
    val targetFrequency =
        if (isTwelfthFretMode) selectedString.frequency * 2 else selectedString.frequency
    val targetNote = selectedString.note

    // Layout principal da tela do afinador
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Box(modifier = Modifier.fillMaxWidth()){
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

        Spacer(Modifier.height(20.dp))

        /* 2) MEDIDOR EM ARCO – altura controlada, colado na base */
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TuningMeterArc(
                detectedFrequency = detectedFrequency,
                targetFrequency = targetFrequency,
                targetNote = targetNote
            )
        }

        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { showDeleteDialog = true }) {
                Text("Apagar afinação", color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = onBack) {
                Text("Voltar")
            }
        }
    }

    // Diálogo de alerta com o estilo do Material 3
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

// O NOVO COMPOSABLE DO MEDIDOR COM O PONTEIRO
// NOVO COMPOSABLE: O Medidor em Arco
@Composable
fun TuningMeterArc(detectedFrequency: Float, targetFrequency: Float, targetNote: String) {
    // ... (a lógica de 'cents', 'angle', etc., continua a mesma)
    val cents = if (detectedFrequency > 0f && targetFrequency > 0f) {
        (1200 * log2(detectedFrequency / targetFrequency)).toFloat()
    } else {
        0f
    }
    val clampedCents = cents.coerceIn(-50f, 50f)
    val angle: Float by animateFloatAsState(
        targetValue = (clampedCents / 50f) * 60f,
        animationSpec = tween(durationMillis = 300)
    )
    val isInTune = abs(cents) < 5f

    // --- CORREÇÃO: CAPTURAMOS AS CORES DO TEMA AQUI ---
    // Fazemos isso no escopo @Composable, ANTES do Canvas.
    val indicatorColor =
        if (isInTune) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    // ----------------------------------------------------


    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        // O painel de texto no centro (usa 'indicatorColor' que já capturamos)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = targetNote,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                color = indicatorColor
            )
            Text(
                text = "${detectedFrequency.roundToInt()} Hz / ${targetFrequency.roundToInt()} Hz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Aqui podemos acessar diretamente, pois Text é @Composable
            )
            Spacer(modifier = Modifier.height(8.dp))
            val statusText = when {
                isInTune -> "Afinado!"
                cents < -5f -> "Aperte"
                cents > 5f -> "Afrouxe"
                else -> "..."
            }
            Text(statusText, style = MaterialTheme.typography.titleLarge, color = indicatorColor)
        }

        // O Canvas que desenha o arco e a agulha
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcSize = size.width * 0.7f
            val strokeWidth = 25f
            val topLeft = Offset((size.width - arcSize) / 2, (size.height - arcSize) / 2)

            // Arco de fundo - AGORA USANDO A VARIÁVEL 'surfaceColor'
            drawArc(
                color = surfaceColor, // <-- CORREÇÃO
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val successGreen = Color(0xFF34C759) // O verde padrão do sistema iOS

            // Gradiente de cor para o arco principal
            val brush = Brush.sweepGradient(
                center = center,
                colorStops = arrayOf(
                    // O gradiente completo de 360°
                    0.0f to Color.Red,
                    0.416f to Color.Red,
                    0.583f to Color.Yellow,
                    0.75f to successGreen,
                    0.916f to Color.Yellow,
                    1.0f to Color.Red
                )
            )

            // Arco colorido
            drawArc(
                brush = brush,
                alpha = 0.6f,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Desenha o ponteiro - USA A VARIÁVEL 'indicatorColor'
            rotate(degrees = angle, pivot = center) {
                drawLine(
                    color = indicatorColor, // <-- CORREÇÃO
                    start = Offset(center.x, center.y),
                    end = Offset(center.x, topLeft.y - 10),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
            }

            // Pivô do ponteiro - USA AS VARIÁVEIS 'indicatorColor' E 'backgroundColor'
            drawCircle(color = indicatorColor, radius = 12f, center = center) // <-- CORREÇÃO
            drawCircle(color = backgroundColor, radius = 6f, center = center) // <-- CORREÇÃO
        }
    }
}