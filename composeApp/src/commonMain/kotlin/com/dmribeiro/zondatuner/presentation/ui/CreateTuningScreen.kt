package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel
import com.dmribeiro.zondatuner.utils.formatFrequency
import org.koin.compose.koinInject
import kotlin.math.pow

@Composable
fun CreateTuningScreenContent(
    existingTuning: TuningDataUi? = null, // 🔹 Se for null, criamos uma nova afinação
    onBack: () -> Unit,
    onSave: (TuningDataUi) -> Unit
) {

    var name by remember { mutableStateOf(existingTuning?.name) }
    var description by remember { mutableStateOf(existingTuning?.description) }
    var stringNotes by remember { mutableStateOf(existingTuning?.strings?.map { it.note } ?: listOf("E", "A", "D", "G", "B", "E")) }
    var octaveShifts by remember { mutableStateOf(existingTuning?.strings?.map { 0 } ?: List(6) { 0 }) }

    val notes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(if (existingTuning == null) "Criar Nova Afinação" else "Editar Afinação", style = MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name ?: "",
            onValueChange = { name = it },
            label = { Text("Nome da Afinação") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description ?: "",
            onValueChange = { description = it },
            label = { Text("Descrição (Opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(6) { index ->
                TuningStringRow(
                    stringNumber = 6 - index,
                    selectedNote = stringNotes[index],
                    octaveShift = octaveShifts[index],
                    frequency = noteToFrequency(stringNotes[index], 6 - index, octaveShifts[index]),
                    notes = notes,
                    onNoteChange = { newNote ->
                        stringNotes = stringNotes.toMutableList().apply { set(index, newNote) }
                    },
                    onOctaveChange = { shift ->
                        octaveShifts = octaveShifts.toMutableList().apply { set(index, shift) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    val newTuning = TuningDataUi(
                        id = existingTuning?.id ?: 0,
                        name = name ?: "No name",
                        description = description ?: "No description",
                        strings = stringNotes.mapIndexed { index, note ->
                            GuitarString(
                                number = 6 - index,
                                frequency = noteToFrequency(note, 6 - index, octaveShifts[index]),
                                note = note
                            )
                        }
                    )
                    onSave(newTuning) // 🔹 Chama a ação correta (criação ou edição)
                    onBack()
                }
            ) {
                Text(if (existingTuning == null) "Salvar" else "Atualizar")
            }
        }
    }
}

// 🔹 Componente otimizado para cada corda
@Composable
fun TuningStringRow(
    stringNumber: Int,
    selectedNote: String,
    octaveShift: Int,
    frequency: Float,
    notes: List<String>,
    onNoteChange: (String) -> Unit,
    onOctaveChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .background(Color.LightGray)
            .border(1.dp, Color.Gray)
            .horizontalScroll(rememberScrollState()), // 🔹 Permite rolar horizontalmente se necessário
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Corda $stringNumber",
                style = MaterialTheme.typography.body2,
                color = Color.Red,
                modifier = Modifier.padding(2.dp)
            )

            Text(
                text = frequency.toString().formatFrequency(), // 🔹 Agora a frequência está correta e formatada!
                style = MaterialTheme.typography.body2,
                color = Color.Blue,
                modifier = Modifier.padding(2.dp)
            )
        }

        // 🔹 Dropdown para seleção da nota
        DropdownMenuComponent(
            label = "Nota",
            selectedValue = selectedNote,
            options = notes,
            onSelect = onNoteChange
        )

        // 🔹 Controles de oitava organizados (MANTIDOS COMO ESTAVAM)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onOctaveChange(octaveShift - 1) },
                modifier = Modifier,
                shape = CircleShape
            ) {
                Text("🔼", style = MaterialTheme.typography.body2, color = Color.White)
            }

            Text(
                text = "Oitava: $octaveShift",
                fontSize = 12.sp,
                color = Color.Black,
                modifier = Modifier
                    .background(Color.LightGray)
                    .padding(2.dp)
            )

            Button(
                onClick = { onOctaveChange(octaveShift + 1) },
                modifier = Modifier,
                shape = CircleShape
            ) {
                Text("🔽", style = MaterialTheme.typography.body2, color = Color.White)
            }
        }
    }
}

@Composable
fun DropdownMenuComponent(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,

            ) {
            Button(onClick = { expanded = true }, shape = CircleShape) {
                Text(selectedValue)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { note ->
                    DropdownMenuItem(onClick = {
                        onSelect(note)
                        expanded = false
                    }) {
                        Text(note)
                    }
                }
            }
        }
    }
}

fun noteToFrequency(note: String, stringNumber: Int, octaveShift: Int = 0): Float {
    val baseFrequencies = mapOf(
        "C" to 16.35f, "C#" to 17.32f, "D" to 18.35f, "D#" to 19.45f, "E" to 20.60f,
        "F" to 21.83f, "F#" to 23.12f, "G" to 24.50f, "G#" to 25.96f, "A" to 27.50f,
        "A#" to 29.14f, "B" to 30.87f
    )

    // 🔹 Frequências padrão das cordas na afinação tradicional (EADGBE)
    val standardTuning = mapOf(
        6 to "E" to 82.41f,
        5 to "A" to 110.00f,
        4 to "D" to 146.83f,
        3 to "G" to 196.00f,
        2 to "B" to 246.94f,
        1 to "E" to 329.63f
    )

    // 🔹 Pega a frequência base da corda na afinação padrão
    val standardEntry = standardTuning.entries.find { it.key.first == stringNumber }
        ?: return 0f

    val baseNote = standardEntry.key.second // Nota padrão da corda
    val baseFrequency = standardEntry.value // Frequência padrão da corda

    // 🔹 Calcula a diferença de semitons entre a nova nota e a nota padrão da corda
    val semitoneDifference = baseFrequencies.keys.indexOf(note) - baseFrequencies.keys.indexOf(baseNote)

    // 🔹 Aplica a transformação para mudar a nota
    val adjustedFrequency = baseFrequency * (2.0.pow(semitoneDifference / 12.0)).toFloat()

    // 🔹 Aplica o shift da oitava
    return adjustedFrequency * (2.0.pow(octaveShift)).toFloat()
}