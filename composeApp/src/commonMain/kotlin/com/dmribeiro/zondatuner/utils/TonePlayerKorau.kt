package com.dmribeiro.zondatuner.utils

import korlibs.audio.sound.AudioData
import korlibs.audio.sound.AudioSamples
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.toSound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object KorioTonePlayer {
    private var currentChannel: SoundChannel? = null
    private const val SAMPLE_RATE = 44_100

    suspend fun play(freq: Float, durationMs: Int) {
        if (freq <= 0f) return

        val pcmShort = withContext(Dispatchers.Default) {
            val pcmFloat = sineToneBuffer(freq, durationMs, SAMPLE_RATE.toDouble())
            if (pcmFloat.isEmpty()) return@withContext ShortArray(0)
            ShortArray(pcmFloat.size) {
                (pcmFloat[it].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
            }
        }

        if (pcmShort.isEmpty()) return

        withContext(Dispatchers.Main) {
            currentChannel?.stop()

            val audioSamples = AudioSamples(1, pcmShort.size, arrayOf(pcmShort))
            val audioData = AudioData(SAMPLE_RATE, audioSamples)
            currentChannel = audioData.toSound().play()
        }
    }
}

suspend fun playTone(freq: Float, durationMs: Int) =
    KorioTonePlayer.play(freq, durationMs)
