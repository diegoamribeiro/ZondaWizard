package com.dmribeiro.zondatuner.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

actual fun runAudio(block: suspend CoroutineScope.() -> Unit) {
    audioScope.launch(block = block)
}
