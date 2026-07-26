package com.dmribeiro.zondatuner.domain.util

import com.dmribeiro.zondatuner.domain.model.Tuning

private val tuningDisplayComparator =
    compareBy(String.CASE_INSENSITIVE_ORDER, Tuning::name).thenBy { it.id }

/** Ordem fixa de exibição: alfabética por nome (A→Z), desempate por id. */
fun List<Tuning>.sortedForDisplay(): List<Tuning> = sortedWith(tuningDisplayComparator)
