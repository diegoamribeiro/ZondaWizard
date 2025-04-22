package com.dmribeiro.zondatuner.permissions

expect class PermissionHandler {
    fun requestAudioPermission(onResult: (Boolean) -> Unit)
}