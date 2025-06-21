package com.dmribeiro.zondatuner.utils

import androidx.compose.runtime.Composable
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

@Composable
actual fun vibrate(): () -> Unit {
    return {
        val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleRigid)
        generator.prepare()
        generator.impactOccurred()
    }
}