package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.dmribeiro.zondatuner.navigation.AppDestination
import com.dmribeiro.zondatuner.navigation.AppTopBarComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    Navigator(screen = AppDestination.HomeScreen) { navigator ->
        val currentScreen = navigator.lastItem as? AppDestination

        Scaffold(
            topBar = {
                currentScreen?.let {
                    AppTopBarComponent(
                        appTopBarState = it.topBarConfig,
                        onBackButtonClick = {
                            navigator.pop()
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                currentScreen?.Content()
            }
        }
    }
}