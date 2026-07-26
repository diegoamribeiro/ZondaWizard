package com.dmribeiro.zondatuner.audio

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor

actual class FrequencyAudioProcessor actual constructor(
    private val onFrequencyDetected: (Float) -> Unit,
) : AudioProcessor {

    private val yinDetector = YinPitchDetector(
        sampleRate = SAMPLE_RATE.toFloat(),
        threshold = PitchDetectionConfig.YIN_THRESHOLD,
        minFrequency = PitchDetectionConfig.MIN_FREQUENCY,
        maxFrequency = PitchDetectionConfig.MAX_FREQUENCY,
    )

    override fun process(audioEvent: AudioEvent?): Boolean {
        if (audioEvent == null) return false

        if (audioEvent.rms < PitchDetectionConfig.MIN_RMS) return true

        val buffer = audioEvent.floatBuffer
        val size = audioEvent.bufferSize
        if (size < 4) return true

        val samples = FloatArray(size) { buffer[it] }
        val frequency = yinDetector.detectPitch(samples)
        if (frequency in PitchDetectionConfig.MIN_FREQUENCY..PitchDetectionConfig.MAX_FREQUENCY) {
            onFrequencyDetected(frequency)
        }
        return true
    }

    override fun processingFinished() = Unit

    actual fun start() = Unit

    actual fun stop() = Unit

    companion object {
        private const val SAMPLE_RATE = 44100
    }
}
