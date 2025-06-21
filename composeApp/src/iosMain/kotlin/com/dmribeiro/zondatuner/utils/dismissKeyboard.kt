package com.dmribeiro.zondatuner.utils

import androidx.compose.runtime.Composable
import platform.UIKit.UIApplication
import platform.UIKit.endEditing

@Composable
actual fun dismissKeyboardLambda(): () -> Unit {
    return {
        UIApplication.sharedApplication.keyWindow?.endEditing(true)
    }
}