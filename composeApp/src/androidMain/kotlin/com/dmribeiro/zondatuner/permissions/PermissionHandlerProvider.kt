package com.dmribeiro.zondatuner.permissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getPermissionHandler(): PermissionHandler {
    val context = LocalContext.current
    return PermissionHandler(context)
}