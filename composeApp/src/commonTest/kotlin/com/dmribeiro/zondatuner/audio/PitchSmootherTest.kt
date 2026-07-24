package com.dmribeiro.zondatuner.audio

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PitchSmootherTest {

    @Test
    fun ignoresNonPositiveReadings() {
        val smoother = PitchSmoother()
        assertEquals(0f, smoother.add(0f))
        assertEquals(0f, smoother.add(-10f))
    }

    @Test
    fun convergesToStableFrequency() {
        val smoother = PitchSmoother()
        var result = 0f
        repeat(20) { result = smoother.add(110f) }
        assertTrue(abs(result - 110f) < 0.01f)
    }

    @Test
    fun rejectsSingleOctaveSpike() {
        val smoother = PitchSmoother()
        repeat(10) { smoother.add(110f) }
        // Um único erro de oitava (220 Hz) não deve mover a leitura de forma perceptível.
        val afterSpike = smoother.add(220f)
        assertTrue(abs(afterSpike - 110f) < 1f, "Leitura foi para $afterSpike")
    }

    @Test
    fun smoothsSmallFluctuations() {
        val smoother = PitchSmoother()
        repeat(10) { smoother.add(110f) }
        val jittery = listOf(110.8f, 109.4f, 110.5f, 109.6f, 110.3f)
        val results = jittery.map { smoother.add(it) }
        // A saída deve variar bem menos que a entrada.
        val maxDeviation = results.maxOf { abs(it - 110f) }
        assertTrue(maxDeviation < 0.5f, "Desvio máximo foi $maxDeviation")
    }

    @Test
    fun respondsToRealStringChange() {
        val smoother = PitchSmoother()
        repeat(10) { smoother.add(110f) }
        // Mudança sustentada (nova corda) deve ser seguida rapidamente.
        var result = 0f
        repeat(10) { result = smoother.add(146.83f) }
        assertTrue(abs(result - 146.83f) < 2f, "Leitura ficou em $result")
    }

    @Test
    fun resetClearsHistory() {
        val smoother = PitchSmoother()
        repeat(10) { smoother.add(110f) }
        smoother.reset()
        val first = smoother.add(196f)
        assertEquals(196f, first)
    }
}
