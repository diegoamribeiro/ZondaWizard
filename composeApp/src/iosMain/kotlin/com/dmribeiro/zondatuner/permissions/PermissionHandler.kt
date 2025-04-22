package com.dmribeiro.zondatuner.permissions

import platform.AVFAudio.AVAudioSession


actual class PermissionHandler {
    actual fun requestAudioPermission(onResult: (Boolean) -> Unit) {
        val session = AVAudioSession.sharedInstance()
        session.requestRecordPermission { granted ->
            onResult(granted)
        }
    }
}