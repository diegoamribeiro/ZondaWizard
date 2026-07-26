package com.dmribeiro.zondatuner.audio

/** Limites compartilhados entre Android e iOS para detecção de pitch. */
object PitchDetectionConfig {
    const val MIN_RMS = 0.003f
    const val MIN_FREQUENCY = 40f
    const val MAX_FREQUENCY = 1500f
    const val YIN_THRESHOLD = 0.15f
}
