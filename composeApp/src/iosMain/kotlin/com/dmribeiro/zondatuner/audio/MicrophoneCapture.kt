package com.dmribeiro.zondatuner.audio

actual class MicrophoneCapture actual constructor(
    private val onFrequencyDetected: (Float) -> Unit
) {
    private val audioProcessor: FrequencyAudioProcessor = FrequencyAudioProcessor(onFrequencyDetected)

    actual fun start() {
        println("***MicrophoneCapture started")
        audioProcessor.start()
    }

    actual fun stop() {
        println("MicrophoneCapture stopped")
        audioProcessor.stop()
    }
}