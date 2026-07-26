package com.dmribeiro.zondatuner.domain.util

import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.domain.model.Tuning
import kotlin.test.Test
import kotlin.test.assertEquals

class TuningOrderingTest {

    @Test
    fun sortedForDisplay_ordersAlphabeticallyByName() {
        val tunings = listOf(
            tuning(id = 3, name = "Padrão (EADGBE)"),
            tuning(id = 1, name = "Aerials (System of a Down)"),
            tuning(id = 2, name = "Eb (Charlie Brown Jr)"),
        )

        val sorted = tunings.sortedForDisplay()

        assertEquals(
            listOf("Aerials (System of a Down)", "Eb (Charlie Brown Jr)", "Padrão (EADGBE)"),
            sorted.map { it.name },
        )
    }

    @Test
    fun sortedForDisplay_isCaseInsensitiveAndUsesIdAsTieBreaker() {
        val tunings = listOf(
            tuning(id = 2, name = "custom"),
            tuning(id = 1, name = "Custom"),
        )

        val sorted = tunings.sortedForDisplay()

        assertEquals(listOf(1L, 2L), sorted.map { it.id })
    }

    private fun tuning(id: Long, name: String) = Tuning(
        id = id,
        name = name,
        description = "",
        strings = List(6) { GuitarString(it + 1, 100f, "E", 0) },
    )
}
