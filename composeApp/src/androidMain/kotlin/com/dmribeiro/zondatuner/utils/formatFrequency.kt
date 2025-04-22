package com.dmribeiro.zondatuner.utils

import java.util.Locale

actual fun String.formatFrequency(): String {
    return String.format(Locale.US, "%.2f Hz", this.toFloatOrNull() ?: 0f)
}

actual fun Float.formatFrequency(): String {
    return String.format(Locale.US, "%.2f Hz", this)
}
