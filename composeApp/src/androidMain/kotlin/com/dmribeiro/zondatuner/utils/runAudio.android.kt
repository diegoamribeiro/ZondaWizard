package com.dmribeiro.zondatuner.utils

import android.content.Context
import korlibs.io.android.AndroidCoroutineContext
import korlibs.io.android.withAndroidContext
import korlibs.io.async.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope


object AndroidAudioContext {
    var applicationContext: Context? = null
}
// Sua função 'actual' que agora encontrará a referência para AndroidAudioContext
@OptIn(DelicateCoroutinesApi::class)
actual fun runAudio(block: suspend CoroutineScope.() -> Unit) {
    val ctx = AndroidAudioContext.applicationContext
        ?: error("AndroidAudioContext not initialised")
    GlobalScope.launch {
        withAndroidContext(ctx) {              // fornece o Context
            block()
        }
    }
}