// androidMain
package com.dmribeiro.zondatuner.utils

import android.media.*
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

actual fun playTone(frequency: Float, durationMs: Int) {

    val sampleRate = 44_100
    val totalSamples = (durationMs * sampleRate / 1_000.0).toInt()

    // 20 ms de ataque + 20 ms de release
    val edgeSamples = (sampleRate * 0.020).toInt().coerceAtMost(totalSamples / 2)

    val pcm = ShortArray(totalSamples)

    fun hann(i: Int, n: Int) = 0.5f * (1 - cos(PI * i / n)).toFloat()

    for (i in 0 until totalSamples) {

        // --- envelope Hann de 20 ms no início e no fim -------------
        val env = when {
            i < edgeSamples -> hann(i, edgeSamples)          // ataque
            i >= totalSamples - edgeSamples ->
                hann(totalSamples - i, edgeSamples)    // release
            else -> 1f
        }
        // ------------------------------------------------------------

        val angle = 2.0 * PI * frequency * i / sampleRate
        pcm[i] = (sin(angle) * env * Short.MAX_VALUE).toInt().toShort()
    }

    val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setTransferMode(AudioTrack.MODE_STATIC)
        .setBufferSizeInBytes(pcm.size * 2)
        .build()

    track.write(pcm, 0, pcm.size)
    track.notificationMarkerPosition = pcm.size
    track.play()

    track.setPlaybackPositionUpdateListener(object :
        AudioTrack.OnPlaybackPositionUpdateListener {
        override fun onMarkerReached(p0: AudioTrack?) {
            track.release()
        }

        override fun onPeriodicNotification(p0: AudioTrack?) {}
    })
}