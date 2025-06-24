// Em: commonMain/kotlin/com/dmribeiro/zondatuner/audio/KorioAudio.kt

package com.dmribeiro.zondatuner.utils

import korlibs.audio.sound.AudioData
import korlibs.audio.sound.AudioSamples
import korlibs.audio.sound.NativeSoundProvider
import korlibs.audio.sound.Sound
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.nativeSoundProvider
import korlibs.audio.sound.toSound
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

// --- PARÂMETROS DO KARPLUS-STRONG PARA SOM LIMPO ---
private const val KS_DAMPING = 0.997f // Amortecimento para um som de violão limpo
private const val KS_PLPASS = 0.5f    // Filtro para o timbre

/**
 * Gera o buffer de áudio com o algoritmo Karplus-Strong.
 * Esta função é pura e não depende de nenhuma plataforma.
 */
private fun karplusStrongBuffer(freq: Float, ms: Int, sampleRate: Double): FloatArray {
    val period = (sampleRate / freq).toInt().coerceAtLeast(2)
    val frames = (sampleRate * ms / 1_000).toInt()
    val delayLine = FloatArray(period) { Random.nextFloat() * 2f - 1f }
    val output = FloatArray(frames)
    var idx = 0
    repeat(frames) { i ->
        val current = delayLine[idx]
        val next = delayLine[(idx + 1) % period]
        // Filtro simples para simular o decaimento da corda
        delayLine[idx] = (current * KS_PLPASS + next * (1 - KS_PLPASS)) * KS_DAMPING
        output[i] = current
        idx = (idx + 1) % period
    }
    return output
}


object KorioTonePlayer {
    private var currentChannel: SoundChannel? = null
    private const val SAMPLE_RATE = 44_100

    suspend fun play(freq: Float, durationMs: Int) {
        /** 1️⃣ gera PCM fora da UI *************************************/
        val pcmShort = withContext(Dispatchers.Default) {
            val pcmFloat = karplusStrongBuffer(freq, durationMs, SAMPLE_RATE.toDouble())
            ShortArray(pcmFloat.size) {
                (pcmFloat[it].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
            }
        }

        /** 2️⃣ volta para a main - onde AVFoundation/korau exige ****** */
        withContext(Dispatchers.Main) {
            currentChannel?.stop()

            val audioSamples = AudioSamples(1, pcmShort.size, arrayOf(pcmShort))
            val audioData    = AudioData(SAMPLE_RATE, audioSamples)
            currentChannel   = audioData.toSound().play()   // agora seguro
        }
    }
}

/* API pública */
suspend fun playTone(freq: Float, durationMs: Int) =
    KorioTonePlayer.play(freq, durationMs)