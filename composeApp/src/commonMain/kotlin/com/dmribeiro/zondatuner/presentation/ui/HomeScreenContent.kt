package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.dmribeiro.zondatuner.navigation.AppDestination
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    homeScreenModel: HomeScreenModel
) {
    val state by homeScreenModel.tuningState.listState.collectAsState()
    val navigator = LocalNavigator.current

    // CORREÇÃO: Scaffold agora é do Material 3
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Afinações", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            // CORREÇÃO: FloatingActionButton agora é do Material 3
            FloatingActionButton(
                onClick = { navigator?.push(AppDestination.CreateTuningScreen()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar afinação")
            }
        },
        // O container principal já pega a cor de fundo do tema
        // não precisamos mais de uma Column com .background()
    ) { paddingValues ->

        // O conteúdo principal
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Respeita os insets do Scaffold (top bar, etc)
                .padding(horizontal = 16.dp), // Adiciona padding horizontal geral
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp) // Espaçamento no topo e base da lista
        ) {
            if (state.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center){
                        Text("Nenhuma afinação encontrada", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(state) { tuning ->
                    HomeListItem(
                        tuning = tuning,
                        onSelect = { navigator?.push(AppDestination.TunerScreen(tuning)) },
                        onEdit   = { navigator?.push(AppDestination.CreateTuningScreen(existingTuning = tuning)) }
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
    // CORREÇÃO: Card agora é do Material 3 e usa cores e formas do tema
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                // CORREÇÃO: Textos usam tipografia e cores do tema
                Text(
                    text = tuning.name,
                    style = MaterialTheme.typography.titleLarge
                )
                if (tuning.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tuning.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // CORREÇÃO: IconButton agora é do Material 3
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Afinação",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Cor sutil para o ícone
                )
            }
        }
    }
}