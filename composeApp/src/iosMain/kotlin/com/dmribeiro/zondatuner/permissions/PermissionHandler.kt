package com.dmribeiro.zondatuner.permissions

import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted


actual class PermissionHandler {
    actual fun hasAudioPermission(onResult: (Boolean) -> Unit) {
        val session = AVAudioSession.sharedInstance()
        val permission = session.recordPermission
        onResult(permission == AVAudioSessionRecordPermissionGranted)
    }

    actual fun requestAudioPermission(onResult: (Boolean) -> Unit) {
        val session = AVAudioSession.sharedInstance()
        session.requestRecordPermission { granted ->
            onResult(granted)
        }
    }
}