package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.dmribeiro.zondatuner.utils.dismissKeyboardLambda
import com.dmribeiro.zondatuner.utils.formatFrequency
import kotlin.math.pow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun CreateTuningScreenContent(
    existingTuning: TuningDataUi? = null,
    onBack: () -> Unit,
    onSave: (TuningDataUi) -> Unit
) {
    var name by remember { mutableStateOf(existingTuning?.name ?: "") }
    var description by remember { mutableStateOf(existingTuning?.description ?: "") }

    var stringsState by remember {
        mutableStateOf(
            existingTuning?.strings?.map { it.copy() } ?: defaultStrings()
        )
    }

    val notes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val dismissKeyboard = dismissKeyboardLambda()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                dismissKeyboard()
            }
            .padding(16.dp)
    ) {
        Text(if (existingTuning == null) "Criar Nova Afinação" else "Editar Afinação", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome da Afinação") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
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
                val guitarString = stringsState[index]
                TuningStringRow(
                    stringNumber = guitarString.number,
                    selectedNote = guitarString.note,
                    octaveShift = guitarString.octaveShift,
                    frequency = guitarString.frequency,
                    notes = notes,
                    onNoteChange = { newNote ->
                        stringsState = stringsState.toMutableList().apply {
                            set(index, get(index).copy(
                                note = newNote,
                                frequency = noteToFrequency(newNote, guitarString.number, get(index).octaveShift)
                            ))
                        }
                    },
                    onOctaveChange = { newShift ->
                        stringsState = stringsState.toMutableList().apply {
                            set(index, get(index).copy(
                                octaveShift = newShift,
                                frequency = noteToFrequency(get(index).note, guitarString.number, newShift)
                            ))
                        }
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
                        name = name,
                        description = description,
                        strings = stringsState
                    )
                    onSave(newTuning)
                    onBack()
                }
            ) {
                Text(if (existingTuning == null) "Salvar" else "Atualizar")
            }
        }
    }
}

fun defaultStrings(): List<GuitarString> = listOf(
    GuitarString(6, 82.41f, "E", 0),
    GuitarString(5, 110.00f, "A", 0),
    GuitarString(4, 146.83f, "D", 0),
    GuitarString(3, 196.00f, "G", 0),
    GuitarString(2, 246.94f, "B", 0),
    GuitarString(1, 329.63f, "E", 0)
)

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

    val standardEntry = standardTuning.entries.find { it.key.first == stringNumber } ?: return 0f

    val baseNote = standardEntry.key.second
    val baseFrequency = standardEntry.value

    val semitoneDifference = baseFrequencies.keys.indexOf(note) - baseFrequencies.keys.indexOf(baseNote)

    val adjustedFrequency = baseFrequency * (2.0.pow(semitoneDifference / 12.0)).toFloat()

    return adjustedFrequency * (2.0.pow(octaveShift)).toFloat()
}