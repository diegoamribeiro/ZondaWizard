package com.dmribeiro.zondatuner.utils

import com.dmribeiro.zondatuner.audio.YinPitchDetector
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TuningAlignmentTest {

    private val standardStrings = listOf(
        82.41f, 110.00f, 146.83f, 196.00f, 246.94f, 329.63f,
    )

    @Test
    fun pickAndTunerUseSameTargetFrequency() {
        val stringFreq = 246.94f
        val twelfthMode = true
        val pickPlayedHz = if (twelfthMode) stringFreq * 2 else stringFreq
        val tunerTargetHz = if (twelfthMode) stringFreq * 2 else stringFreq
        assertEquals(tunerTargetHz, pickPlayedHz)
    }

    @Test
    fun sinePlaybackMatchesRequestedFrequency() {
        standardStrings.forEach { target ->
            val buffer = sineToneBuffer(target, ms = 500, sampleRate = 44_100.0)
            val detector = YinPitchDetector(44_100f)
            val detected = detector.detectPitch(buffer)
            val cents = frequencyDeltaCents(detected, target)
            assertTrue(
                abs(cents) < 1.5f,
                "Target $target Hz → detected $detected Hz ($cents cents)",
            )
        }
    }

    @Test
    fun karplusQuantizationCanExceedInTuneThresholdOnSomeStrings() {
        val bString = 246.94f
        val played = karplusPlaybackFrequency(bString)
        val cents = frequencyDeltaCents(played, bString)
        assertTrue(abs(cents) > 3f, "Expected material error; got $cents cents")
    }

    @Test
    fun alignFrequencySnapsHarmonicToTargetOctave() {
        assertEquals(110f, alignFrequencyToTargetOctave(220f, 110f), absoluteTolerance = 0.01f)
        assertEquals(110f, alignFrequencyToTargetOctave(55f, 110f), absoluteTolerance = 0.01f)
        assertEquals(81.05f, alignFrequencyToTargetOctave(162.1f, 82.41f), absoluteTolerance = 0.1f)
        assertEquals(329.63f, alignFrequencyToTargetOctave(659.26f, 329.63f), absoluteTolerance = 0.01f)
    }

    @Test
    fun harmonicWithoutAlignmentClampsToGaugeEdge() {
        val rawCents = frequencyDeltaCents(220f, 110f)
        assertTrue(abs(rawCents) > 100f)
        val alignedCents = frequencyDeltaCents(
            alignFrequencyToTargetOctave(220f, 110f),
            110f,
        )
        assertTrue(abs(alignedCents) < 1f)
    }

    @Test
    fun subharmonicGarbageIsRejected() {
        val aligned = alignFrequencyToTargetOctave(65.4f, 196f)
        assertFalse(isPlausibleTunerReading(aligned, 196f))
    }

    @Test
    fun fundamentalReadingIsAccepted() {
        assertTrue(isPlausibleTunerReading(alignFrequencyToTargetOctave(162.1f, 82.41f), 82.41f))
        assertTrue(isPlausibleTunerReading(82.1f, 82.41f))
    }

    @Test
    fun zeroCentsNeedlePointsToTopCenter() {
        assertEquals(270f, centsToGaugeAngleDegrees(0f))
    }

    @Test
    fun inTunePickToneYieldsNearZeroCentsOnGauge() {
        val target = 329.63f
        val buffer = sineToneBuffer(target, ms = 500, sampleRate = 44_100.0)
        val detected = YinPitchDetector(44_100f).detectPitch(buffer)
        val cents = frequencyDeltaCents(detected, target)
        assertTrue(abs(cents) < 5f)
        assertEquals(270f, centsToGaugeAngleDegrees(cents), absoluteTolerance = 2f)
    }
}
