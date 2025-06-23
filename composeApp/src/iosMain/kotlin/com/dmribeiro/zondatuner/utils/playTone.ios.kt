package com.dmribeiro.zondatuner.utils

import kotlinx.cinterop.*
import platform.AVFAudio.*
import kotlin.random.Random
import kotlin.math.*

private const val KS_DAMPING = 0.9975f
private const val KS_PLPASS = 0.5f

private fun karplusStrongBuffer(freq: Float, ms: Int, sr: Double): FloatArray {
    val period = (sr / freq).toInt().coerceAtLeast(2)
    val frames = (sr * ms / 1_000).toInt()
    val delay = FloatArray(period) { Random.nextFloat() * 2f - 1f }
    val output = FloatArray(frames)
    var idx = 0
    repeat(frames) { i ->
        val cur = delay[idx]
        val next = delay[(idx + 1) % period]
        delay[idx] = (cur * KS_PLPASS + next * (1 - KS_PLPASS)) * KS_DAMPING
        output[i] = cur
        idx = (idx + 1) % period
    }
    return output
}

object TonePlayer {
    private var player: AVAudioPlayerNode? = null
    private var format: AVAudioFormat? = null

    /** Deve ser chamado *apenas uma vez* logo após criar o engine */
    fun setup(engine: AVAudioEngine) {
        if (player != null) return
        player = AVAudioPlayerNode()
        format = engine.mainMixerNode.outputFormatForBus(0u)
        engine.attachNode(player!!)
        engine.connect(
            player!!,
            engine.mainMixerNode,
            format
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    fun play(freq: Float, durationMs: Int) {
        val ply = player ?: return
        val fmt = format ?: return
        val sr = fmt.sampleRate
        val fade = (sr * 0.005).toInt()
        val pcm = karplusStrongBuffer(freq, durationMs, sr)
        val buf = AVAudioPCMBuffer(fmt, pcm.size.toUInt())!!.apply {
            frameLength = pcm.size.toUInt()
            val ch0 = floatChannelData!![0]!!.reinterpret<FloatVar>()
            val stereo = fmt.channelCount.toInt() > 1
            val ch1 = if (stereo) floatChannelData!![1]!!.reinterpret<FloatVar>() else null
            pcm.forEachIndexed { i, sample ->
                val env = when {
                    i < fade           -> i / fade.toFloat()
                    i >= pcm.size - fade -> (pcm.size - i) / fade.toFloat()
                    else               -> 1f
                }
                ch0[i] = sample * env
                ch1?.set(i, sample * env)
            }
        }
        if (!ply.playing) ply.play()
        ply.scheduleBuffer(buf, null, 0u, null)
    }
}

/** Ponte expect/actual */
@OptIn(ExperimentalForeignApi::class)
actual fun playTone(frequency: Float, durationMs: Int) {
    TonePlayer.play(frequency, durationMs)
}