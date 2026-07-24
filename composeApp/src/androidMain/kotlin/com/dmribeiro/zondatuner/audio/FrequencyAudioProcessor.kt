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
        PitchDetectionHandler { result, event ->
            val frequency = result.pitch
            // Filtra estimativas pouco confiáveis (ruído, harmônicos, silêncio),
            // que são a principal causa do ponteiro tremer.
            val isReliable = frequency in MIN_FREQUENCY..MAX_FREQUENCY &&
                result.probability >= MIN_PROBABILITY &&
                event.rms >= MIN_RMS
            if (isReliable) {
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

        // Confiança mínima do YIN (0..1). Sinal limpo fica acima de 0.9.
        private const val MIN_PROBABILITY = 0.85f

        // Gate de volume: abaixo disso é ruído de fundo/silêncio.
        private const val MIN_RMS = 0.003

        // Faixa útil de um violão/guitarra (inclui 12ª casa da corda mais aguda).
        private const val MIN_FREQUENCY = 40f
        private const val MAX_FREQUENCY = 1500f
    }

    actual fun start() {
    }

    actual fun stop() {
    }
}