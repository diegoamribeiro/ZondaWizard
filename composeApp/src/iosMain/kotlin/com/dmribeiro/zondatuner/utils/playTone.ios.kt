// iosMain

package com.dmribeiro.zondatuner.utils


import kotlinx.cinterop.*

import platform.AVFAudio.*

import kotlin.math.PI

import kotlin.math.sin


@OptIn(ExperimentalForeignApi::class)

actual fun playTone(frequency: Float, durationMs: Int) {

    val sampleRate = 44100.0

    val totalFrames = (sampleRate * durationMs / 1000.0).toInt()


    val audioFormat = AVAudioFormat(
        commonFormat = AVAudioPCMFormatFloat32,
        sampleRate = sampleRate,
        channels = 1u,
        interleaved = false
    )

    val buffer = AVAudioPCMBuffer(audioFormat, totalFrames.toUInt())
    buffer.frameLength = totalFrames.toUInt()
    val floatChannelData = buffer.floatChannelData!![0]!!

    for (i in 0 until totalFrames) {
        floatChannelData[i] = (sin(2.0 * PI * frequency * i / sampleRate)).toFloat()
    }

    val player = AVAudioPlayerNode()
    val engine = AVAudioEngine()

    engine.attachNode(player)
    engine.connect(player, engine.mainMixerNode, audioFormat)
    engine.prepare()
    engine.startAndReturnError(null)
    player.scheduleBuffer(buffer, null, 0u, null)
    player.play()

}


object TonePlayer {

    private val player = AVAudioPlayerNode()

    private var engine: AVAudioEngine? = null

    private var format: AVAudioFormat? = null


    fun setup(sharedEngine: AVAudioEngine) {

        if (engine != null) return

        engine = sharedEngine

        format = engine!!.mainMixerNode.outputFormatForBus(0u)



        engine!!.attachNode(player)

        engine!!.connect(player, engine!!.mainMixerNode, format)

    }


    @OptIn(ExperimentalForeignApi::class)

    fun play(frequency: Float, durationMs: Int) {

        val eng = engine ?: return

        val fmt = format ?: return


        val rate = fmt.sampleRate

        val totalFrames = (rate * durationMs / 1000).toInt()

        val fadeFrames = (rate * 0.005).toInt()



        memScoped {

            val buffer = AVAudioPCMBuffer(fmt, totalFrames.toUInt())!!

            buffer.frameLength = totalFrames.toUInt()


            val data = buffer.floatChannelData!![0]!!

            for (i in 0 until totalFrames) {

                val envelope = when {

                    i < fadeFrames -> i / fadeFrames.toFloat()

                    i > totalFrames - fadeFrames -> (totalFrames - i) / fadeFrames.toFloat()

                    else -> 1f

                }

                data[i] = (sin(2.0 * PI * frequency * i / rate) * envelope).toFloat()

            }


            if (!player.playing) player.play()

            player.scheduleBuffer(buffer, null, 0u, null)

        }

    }

}