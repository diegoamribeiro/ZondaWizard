package com.dmribeiro.zondatuner.audio

expect class FrequencyAudioProcessor(onFrequencyDetected: (Float) -> Unit) {
    fun start()
    fun stop()
}