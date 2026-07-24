package com.dmribeiro.zondatuner.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.dmribeiro.zondatuner.MainActivity

actual class PermissionHandler(private val context: Context) {
    actual fun hasAudioPermission(onResult: (Boolean) -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        onResult(granted)
    }

    actual fun requestAudioPermission(onResult: (Boolean) -> Unit) {
        val activity = context as? MainActivity
        if (activity != null) {
            activity.requestAudioPermission(onResult)
        } else {
            onResult(false)
        }
    }
}
