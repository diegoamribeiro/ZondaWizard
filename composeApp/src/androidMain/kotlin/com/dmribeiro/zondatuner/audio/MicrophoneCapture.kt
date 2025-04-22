package com.dmribeiro.zondatuner.audio

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import kotlinx.coroutines.*

actual class MicrophoneCapture actual constructor(
    private val onFrequencyDetected: (Float) -> Unit
) {
    private var dispatcher: AudioDispatcher? = null
    private var job: Job? = null
    private var frequencyAudioProcessor: FrequencyAudioProcessor? = null

    actual fun start() {
        job = CoroutineScope(Dispatchers.Default).launch {
            try {
                dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                    SAMPLE_RATE, BUFFER_SIZE, OVERLAP
                )

                frequencyAudioProcessor = FrequencyAudioProcessor { frequency ->
                    println("📡 Frequência Detectada: $frequency Hz")
                    onFrequencyDetected(frequency)
                }

                dispatcher?.addAudioProcessor(frequencyAudioProcessor!!)
                dispatcher?.run()
            } catch (e: Exception) {
                println("Erro ao capturar áudio: ${e.message}")
            }
        }
    }

    actual fun stop() {
        job?.cancel()
        dispatcher?.stop()
        dispatcher = null
        frequencyAudioProcessor = null
    }

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
        private const val OVERLAP = 1024
    }
}