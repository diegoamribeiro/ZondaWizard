package com.dmribeiro.zondatuner.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertTrue

class YinPitchDetectorTest {

    private fun generateSineWave(
        frequency: Float,
        sampleRate: Float,
        durationSeconds: Float = 0.5f
    ): FloatArray {
        val sampleCount = (sampleRate * durationSeconds).toInt()
        return FloatArray(sampleCount) { i ->
            sin(2.0 * PI * frequency * i / sampleRate).toFloat()
        }
    }

    @Test
    fun detectsLowEString() {
        val sampleRate = 44100f
        val targetFrequency = 82.41f
        val detector = YinPitchDetector(sampleRate)
        val samples = generateSineWave(targetFrequency, sampleRate)

        val detected = detector.detectPitch(samples)

        assertTrue(abs(detected - targetFrequency) < 1f, "Expected ~$targetFrequency Hz, got $detected Hz")
    }

    @Test
    fun detectsA440() {
        val sampleRate = 44100f
        val targetFrequency = 440f
        val detector = YinPitchDetector(sampleRate)
        val samples = generateSineWave(targetFrequency, sampleRate)

        val detected = detector.detectPitch(samples)

        assertTrue(abs(detected - targetFrequency) < 1f, "Expected ~$targetFrequency Hz, got $detected Hz")
    }

    @Test
    fun returnsZeroForSilence() {
        val sampleRate = 44100f
        val detector = YinPitchDetector(sampleRate)
        val samples = FloatArray(4096) { 0f }

        val detected = detector.detectPitch(samples)

        assertTrue(detected == 0f || detected.roundToInt() == 0, "Expected 0 Hz for silence, got $detected Hz")
    }
}
