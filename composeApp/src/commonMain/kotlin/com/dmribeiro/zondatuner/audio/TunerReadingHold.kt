package com.dmribeiro.zondatuner.audio

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark

/**
 * Mantém a última leitura válida por um curto período (signalTimeout),
 * evitando que o ponteiro "pule" em falhas breves de detecção.
 * Após signalTimeout sem novas leituras, a UI zera o valor exibido.
 */
class TunerReadingHold(
    private val signalTimeout: Duration = 2.5.seconds,
    private val clearTimeout: Duration = 4.5.seconds,
) {
    var heldFrequency: Float = 0f
        private set

    private var lastReadingAt: TimeMark? = null

    fun update(smoothedFrequency: Float, now: TimeMark) {
        if (smoothedFrequency <= 0f) return
        heldFrequency = smoothedFrequency
        lastReadingAt = now
    }

    fun isReceivingSignal(now: TimeMark): Boolean {
        val last = lastReadingAt ?: return false
        return last.elapsedNow() < signalTimeout
    }

    fun shouldClear(now: TimeMark): Boolean {
        val last = lastReadingAt ?: return heldFrequency > 0f
        return last.elapsedNow() >= clearTimeout
    }

    fun reset() {
        heldFrequency = 0f
        lastReadingAt = null
    }
}
