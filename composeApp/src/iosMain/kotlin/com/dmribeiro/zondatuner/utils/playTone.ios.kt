// iosMain
package com.dmribeiro.zondatuner.utils

import kotlinx.cinterop.*
import platform.AVFAudio.*
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalForeignApi::class)
actual fun playTone(frequency: Float, durationMs: Int) {
    val engine = AVAudioEngine()
    val mixer = engine.mainMixerNode
    val format = mixer.outputFormatForBus(0u)

    val rate = format.sampleRate
    val totalFrames = (rate * durationMs / 1_000.0).toInt()
    val fadeFrames = (rate * 0.005).toInt()        // 5 ms de fade-in/out

    val player = AVAudioPlayerNode()
    engine.attachNode(player)
    engine.connect(player, mixer, format)

    memScoped {
        val buffer = AVAudioPCMBuffer(format, frameCapacity = totalFrames.toUInt())!!
        buffer.frameLength = totalFrames.toUInt()

        val data = buffer.floatChannelData!![0]!!          // ptr para canal L
        for (i in 0 until totalFrames) {
            val env = when {
                i < fadeFrames -> i / fadeFrames.toFloat()
                i > totalFrames - fadeFrames -> (totalFrames - i) / fadeFrames.toFloat()
                else -> 1f
            }
            val sample =
                (sin(2.0 * PI * frequency * i / rate) * env).toFloat()
            data[i] = sample
        }

        engine.prepare()
        engine.startAndReturnError(null)

        player.scheduleBuffer(buffer, null, 0u) {
            player.stop()
            engine.stop()
        }
        player.play()
    }
}