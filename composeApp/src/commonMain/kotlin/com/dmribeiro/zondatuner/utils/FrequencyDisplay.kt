package com.dmribeiro.zondatuner.utils

import kotlin.math.round

/** Hz com 1 decimal — alinhado ao cálculo de cents (evita "329 / 330" quando afinado). */
fun Float.formatTunerHz(): String {
    val rounded = round(this * 10f) / 10f
    val intPart = rounded.toInt()
    val decimal = round((rounded - intPart) * 10f).toInt().coerceIn(0, 9)
    return "$intPart.$decimal Hz"
}
