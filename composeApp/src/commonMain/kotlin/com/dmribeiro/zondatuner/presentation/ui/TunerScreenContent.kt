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
    var detectedFrequency by remember { mutableStateOf(0f) }
    var selectedStringIndex by remember { mutableStateOf(6) } // 🔹 Começa corretamente na 6ª corda
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 🔹 Busca a corda correta com base no número e não no índice
    val selectedString = tuning.getGuitarStrings().find { it.number == selectedStringIndex }
        ?: tuning.getGuitarStrings().first()

    val targetFrequency = selectedString.frequency
    val targetNote = selectedString.note

    val audioProcessor = remember {
        MicrophoneCapture { frequency ->
            detectedFrequency = frequency
        }
    }

    DisposableEffect(Unit) {
        audioProcessor.start()
        onDispose { audioProcessor.stop() }
    }

    // 🔹 Permite rolar a tela caso os itens não caibam na tela do dispositivo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray300) // ✅ Agora o fundo se adapta corretamente ao tema escuro
            .padding(top = 0.dp, end = 16.dp, bottom = 16.dp, start = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔹 Exibe as cordas e permite a seleção correta
        GuitarStringsSelector(
            tuning = tuning,
            onStringSelected = { selectedStringIndex = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 🔹 Exibe o medidor com a nota correta
        TunerMeter(
            detectedFrequency = detectedFrequency,
            targetFrequency = targetFrequency,
            note = targetNote
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Mantém "Frequência Detectada" estático e altera apenas o valor dinâmico
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Frequência Detectada: ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White // ✅ Agora o texto se adapta ao tema escuro
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${detectedFrequency.roundToInt()} Hz",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Cyan // ✅ Mantém destaque
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