package com.dmribeiro.zondatuner.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.TimeSource

class TunerReadingHoldTest {

    @Test
    fun updateStoresFrequency() {
        val hold = TunerReadingHold()
        val now = TimeSource.Monotonic.markNow()
        hold.update(110f, now)
        assertEquals(110f, hold.heldFrequency)
        assertTrue(hold.isReceivingSignal(now))
    }

    @Test
    fun ignoresNonPositiveReadings() {
        val hold = TunerReadingHold()
        val now = TimeSource.Monotonic.markNow()
        hold.update(110f, now)
        hold.update(0f, now)
        assertEquals(110f, hold.heldFrequency)
    }

    @Test
    fun resetClearsState() {
        val hold = TunerReadingHold()
        hold.update(110f, TimeSource.Monotonic.markNow())
        hold.reset()
        assertEquals(0f, hold.heldFrequency)
        assertFalse(hold.isReceivingSignal(TimeSource.Monotonic.markNow()))
    }
}
