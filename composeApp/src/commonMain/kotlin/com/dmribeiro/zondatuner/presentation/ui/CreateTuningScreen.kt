package com.dmribeiro.zondatuner.presentation.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.utils.dismissKeyboardLambda
import com.dmribeiro.zondatuner.utils.formatFrequency
import org.jetbrains.compose.resources.painterResource
import zondawizard.composeapp.generated.resources.Res
import zondawizard.composeapp.generated.resources.icon_minus_line
import zondawizard.composeapp.generated.resources.pick_filled_upside_down
import kotlin.math.pow

@Composable
fun CreateTuningScreenContent(
    existingTuning: TuningDataUi? = null,
    onBack: () -> Unit,
    onSave: (TuningDataUi) -> Unit
) {
    var name by remember { mutableStateOf(existingTuning?.name ?: "") }
    var description by remember { mutableStateOf(existingTuning?.description ?: "") }
    var stringsState by remember {
        mutableStateOf(existingTuning?.strings?.map { it.copy() } ?: defaultStrings())
    }
    val notes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val dismissKeyboard = dismissKeyboardLambda()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { dismissKeyboard() },
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = existingTuning?.name ?: "Criar Nova Afinação",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            // CORREÇÃO: Usando OutlinedTextField e TextFieldDefaults do Material 3
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome da Afinação") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stringsState) { guitarString ->
                    TuningStringRow(
                        guitarString = guitarString,
                        notes = notes,
                        onNoteChange = { newNote ->
                            stringsState = stringsState.map {
                                if (it.number == guitarString.number) it.copy(
                                    note = newNote,
                                    frequency = noteToFrequency(newNote, it.number, it.octaveShift)
                                ) else it
                            }
                        },
                        onOctaveChange = { newShift ->
                            stringsState = stringsState.map {
                                if (it.number == guitarString.number) it.copy(
                                    octaveShift = newShift,
                                    frequency = noteToFrequency(it.note, it.number, newShift)
                                ) else it
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onBack) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
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
}

@Composable
fun TuningStringRow(
    guitarString: GuitarString,
    notes: List<String>,
    onNoteChange: (String) -> Unit,
    onOctaveChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Corda ${guitarString.number}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = guitarString.frequency.toString().formatFrequency(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onOctaveChange(guitarString.octaveShift + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar Oitava")
                }
                Text(
                    text = "${guitarString.octaveShift}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                // CORREÇÃO: Ícone de 'Remove' para diminuir
                IconButton(onClick = { onOctaveChange(guitarString.octaveShift - 1) }) {
                    Icon(painterResource(Res.drawable.icon_minus_line), contentDescription = "Diminuir Oitava")
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            DropdownMenuComponent(
                selectedValue = guitarString.note,
                options = notes,
                onSelect = onNoteChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuComponent(
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        // CORREÇÃO: Usando OutlinedTextField e TextFieldDefaults do M3
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().width(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { note ->
                DropdownMenuItem(
                    text = { Text(note) },
                    onClick = {
                        onSelect(note)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ... (suas funções defaultStrings e noteToFrequency permanecem as mesmas)


// ... (suas funções defaultStrings e noteToFrequency permanecem as mesmas)
fun defaultStrings(): List<GuitarString> = listOf(
    GuitarString(6, 82.41f, "E", 0),
    GuitarString(5, 110.00f, "A", 0),
    GuitarString(4, 146.83f, "D", 0),
    GuitarString(3, 196.00f, "G", 0),
    GuitarString(2, 246.94f, "B", 0),
    GuitarString(1, 329.63f, "E", 0)
)
fun noteToFrequency(note: String, stringNumber: Int, octaveShift: Int = 0): Float {
    val baseFrequencies = mapOf(
        "C" to 16.35f, "C#" to 17.32f, "D" to 18.35f, "D#" to 19.45f, "E" to 20.60f,
        "F" to 21.83f, "F#" to 23.12f, "G" to 24.50f, "G#" to 25.96f, "A" to 27.50f,
        "A#" to 29.14f, "B" to 30.87f
    )
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