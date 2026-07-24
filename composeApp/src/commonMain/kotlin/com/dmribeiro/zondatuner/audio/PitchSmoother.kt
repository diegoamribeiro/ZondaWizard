package com.dmribeiro.zondatuner.audio

import kotlin.math.abs

/**
 * Estabiliza leituras de pitch em duas etapas:
 * 1. Mediana móvel: descarta leituras isoladas (ruído, erros de oitava).
 * 2. Média exponencial adaptativa: suaviza flutuações pequenas sem perder
 *    resposta quando a frequência muda de verdade (ex.: troca de corda).
 */
class PitchSmoother(
    private val medianWindow: Int = 5,
    private val smoothingFactor: Float = 0.2f,
    private val fastJumpRatio: Float = 0.05f,
    private val fastSmoothingFactor: Float = 0.5f,
) {
    private val window = ArrayDeque<Float>()
    private var smoothed = 0f

    fun add(rawFrequency: Float): Float {
        if (rawFrequency <= 0f) return smoothed

        if (window.size >= medianWindow) window.removeFirst()
        window.addLast(rawFrequency)

        val median = window.sorted()[window.size / 2]

        smoothed = if (smoothed <= 0f) {
            median
        } else {
            val relativeChange = abs(median - smoothed) / smoothed
            val alpha =
                if (relativeChange > fastJumpRatio) fastSmoothingFactor else smoothingFactor
            smoothed + alpha * (median - smoothed)
        }
        return smoothed
    }

    fun reset() {
        window.clear()
        smoothed = 0f
    }
}
