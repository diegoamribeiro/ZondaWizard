package com.dmribeiro.zondatuner

import androidx.compose.ui.window.ComposeUIViewController
import com.dmribeiro.zondatuner.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}