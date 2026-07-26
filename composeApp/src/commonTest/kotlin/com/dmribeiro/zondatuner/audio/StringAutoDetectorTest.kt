package com.dmribeiro.zondatuner.audio

import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.utils.StringMatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StringAutoDetectorTest {

    private val standardTuning = listOf(
        GuitarString(6, 82.41f, "E", 0),
        GuitarString(5, 110.00f, "A", 0),
        GuitarString(4, 146.83f, "D", 0),
        GuitarString(3, 196.00f, "G", 0),
        GuitarString(2, 246.94f, "B", 0),
        GuitarString(1, 329.63f, "E", 0),
    )

    @Test
    fun requiresStableReadingsBeforeSwitching() {
        val detector = StringAutoDetector(stableReadingsRequired = 4)
        assertNull(detector.evaluate(196.2f, standardTuning))
        assertNull(detector.evaluate(196.2f, standardTuning))
        assertNull(detector.evaluate(196.2f, standardTuning))
        val fourth = detector.evaluate(196.2f, standardTuning)
        assertEquals(3, fourth?.stringNumber)
        assertEquals(false, fourth?.isTwelfthFret)
    }

    @Test
    fun doesNotOscillateBetweenAdjacentStrings() {
        val detector = StringAutoDetector(stableReadingsRequired = 4)
        repeat(4) { detector.evaluate(81.3f, standardTuning) }
        assertEquals(6, detector.evaluate(81.3f, standardTuning)?.stringNumber)

        repeat(2) {
            detector.evaluate(110.5f, standardTuning)
        }
        assertEquals(6, detector.evaluate(110.5f, standardTuning)?.stringNumber)

        repeat(4) {
            detector.evaluate(110.5f, standardTuning)
        }
        assertEquals(5, detector.evaluate(110.5f, standardTuning)?.stringNumber)
    }

    @Test
    fun resetClearsActiveMatch() {
        val detector = StringAutoDetector(stableReadingsRequired = 2)
        repeat(2) { detector.evaluate(196.2f, standardTuning) }
        assertEquals(3, detector.evaluate(196.2f, standardTuning)?.stringNumber)

        detector.reset()
        assertNull(detector.evaluate(196.2f, standardTuning))
    }

    @Test
    fun fastSwitchWhenMuchBetterMatch() {
        val detector = StringAutoDetector(stableReadingsRequired = 4, fastSwitchCentsDelta = 15f)
        repeat(4) { detector.evaluate(81.3f, standardTuning) }
        assertEquals(6, detector.evaluate(81.3f, standardTuning)?.stringNumber)

        val switched = detector.evaluate(196.2f, standardTuning)
        assertEquals(3, switched?.stringNumber)
    }
}
