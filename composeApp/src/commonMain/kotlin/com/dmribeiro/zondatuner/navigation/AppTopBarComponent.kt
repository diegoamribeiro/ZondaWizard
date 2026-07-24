package com.dmribeiro.zondatuner.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dmribeiro.zondatuner.theme.ZondaTunerTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

class AppTopBarComponentState {
    var showBackButton by mutableStateOf(false)
    var showMenuButton by mutableStateOf(false)
    var title by mutableStateOf<String?>(null)
}

@ExperimentalMaterial3Api
@Composable
fun AppTopBarComponent(
    appTopBarState: AppTopBarComponentState = AppTopBarComponentState(),
    onBackButtonClick: () -> Unit = {},
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (appTopBarState.showBackButton) {
            AppTopBarIconButton(
                onClick = onBackButtonClick,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar"
            )
        }

        Text(
            text = appTopBarState.title ?: "",
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = if (appTopBarState.showBackButton) 8.dp else 0.dp)
        )

        if (appTopBarState.showMenuButton) {
            Box {
                AppTopBarIconButton(
                    onClick = { menuExpanded = true },
                    icon = Icons.Filled.MoreVert,
                    contentDescription = "Mais opções"
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    onEditClick?.let { onEdit ->
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                    }
                    onDeleteClick?.let { onDelete ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Apagar",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@ExperimentalMaterial3Api
@Composable
@Preview
private fun AppTopBarComponent_Preview() {
    ZondaTunerTheme {
        AppTopBarComponent(
            appTopBarState = AppTopBarComponentState().apply {
                showBackButton = true
                showMenuButton = true
                title = "Standard"
            },
            onBackButtonClick = {},
            onEditClick = {},
            onDeleteClick = {},
        )
    }
}
