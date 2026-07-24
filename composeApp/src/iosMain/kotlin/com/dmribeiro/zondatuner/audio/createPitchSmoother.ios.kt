package com.dmribeiro.zondatuner.audio

actual fun createPitchSmoother(): PitchSmoother = PitchSmoother(
    medianWindow = 4,
    smoothingFactor = 0.35f,
    fastJumpRatio = 0.06f,
    fastSmoothingFactor = 0.65f,
)
