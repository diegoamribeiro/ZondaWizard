package com.dmribeiro.zondatuner.presentation.dataui

import com.dmribeiro.zondatuner.domain.model.GuitarString

data class TuningDataUi(
    val id: Long,
    val name: String,
    val description: String,
    val strings: List<GuitarString>
)