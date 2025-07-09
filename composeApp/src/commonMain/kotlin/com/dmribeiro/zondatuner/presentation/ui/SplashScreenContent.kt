package com.dmribeiro.zondatuner.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import zondawizard.composeapp.generated.resources.Res
import zondawizard.composeapp.generated.resources.img_splash_zonda_tuner_png

@Composable
fun SplashScreenContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (isSystemInDarkTheme()) Color.White else Color.Black
    ) {
        // Este Box ocupa a tela toda e centraliza o que estiver dentro.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // A Image define seu próprio tamanho. Não precisamos de uma Box extra.
            // O Box pai já vai centralizá-la na tela.
            Box(modifier = Modifier.size(width = 200.dp, height = 300.dp)
                .background(if (isSystemInDarkTheme()) Color.Black else Color.White),
                contentAlignment = Alignment.Center
            ){
                Image(
                    painter = painterResource(Res.drawable.img_splash_zonda_tuner_png),
                    contentDescription = "Zonda Tuner Logo",
                    modifier = Modifier.size(250.dp, 300.dp),
                    colorFilter = ColorFilter.tint(
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    )
                )
            }
        }
    }
}