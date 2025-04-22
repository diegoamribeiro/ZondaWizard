package com.dmribeiro.zondatuner.domain.model

data class Tuning(
    val id: Long,
    val name: String,
    val description: String,
    val strings: List<GuitarString>
)
