package com.dmribeiro.zondatuner.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TopBarMenuActions {
    var onEdit by mutableStateOf<(() -> Unit)?>(null)
    var onDelete by mutableStateOf<(() -> Unit)?>(null)

    fun clear() {
        onEdit = null
        onDelete = null
    }
}

val LocalTopBarMenuActions = compositionLocalOf { TopBarMenuActions() }
