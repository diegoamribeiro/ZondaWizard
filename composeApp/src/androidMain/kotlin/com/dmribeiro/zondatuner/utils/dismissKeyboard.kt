package com.dmribeiro.zondatuner.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager

@Composable
actual fun dismissKeyboardLambda(): () -> Unit {
    val focusManager = LocalFocusManager.current
    return { focusManager.clearFocus() }
}