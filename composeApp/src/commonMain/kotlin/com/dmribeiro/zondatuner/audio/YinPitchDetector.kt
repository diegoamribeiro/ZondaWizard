package com.dmribeiro.zondatuner.audio

/**
 * YIN pitch detection algorithm (Cheveigné & Kawahara, 2002).
 * Pure Kotlin implementation shared across platforms.
 */
class YinPitchDetector(
    private val sampleRate: Float,
    private val threshold: Float = 0.15f,
    private val minFrequency: Float = 50f,
    private val maxFrequency: Float = 1000f
) {
    fun detectPitch(samples: FloatArray): Float {
        if (samples.size < 4) return 0f

        val maxTau = (sampleRate / minFrequency).toInt().coerceAtMost(samples.size - 1)
        val minTau = (sampleRate / maxFrequency).toInt().coerceAtLeast(2)
        if (minTau >= maxTau) return 0f

        val yinBuffer = FloatArray(maxTau + 1)

        var runningSum = 0f
        yinBuffer[0] = 1f

        for (tau in 1..maxTau) {
            var sum = 0f
            for (i in 0 until samples.size - tau) {
                val delta = samples[i] - samples[i + tau]
                sum += delta * delta
            }
            yinBuffer[tau] = sum
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = if (runningSum == 0f) 1f else yinBuffer[tau] * tau / runningSum
        }

        var tauEstimate = -1
        var tau = minTau
        while (tau < maxTau) {
            if (yinBuffer[tau] < threshold) {
                var bestTau = tau
                while (bestTau + 1 < maxTau && yinBuffer[bestTau + 1] < yinBuffer[bestTau]) {
                    bestTau++
                }
                tauEstimate = bestTau
                break
            }
            tau++
        }

        if (tauEstimate == -1) {
            var minVal = Float.MAX_VALUE
            for (candidate in minTau until maxTau) {
                if (yinBuffer[candidate] < minVal) {
                    minVal = yinBuffer[candidate]
                    tauEstimate = candidate
                }
            }
            if (minVal >= threshold) return 0f
        }

        val betterTau = parabolicInterpolation(yinBuffer, tauEstimate)
        return if (betterTau > 0f) sampleRate / betterTau else 0f
    }

    private fun parabolicInterpolation(yinBuffer: FloatArray, tauEstimate: Int): Float {
        if (tauEstimate <= 0 || tauEstimate >= yinBuffer.size - 1) {
            return tauEstimate.toFloat()
        }

        val s0 = yinBuffer[tauEstimate - 1]
        val s1 = yinBuffer[tauEstimate]
        val s2 = yinBuffer[tauEstimate + 1]

        val adjustment = (s2 - s0) / (2f * (2f * s1 - s2 - s0))
        return if (adjustment.isFinite()) {
            tauEstimate + adjustment
        } else {
            tauEstimate.toFloat()
        }
    }
}
