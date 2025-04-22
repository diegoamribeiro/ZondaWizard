package com.dmribeiro.zondatuner.utils

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

actual fun String.formatFrequency(): String {
    return NSString.stringWithFormat("%.2f Hz", this.toDoubleOrNull() ?: 0.0)
}

actual fun Float.formatFrequency(): String {
    return NSString.stringWithFormat("%.2f Hz", this)
}
