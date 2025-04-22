package com.dmribeiro.zondatuner.presentation.dataui

import kotlinx.coroutines.flow.MutableStateFlow

class TuningStateUI {
    val listState = MutableStateFlow<List<TuningDataUi>>(emptyList())
    var loading: Boolean = false
    var error: Throwable? = null
}