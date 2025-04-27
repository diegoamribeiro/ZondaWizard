package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmribeiro.zondatuner.audio.MicrophoneCapture
import com.dmribeiro.zondatuner.domain.model.Tuning
import com.dmribeiro.zondatuner.permissions.getPermissionHandler
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel
import com.dmribeiro.zondatuner.theme.Gray100
import com.dmribeiro.zondatuner.theme.Gray300
import com.dmribeiro.zondatuner.theme.Gray700
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun TunerScreenContent(
    onBack: () -> Unit,
    tuning: TuningDataUi
) {
    var permissionGranted by remember { mutableStateOf(false) }
    val permissionHandler = getPermissionHandler()

    // 🔹 Solicita permissão ao iniciar a tela
    LaunchedEffect(Unit) {
        permissionHandler.requestAudioPermission { granted ->
            permissionGranted = granted
        }
    }

    if (permissionGranted) {
        TunerScreenWithAudio(onBack, tuning)
    } else {
        PermissionRequestScreen(onRequestPermission = {
            permissionHandler.requestAudioPermission { granted ->
                permissionGranted = granted
            }
        })
    }
}

// 🔹 Tela para exibir aviso e solicitar permissão do microfone
@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Permissão para acessar o microfone é necessária!")
        Spacer(modifier = Modifier.height(16.dp))
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
    // ------------------------------------------------------------------
    // 1. Frequência bruta que vem do MicrophoneCapture
    // ------------------------------------------------------------------
    var detectedFrequency by remember { mutableStateOf(0f) }

    // 🔵 NOVO  : buffer circular para suavizar
    val freqBuffer = remember { mutableStateListOf<Float>() }

    // ------------------------------------------------------------------
    // 2. Cordas / nota alvo (seu código original)
    // ------------------------------------------------------------------
    var selectedStringIndex by remember { mutableStateOf(6) }
    var showDeleteDialog   by remember { mutableStateOf(false) }

    val selectedString = tuning.getGuitarStrings()
        .find { it.number == selectedStringIndex }
        ?: tuning.getGuitarStrings().first()

    val targetFrequency = selectedString.frequency
    val targetNote      = selectedString.note

    // ------------------------------------------------------------------
    // 3. Captura de áudio  (inalterado)
    // ------------------------------------------------------------------
    val audioProcessor = remember {
        MicrophoneCapture { freq -> detectedFrequency = freq }   // ← bruto
    }

    DisposableEffect(Unit) {
        audioProcessor.start()
        onDispose { audioProcessor.stop() }
    }

    // ------------------------------------------------------------------
    // 4. ATUALIZA BUFFER  (executa sempre que chegar um valor novo)
    // ------------------------------------------------------------------
    LaunchedEffect(detectedFrequency) {
        if (detectedFrequency > 0) {           // ignora zeros
            if (freqBuffer.size >= 5) freqBuffer.removeFirst()
            freqBuffer.add(detectedFrequency)
        }
    }

    // 🔵 NOVO : frequência SUAVIZADA
    val smoothFrequency =
        if (freqBuffer.isNotEmpty()) freqBuffer.average().toFloat() else 0f
    // ------------------------------------------------------------------

    /* --------------------------- UI ------------------------------- */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray300)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        GuitarStringsSelector(
            tuning = tuning,
            onStringSelected = { selectedStringIndex = it }
        )

        Spacer(Modifier.height(10.dp))

        /* ---------- usa smoothFrequency em vez de detectedFrequency ---- */
        TunerMeter(
            detectedFrequency = smoothFrequency,
            targetFrequency   = targetFrequency,
            note              = targetNote
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text       = "Frequência Detectada:",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text       = "${smoothFrequency.roundToInt()} Hz",   // aqui tb
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.Cyan
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("Apagar", color = Color.White)
            }
            Button(onClick = onBack) {
                Text("Voltar")
            }
        }
    }

    // 🔹 Diálogo de Confirmação para deletar afinação
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Apagar Afinação") },
            text = { Text("Tem certeza que deseja apagar '${tuning.name}'?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeTuning(tuning.id)
                        onBack()
                    }
                ) {
                    Text("Sim")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview
@Composable
fun PreviewTunerScreenContent() {
    TunerScreenWithAudio(
        onBack = { /* handle onBack callback */ },
        tuning = TuningDataUi(
            id = 1,
            name = "Standard",
            description = "Description",
            strings = listOf()
        )
    )
}