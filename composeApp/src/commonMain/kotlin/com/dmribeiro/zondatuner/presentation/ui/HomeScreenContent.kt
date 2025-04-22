package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.dmribeiro.zondatuner.AppLogger
import com.dmribeiro.zondatuner.navigation.AppDestination
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel

@Composable
fun HomeScreenContent(
    homeScreenModel: HomeScreenModel
) {
    val state by homeScreenModel.tuningState.listState.collectAsState()
    val navigator = LocalNavigator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Lista de Afinações", style = MaterialTheme.typography.h6)
            Button(onClick = { navigator?.push(AppDestination.CreateTuningScreen()) }) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isEmpty()) {
            Text("Nenhuma afinação encontrada")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state) { tuning ->
                    HomeListItem(
                        tuning = tuning,
                        onSelect = { navigator?.push(AppDestination.TunerScreen(tuning)) },
                        onEdit = { navigator?.push(AppDestination.CreateTuningScreen(existingTuning = tuning)) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeListItem(
    tuning: TuningDataUi,
    onSelect: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f) // ✅ Ocupa o espaço disponível antes do botão de edição
            ) {
                Text(
                    text = tuning.name,
                    fontSize = 18.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                AppLogger.d("***DESCRIPTION:", "${tuning.description}" )
                Text(
                    text = tuning.description,
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onEdit) { // ✅ Clique no botão de edição leva para a tela de edição
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
            }
        }
    }

}