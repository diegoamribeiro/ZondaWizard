package com.dmribeiro.zondatuner.audio


import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor

actual class FrequencyAudioProcessor actual constructor(
    private val onFrequencyDetected: (Float) -> Unit
) : AudioProcessor {

    private val pitchProcessor = PitchProcessor(
        PitchProcessor.PitchEstimationAlgorithm.YIN,
        SAMPLE_RATE.toFloat(),
        BUFFER_SIZE,
        PitchDetectionHandler { result, _ ->
            val frequency = result.pitch
            if (frequency > 0) {
                onFrequencyDetected(frequency)
            }
        }
    )

    override fun process(audioEvent: AudioEvent?): Boolean {
        if (audioEvent != null) {
            return pitchProcessor.process(audioEvent)
        }
        return false
    }

    override fun processingFinished() {
        pitchProcessor.processingFinished()
    }

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
        private const val OVERLAP = 1024
    }

    actual fun start() {
    }

    actual fun stop() {
    }
}