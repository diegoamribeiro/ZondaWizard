package com.dmribeiro.zondatuner

import androidx.compose.material.Surface
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import com.dmribeiro.zondatuner.navigation.AppDestination
import com.dmribeiro.zondatuner.presentation.ui.MainScreen
import com.dmribeiro.zondatuner.theme.ZondaTunerTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

@Composable
@Preview
fun App() {
    KoinContext{
        ZondaTunerTheme {
            Surface {
                MainScreen()
            }
        }
    }
}