package com.dmribeiro.zondatuner.audio

import kotlinx.coroutines.flow.MutableStateFlow

expect class MicrophoneCapture(onFrequencyDetected: (Float) -> Unit) {
    fun start()
    fun stop()
}