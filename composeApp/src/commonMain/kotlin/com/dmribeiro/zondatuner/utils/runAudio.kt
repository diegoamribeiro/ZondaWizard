package com.dmribeiro.zondatuner.utils

import kotlinx.coroutines.CoroutineScope

expect fun runAudio(block: suspend CoroutineScope.() -> Unit)
