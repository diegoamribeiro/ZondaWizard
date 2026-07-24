package com.dmribeiro.zondatuner.audio

actual fun createPitchSmoother(): PitchSmoother = PitchSmoother(
    medianWindow = 5,
    smoothingFactor = 0.2f,
    fastJumpRatio = 0.05f,
    fastSmoothingFactor = 0.5f,
)
