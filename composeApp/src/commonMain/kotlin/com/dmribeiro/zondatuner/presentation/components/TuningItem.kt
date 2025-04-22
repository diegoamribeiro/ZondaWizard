package com.dmribeiro.zondatuner.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi

@Composable
fun TuningItem(tuning: TuningDataUi, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = tuning.name, style = MaterialTheme.typography.h4)
            Text(text = tuning.name, style = MaterialTheme.typography.h6)
            Button(onClick = onDelete, modifier = Modifier.align(Alignment.End)) {
                Text("Excluir")
            }
        }
    }
}