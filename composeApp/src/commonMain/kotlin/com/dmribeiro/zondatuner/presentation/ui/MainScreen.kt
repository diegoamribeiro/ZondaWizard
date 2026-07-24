package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.dmribeiro.zondatuner.navigation.AppDestination
import com.dmribeiro.zondatuner.navigation.AppTopBarComponent
import com.dmribeiro.zondatuner.navigation.LocalTopBarMenuActions
import com.dmribeiro.zondatuner.navigation.TopBarMenuActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val menuActions = remember { TopBarMenuActions() }

    CompositionLocalProvider(LocalTopBarMenuActions provides menuActions) {
        Navigator(screen = AppDestination.SplashScreen) { navigator ->
            val currentScreen = navigator.lastItem as? AppDestination

            Scaffold(
                topBar = {
                    if (currentScreen !is AppDestination.SplashScreen) {
                        currentScreen?.let {
                            AppTopBarComponent(
                                appTopBarState = it.topBarConfig,
                                onBackButtonClick = { navigator.pop() },
                                onEditClick = menuActions.onEdit,
                                onDeleteClick = menuActions.onDelete,
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    currentScreen?.Content()
                }
            }
        }
    }
}
