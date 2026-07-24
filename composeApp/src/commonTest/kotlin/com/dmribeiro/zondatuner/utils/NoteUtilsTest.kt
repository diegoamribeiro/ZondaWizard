package com.dmribeiro.zondatuner.utils

import com.dmribeiro.zondatuner.presentation.ui.noteToFrequency
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoteUtilsTest {

    @Test
    fun normalizeFlatNotesToSharps() {
        assertEquals("D#", normalizeNote("Eb"))
        assertEquals("G#", normalizeNote("Ab"))
        assertEquals("C#", normalizeNote("Db"))
        assertEquals("F#", normalizeNote("Gb"))
        assertEquals("A#", normalizeNote("Bb"))
    }

    @Test
    fun noteToFrequencyAcceptsFlats() {
        val fromFlat = noteToFrequency("Eb", stringNumber = 6, octaveShift = 0)
        val fromSharp = noteToFrequency("D#", stringNumber = 6, octaveShift = 0)

        assertTrue(abs(fromFlat - 77.78f) < 0.1f)
        assertEquals(fromFlat, fromSharp)
    }
}
