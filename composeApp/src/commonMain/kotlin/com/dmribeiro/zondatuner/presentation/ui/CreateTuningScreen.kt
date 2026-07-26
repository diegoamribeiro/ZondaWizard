package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.utils.chromaticIndex
import com.dmribeiro.zondatuner.utils.chromaticNotes
import com.dmribeiro.zondatuner.utils.dismissKeyboardLambda
import com.dmribeiro.zondatuner.utils.formatFrequency
import com.dmribeiro.zondatuner.utils.normalizeNote
import kotlin.math.pow
import org.jetbrains.compose.resources.painterResource
import zondawizard.composeapp.generated.resources.Res
import zondawizard.composeapp.generated.resources.icon_minus_line

@Composable
fun CreateTuningScreenContent(
    existingTuning: TuningDataUi? = null,
    onBack: () -> Unit,
    onSave: (TuningDataUi) -> Unit,
) {
    var name by remember { mutableStateOf(existingTuning?.name ?: "") }
    var description by remember { mutableStateOf(existingTuning?.description ?: "") }
    var stringsState by remember {
        mutableStateOf(existingTuning?.strings?.map { it.copy() } ?: defaultStrings())
    }
    val notes = chromaticNotes()
    val dismissKeyboard = dismissKeyboardLambda()

    val onNoteChange: (Int, String) -> Unit = { stringNumber, newNote ->
        stringsState = stringsState.map {
            if (it.number == stringNumber) {
                it.copy(
                    note = newNote,
                    frequency = noteToFrequency(newNote, it.number, it.octaveShift),
                )
            } else {
                it
            }
        }
    }

    val onOctaveChange: (Int, Int) -> Unit = { stringNumber, newShift ->
        stringsState = stringsState.map {
            if (it.number == stringNumber) {
                it.copy(
                    octaveShift = newShift,
                    frequency = noteToFrequency(it.note, it.number, newShift),
                )
            } else {
                it
            }
        }
    }

    val onSaveClick = {
        val newTuning = TuningDataUi(
            id = existingTuning?.id ?: 0,
            name = name,
            description = description,
            strings = stringsState,
        )
        onSave(newTuning)
        onBack()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { dismissKeyboard() },
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                LandscapeCreateTuningLayout(
                    name = name,
                    onNameChange = { name = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    stringsState = stringsState,
                    notes = notes,
                    onNoteChange = onNoteChange,
                    onOctaveChange = onOctaveChange,
                    isEditing = existingTuning != null,
                    onBack = onBack,
                    onSave = onSaveClick,
                )
            } else {
                PortraitCreateTuningLayout(
                    name = name,
                    onNameChange = { name = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    stringsState = stringsState,
                    notes = notes,
                    onNoteChange = onNoteChange,
                    onOctaveChange = onOctaveChange,
                    isEditing = existingTuning != null,
                    onBack = onBack,
                    onSave = onSaveClick,
                )
            }
        }
    }
}

@Composable
private fun PortraitCreateTuningLayout(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    stringsState: List<GuitarString>,
    notes: List<String>,
    onNoteChange: (Int, String) -> Unit,
    onOctaveChange: (Int, Int) -> Unit,
    isEditing: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))

        CreateTuningMetadataFields(
            name = name,
            onNameChange = onNameChange,
            description = description,
            onDescriptionChange = onDescriptionChange,
        )

        Spacer(modifier = Modifier.height(16.dp))

        TuningNotesRow(
            strings = stringsState,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreateTuningStringsEditor(
            stringsState = stringsState,
            notes = notes,
            compact = false,
            onNoteChange = onNoteChange,
            onOctaveChange = onOctaveChange,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreateTuningFooter(
            isEditing = isEditing,
            onBack = onBack,
            onSave = onSave,
        )
    }
}

@Composable
private fun LandscapeCreateTuningLayout(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    stringsState: List<GuitarString>,
    notes: List<String>,
    onNoteChange: (Int, String) -> Unit,
    onOctaveChange: (Int, Int) -> Unit,
    isEditing: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .weight(0.40f)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                CreateTuningMetadataFields(
                    name = name,
                    onNameChange = onNameChange,
                    description = description,
                    onDescriptionChange = onDescriptionChange,
                )

                Spacer(modifier = Modifier.height(16.dp))

                TuningNotesRow(strings = stringsState)
            }

            CreateTuningStringsEditor(
                stringsState = stringsState,
                notes = notes,
                compact = true,
                onNoteChange = onNoteChange,
                onOctaveChange = onOctaveChange,
                modifier = Modifier
                    .weight(0.60f)
                    .fillMaxHeight(),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        CreateTuningFooter(
            isEditing = isEditing,
            onBack = onBack,
            onSave = onSave,
        )
    }
}

@Composable
private fun CreateTuningMetadataFields(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Nome da Afinação") },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        singleLine = true,
        colors = tuningTextFieldColors(),
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Descrição (Opcional)") },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = tuningTextFieldColors(),
    )
}

@Composable
private fun CreateTuningStringsEditor(
    stringsState: List<GuitarString>,
    notes: List<String>,
    compact: Boolean,
    onNoteChange: (Int, String) -> Unit,
    onOctaveChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(stringsState, key = { it.number }) { guitarString ->
            TuningStringRow(
                guitarString = guitarString,
                notes = notes,
                compact = compact,
                onNoteChange = { newNote -> onNoteChange(guitarString.number, newNote) },
                onOctaveChange = { newShift -> onOctaveChange(guitarString.number, newShift) },
            )
        }
    }
}

@Composable
private fun CreateTuningFooter(
    isEditing: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(onClick = onBack) {
            Text("Cancelar")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onSave) {
            Text(if (isEditing) "Atualizar" else "Salvar")
        }
    }
}

@Composable
private fun tuningTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
fun TuningStringRow(
    guitarString: GuitarString,
    notes: List<String>,
    onNoteChange: (String) -> Unit,
    onOctaveChange: (Int) -> Unit,
    compact: Boolean = false,
) {
    val verticalPadding = if (compact) 8.dp else 12.dp
    val titleStyle = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Corda ${guitarString.number}",
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = guitarString.frequency.toString().formatFrequency(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onOctaveChange(guitarString.octaveShift + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar Oitava")
                }
                Text(
                    text = "${guitarString.octaveShift}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                IconButton(onClick = { onOctaveChange(guitarString.octaveShift - 1) }) {
                    Icon(
                        painterResource(Res.drawable.icon_minus_line),
                        contentDescription = "Diminuir Oitava",
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            DropdownMenuComponent(
                selectedValue = guitarString.note,
                options = notes,
                onSelect = onNoteChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuComponent(
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().width(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { note ->
                DropdownMenuItem(
                    text = { Text(note) },
                    onClick = {
                        onSelect(note)
                        expanded = false
                    },
                )
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
    GuitarString(1, 329.63f, "E", 0),
)

fun noteToFrequency(note: String, stringNumber: Int, octaveShift: Int = 0): Float {
    val normalizedNote = normalizeNote(note)
    val standardTuning = mapOf(
        6 to "E" to 82.41f,
        5 to "A" to 110.00f,
        4 to "D" to 146.83f,
        3 to "G" to 196.00f,
        2 to "B" to 246.94f,
        1 to "E" to 329.63f,
    )
    val standardEntry = standardTuning.entries.find { it.key.first == stringNumber } ?: return 0f
    val baseNote = standardEntry.key.second
    val baseFrequency = standardEntry.value
    val semitoneDifference = chromaticIndex(normalizedNote) - chromaticIndex(baseNote)
    val adjustedFrequency = baseFrequency * (2.0.pow(semitoneDifference / 12.0)).toFloat()
    return adjustedFrequency * (2.0.pow(octaveShift)).toFloat()
}
