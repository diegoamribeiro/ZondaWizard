// androidMain/utils/Audio.kt
package com.dmribeiro.zondatuner.utils

import android.media.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

private const val SAMPLE_RATE = 44_100
private const val KS_DAMPING = 0.998f          // sustain (0.99–0.999)

/*-------------------------------------------------------------*
 *            Algoritmo Karplus-Strong (violão)                 *
 *-------------------------------------------------------------*/
private fun karplusStrong(
    frequency: Float,
    frames: Int
): FloatArray {

    val period = (SAMPLE_RATE / frequency).toInt().coerceAtLeast(2)
    val delayLine = FloatArray(period) { Random.nextFloat() * 2f - 1f }
    val result = FloatArray(frames)

    var idx = 0
    for (i in 0 until frames) {
        val current = delayLine[idx]
        val nextIdx = (idx + 1) % period
        val next = delayLine[nextIdx]

        /*  baixa-passa simples + damping  */
        delayLine[idx] = ((current + next) * 0.5f) * KS_DAMPING

        result[i] = current
        idx = nextIdx
    }
    return result
}

/*-------------------------------------------------------------*
 *                   API chamável do app                       *
 *-------------------------------------------------------------*/
actual fun playTone(frequency: Float, durationMs: Int) {

    /* -------- 1. gera PCM em float -------------------------- */
    val totalFrames = (durationMs * SAMPLE_RATE / 1_000.0).toInt()
    val fadeFrames = (SAMPLE_RATE * 0.020).toInt().coerceAtMost(totalFrames / 2)

    val pcmFloat = karplusStrong(frequency, totalFrames)

    /* envelope de 20 ms (Hann) para evitar estalos */
    fun hann(i: Int, n: Int) = 0.5f * (1 - cos(PI * i / n)).toFloat()
    for (i in 0 until totalFrames) {
        val env = when {
            i < fadeFrames -> hann(i, fadeFrames)
            i >= totalFrames - fadeFrames -> hann(totalFrames - i, fadeFrames)
            else -> 1f
        }
        pcmFloat[i] *= env
    }

    /* -------- 2. converte p/ SHORT (16-bit PCM) ------------- */
    val pcmShort = ShortArray(totalFrames) { i ->
        (pcmFloat[i].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
    }

    /* -------- 3. toca com AudioTrack (Builder API) ---------- */
    val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setTransferMode(AudioTrack.MODE_STATIC)
        .setBufferSizeInBytes(pcmShort.size * 2)
        .build()

    track.write(pcmShort, 0, pcmShort.size)
    track.notificationMarkerPosition = pcmShort.size
    track.play()

    track.setPlaybackPositionUpdateListener(object :
        AudioTrack.OnPlaybackPositionUpdateListener {
        override fun onMarkerReached(p0: AudioTrack?) {
            track.stop()
            track.release()
        }

        override fun onPeriodicNotification(p0: AudioTrack?) = Unit
    })
}