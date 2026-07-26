package com.dmribeiro.zondatuner

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.dmribeiro.zondatuner.presentation.ui.MainScreen
import com.dmribeiro.zondatuner.theme.ZondaTunerTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

@Composable
@Preview
fun App() {
    KoinContext {
        ZondaTunerTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                MainScreen()
            }
        }
    }
}
