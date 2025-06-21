package com.dmribeiro.zondatuner.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@RequiresApi(Build.VERSION_CODES.O)
@Composable
actual fun vibrate(): () -> Unit {
    val vibrator = LocalContext.current.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    return { vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)) }
}