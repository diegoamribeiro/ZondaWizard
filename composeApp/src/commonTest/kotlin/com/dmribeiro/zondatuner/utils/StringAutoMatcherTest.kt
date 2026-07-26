package com.dmribeiro.zondatuner.utils

import com.dmribeiro.zondatuner.domain.model.GuitarString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StringAutoMatcherTest {

    private val standardTuning = listOf(
        GuitarString(6, 82.41f, "E", 0),
        GuitarString(5, 110.00f, "A", 0),
        GuitarString(4, 146.83f, "D", 0),
        GuitarString(3, 196.00f, "G", 0),
        GuitarString(2, 246.94f, "B", 0),
        GuitarString(1, 329.63f, "E", 0),
    )

    @Test
    fun matchesOpenLowE() {
        val match = findBestStringMatch(81.3f, standardTuning)
        assertNotNull(match)
        assertEquals(6, match.stringNumber)
        assertFalse(match.isTwelfthFret)
    }

    @Test
    fun matchesTwelfthFretLowEAsOpenString() {
        val match = findBestStringMatch(164.1f, standardTuning)
        assertNotNull(match)
        assertEquals(6, match.stringNumber)
        assertFalse(match.isTwelfthFret)
    }

    @Test
    fun matchesHighEString() {
        val match = findBestStringMatch(329.2f, standardTuning)
        assertNotNull(match)
        assertEquals(1, match.stringNumber)
        assertFalse(match.isTwelfthFret)
    }

    @Test
    fun matchesOpenGString() {
        val match = findBestStringMatch(196.2f, standardTuning)
        assertNotNull(match)
        assertEquals(3, match.stringNumber)
        assertFalse(match.isTwelfthFret)
    }

    @Test
    fun rejectsSubharmonicGarbage() {
        assertNull(findBestStringMatch(65.4f, standardTuning))
    }

    @Test
    fun prefersOpenOverTwelfthOnTie() {
        val match = findBestStringMatch(82.41f, standardTuning)
        assertNotNull(match)
        assertEquals(6, match.stringNumber)
        assertFalse(match.isTwelfthFret)
    }

    @Test
    fun targetHzForOpenAndTwelfth() {
        val open = StringMatch(6, isTwelfthFret = false, cents = 0f)
        val twelfth = StringMatch(6, isTwelfthFret = true, cents = 0f)
        assertEquals(82.41f, targetHzForMatch(open, standardTuning))
        assertEquals(164.82f, targetHzForMatch(twelfth, standardTuning), absoluteTolerance = 0.01f)
    }

    @Test
    fun harmonicAlignedWithinThresholdMatches() {
        val match = findBestStringMatch(162.8f, standardTuning)
        assertNotNull(match)
        assertEquals(6, match.stringNumber)
    }
}
