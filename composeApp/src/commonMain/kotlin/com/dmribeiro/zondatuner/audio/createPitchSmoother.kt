package com.dmribeiro.zondatuner.audio

/**
 * Parâmetros de suavização variam por plataforma:
 * - Android já filtra confiança/volume na origem → suavização mais forte.
 * - iOS envia leituras mais "puras" → suavização mais leve para o ponteiro responder.
 */
expect fun createPitchSmoother(): PitchSmoother
