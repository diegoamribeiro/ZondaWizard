package com.dmribeiro.zondatuner.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
    var showFavoriteIcon by mutableStateOf(false)
    var title by mutableStateOf<String?>(null)
}

@ExperimentalMaterial3Api
@Composable
fun AppTopBarComponent(
    appTopBarState: AppTopBarComponentState = AppTopBarComponentState(),
    onBackButtonClick: () -> Unit = {},
    onMenuButtonClick: () -> Unit = {}
) {
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
                icon = Icons.AutoMirrored.Filled.ArrowBack
            )
        }

        Text(
            text = appTopBarState.title ?: "",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .weight(1f) // 🔹 Faz o título ocupar o espaço disponível sem sobrepor o botão de voltar
                .padding(start = if (appTopBarState.showBackButton) 8.dp else 0.dp)
        )

        if (appTopBarState.showMenuButton) {
            AppTopBarIconButton(
                onClick = onMenuButtonClick,
                icon = Icons.Filled.MoreVert
            )
        }
    }
}

@Composable
private fun AppTopBarIconButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}


@Composable
private fun AppTopBarTitle(title: String, showFavoriteIcon: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            title, style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        )
        if (showFavoriteIcon) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
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
                showFavoriteIcon = true
                title = "Title"
            },
            onBackButtonClick = {},
            onMenuButtonClick = {}
        )
    }
}