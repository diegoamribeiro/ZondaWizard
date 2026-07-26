package com.dmribeiro.zondatuner.utils

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/** Cents entre frequência detectada e alvo — mesma fórmula do afinador. */
fun frequencyDeltaCents(detectedHz: Float, targetHz: Float): Float {
    if (detectedHz <= 0f || targetHz <= 0f) return 0f
    return (1200f * log2(detectedHz / targetHz))
}

/**
 * Corrige erros de oitava do detector (ex.: 162 Hz lidos com alvo 82 Hz),
 * snapando para a oitava mais próxima do alvo da corda selecionada.
 */
fun alignFrequencyToTargetOctave(detectedHz: Float, targetHz: Float): Float {
    if (detectedHz <= 0f || targetHz <= 0f) return detectedHz
    val octaveShift = log2(detectedHz / targetHz).roundToInt()
    return detectedHz / 2f.pow(octaveShift)
}

/** Faixa visual do gauge (±50 cents). */
const val GAUGE_CENTS_RANGE = 50f

/** Acima disso após alinhar oitava = lixo do detector (não afinação real). */
const val GARBAGE_READING_MAX_CENTS = 100f

/** Leitura plausível — descarta sub/harmônicos espúrios, mas permite afinação em toda faixa do gauge. */
fun isPlausibleTunerReading(
    alignedHz: Float,
    targetHz: Float,
    maxCents: Float = GARBAGE_READING_MAX_CENTS,
): Boolean {
    if (alignedHz <= 0f || targetHz <= 0f) return false
    return abs(frequencyDeltaCents(alignedHz, targetHz)) <= maxCents
}

/** Ângulo do ponteiro no gauge semicircular (-50..+50 cents → 180°..360°). */
fun centsToGaugeAngleDegrees(cents: Float): Float =
    180f + (cents + 50f) / 100f * 180f
