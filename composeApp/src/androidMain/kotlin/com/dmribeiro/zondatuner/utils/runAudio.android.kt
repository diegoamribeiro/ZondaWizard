package com.dmribeiro.zondatuner.utils

import android.content.Context
import korlibs.io.android.withAndroidContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AndroidAudioContext {
    var applicationContext: Context? = null
}

private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

actual fun runAudio(block: suspend CoroutineScope.() -> Unit) {
    val ctx = AndroidAudioContext.applicationContext
        ?: error("AndroidAudioContext not initialised")
    audioScope.launch {
        withAndroidContext(ctx) {
            block()
        }
    }
}
