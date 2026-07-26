package com.dmribeiro.zondatuner.utils

import com.dmribeiro.zondatuner.domain.model.GuitarString
import kotlin.math.abs
import kotlin.math.ln

/** Máximo de desvio (cents) para aceitar match automático de corda. */
const val AUTO_MATCH_MAX_CENTS = 40f

data class StringMatch(
    val stringNumber: Int,
    val isTwelfthFret: Boolean,
    val cents: Float,
)

fun targetHzForMatch(match: StringMatch, strings: List<GuitarString>): Float {
    val string = strings.find { it.number == match.stringNumber } ?: return 0f
    return if (match.isTwelfthFret) string.frequency * 2f else string.frequency
}

/**
 * Encontra a corda aberta mais próxima da frequência detectada.
 * Modo 12ª casa não é inferido automaticamente — apenas via long-press na palheta.
 * Retorna null se nenhum candidato estiver dentro de [maxCents].
 */
fun findBestStringMatch(
    detectedHz: Float,
    strings: List<GuitarString>,
    maxCents: Float = AUTO_MATCH_MAX_CENTS,
): StringMatch? {
    if (detectedHz <= 0f) return null

    var best: StringMatch? = null
    var bestOctaveDistance = Int.MAX_VALUE

    for (string in strings) {
        val targetHz = string.frequency
        val aligned = alignFrequencyToTargetOctave(detectedHz, targetHz)
        val cents = abs(frequencyDeltaCents(aligned, targetHz))
        if (cents > maxCents) continue

        val octaveDistance = abs(
            kotlin.math.round(ln(detectedHz / targetHz) / LN2).toInt(),
        )
        val candidate = StringMatch(string.number, isTwelfthFret = false, cents = cents)
        val isBetter = when {
            best == null -> true
            octaveDistance < bestOctaveDistance -> true
            octaveDistance > bestOctaveDistance -> false
            candidate.cents < best.cents - 3f -> true
            abs(candidate.cents - best.cents) <= 3f -> {
                val bestTarget = strings.find { it.number == best.stringNumber }?.frequency ?: 0f
                val candidateAboveFundamental = detectedHz >= targetHz
                val bestAboveFundamental = detectedHz >= bestTarget
                when {
                    candidateAboveFundamental && !bestAboveFundamental -> true
                    !candidateAboveFundamental && bestAboveFundamental -> false
                    candidate.stringNumber > best.stringNumber -> true
                    else -> false
                }
            }
            else -> false
        }
        if (isBetter) {
            best = candidate
            bestOctaveDistance = octaveDistance
        }
    }
    return best
}

private val LN2 = ln(2f)
