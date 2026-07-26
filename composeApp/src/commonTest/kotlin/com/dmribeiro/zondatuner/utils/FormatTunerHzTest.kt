package com.dmribeiro.zondatuner.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTunerHzTest {

    @Test
    fun formatTunerHz_oneDecimal() {
        assertEquals("329.6 Hz", 329.63f.formatTunerHz())
        assertEquals("82.4 Hz", 82.41f.formatTunerHz())
        assertEquals("110.0 Hz", 110f.formatTunerHz())
    }
}
