package com.dmribeiro.zondatuner.audio

import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.utils.StringMatch
import com.dmribeiro.zondatuner.utils.findBestStringMatch

/**
 * Estabiliza a auto-seleção de corda com hysteresis — evita flicker entre palhetas.
 */
class StringAutoDetector(
    private val stableReadingsRequired: Int = 4,
    private val fastSwitchCentsDelta: Float = 25f,
) {
    private var pendingMatch: StringMatch? = null
    private var pendingCount = 0
    private var activeMatch: StringMatch? = null

    fun evaluate(detectedHz: Float, strings: List<GuitarString>): StringMatch? {
        if (detectedHz <= 0f) return activeMatch

        val candidate = findBestStringMatch(detectedHz, strings) ?: run {
            pendingMatch = null
            pendingCount = 0
            return activeMatch
        }

        val current = activeMatch
        if (current != null &&
            current.stringNumber == candidate.stringNumber &&
            current.isTwelfthFret == candidate.isTwelfthFret
        ) {
            return current
        }

        if (current != null && current.cents - candidate.cents > fastSwitchCentsDelta) {
            activeMatch = candidate
            pendingMatch = null
            pendingCount = 0
            return candidate
        }

        if (pendingMatch?.stringNumber == candidate.stringNumber &&
            pendingMatch?.isTwelfthFret == candidate.isTwelfthFret
        ) {
            pendingCount++
        } else {
            pendingMatch = candidate
            pendingCount = 1
        }

        if (pendingCount >= stableReadingsRequired) {
            activeMatch = candidate
            return candidate
        }

        return activeMatch
    }

    fun reset() {
        pendingMatch = null
        pendingCount = 0
        activeMatch = null
    }
}
