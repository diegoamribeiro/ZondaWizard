package com.dmribeiro.zondatuner.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dmribeiro.zondatuner.MainActivity
import kotlin.coroutines.coroutineContext


actual class PermissionHandler(private val context: Context) {
    actual fun hasAudioPermission(onResult: (Boolean) -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        onResult(granted)
    }

    actual fun requestAudioPermission(onResult: (Boolean) -> Unit) {
        val activity = context as? Activity ?: return

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            onResult(true)
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
            onResult(false)
        }
    }
}