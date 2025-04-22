package com.dmribeiro.zondatuner.permissions

import androidx.compose.runtime.Composable

@Composable
actual fun getPermissionHandler(): PermissionHandler {
    return PermissionHandler()
}