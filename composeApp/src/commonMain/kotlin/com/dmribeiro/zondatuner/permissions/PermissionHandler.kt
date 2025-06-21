package com.dmribeiro.zondatuner.permissions

expect class PermissionHandler {
    fun hasAudioPermission(onResult: (Boolean) -> Unit)
    fun requestAudioPermission(onResult: (Boolean) -> Unit)
}