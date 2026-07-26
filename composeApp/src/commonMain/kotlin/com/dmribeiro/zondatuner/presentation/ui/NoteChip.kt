package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dmribeiro.zondatuner.domain.model.GuitarString

/** Chip pill com a nota de uma corda — identidade visual das afinações. */
@Composable
fun NoteChip(note: String, modifier: Modifier = Modifier) {
    Text(
        text = note,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Linha com os chips das 6 cordas (da mais grave para a mais aguda). */
@Composable
fun TuningNotesRow(strings: List<GuitarString>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        strings.forEach { guitarString ->
            NoteChip(note = guitarString.note)
        }
    }
}
