package com.dmribeiro.zondatuner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dmribeiro.zondatuner.utils.AndroidAudioContext

class MainActivity : ComponentActivity() {

    private var audioPermissionCallback: ((Boolean) -> Unit)? = null
    private var hasRequestedAudioPermission = false

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRequestedAudioPermission = true
        audioPermissionCallback?.invoke(granted)
        audioPermissionCallback = null
    }

    fun requestAudioPermission(onResult: (Boolean) -> Unit) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED -> onResult(true)

            else -> {
                audioPermissionCallback = onResult
                hasRequestedAudioPermission = true
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    fun isAudioPermissionPermanentlyDenied(): Boolean {
        val notGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED

        return notGranted &&
            hasRequestedAudioPermission &&
            !ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidAudioContext.applicationContext = applicationContext
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
