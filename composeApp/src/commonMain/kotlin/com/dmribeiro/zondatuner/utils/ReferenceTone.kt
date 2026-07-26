package com.dmribeiro.zondatuner.utils

import kotlin.math.PI
import kotlin.math.round
import kotlin.math.sin

/** Frequência realmente emitida pelo Karplus-Strong com período quantizado. */
fun karplusPlaybackFrequency(requestedHz: Float, sampleRate: Double = 44_100.0): Float {
    if (requestedHz <= 0f) return 0f
    val period = round(sampleRate / requestedHz).toInt().coerceAtLeast(2)
    return (sampleRate / period).toFloat()
}

/** Gera PCM senoidal na frequência exata (sem quantização de período). */
fun sineToneBuffer(freq: Float, ms: Int, sampleRate: Double = 44_100.0): FloatArray {
    if (freq <= 0f) return FloatArray(0)

    val frames = (sampleRate * ms / 1_000).toInt().coerceAtLeast(1)
    val output = FloatArray(frames)
    val phaseStep = 2.0 * PI * freq / sampleRate
    val fadeOutFrames = (frames * 0.05).toInt().coerceAtLeast(1)

    for (i in 0 until frames) {
        val envelope = if (i >= frames - fadeOutFrames) {
            (frames - i).toDouble() / fadeOutFrames
        } else {
            1.0
        }
        output[i] = (sin(phaseStep * i) * envelope * 0.9).toFloat()
    }
    return output
}
