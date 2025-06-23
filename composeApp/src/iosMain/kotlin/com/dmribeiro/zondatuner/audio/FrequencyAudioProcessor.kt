// iosMain/kotlin/com/dmribeiro/zondatuner/audio/FrequencyAudioProcessor.kt
package com.dmribeiro.zondatuner.audio

import com.dmribeiro.zondatuner.utils.TonePlayer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import platform.AVFAudio.*
import kotlin.math.*

actual class FrequencyAudioProcessor actual constructor(
    private val onFrequencyDetected: (Float) -> Unit
) {
    private var engine: AVAudioEngine? = null
    private var inputNode: AVAudioInputNode? = null

    companion object {
        private const val SAMPLE_RATE     = 44_100.0    // Hz
        private const val BUFFER_SIZE     = 2_048u      // amostras
        private const val SMOOTH_WINDOW   = 3
    }

    // Buffer para suavização
    private val freqBuffer = mutableListOf<Float>()

    /** 1️⃣ Configura AVAudioSession para Play & Record + viva-voz */
    @OptIn(ExperimentalForeignApi::class)
    private fun configureSession() {
        AVAudioSession.sharedInstance().apply {
            setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                withOptions = AVAudioSessionCategoryOptionAllowBluetooth or
                        AVAudioSessionCategoryOptionMixWithOthers,
                error = null
            )
            setPreferredSampleRate(SAMPLE_RATE, error = null)
            setPreferredIOBufferDuration(0.008, error = null)
            setActive(true, error = null)
            overrideOutputAudioPort(AVAudioSessionPortOverrideSpeaker, error = null)
        }
    }

    /** 2️⃣ Inicia captura e arma a saída de som no mesmo engine */
    @OptIn(ExperimentalForeignApi::class)
    actual fun start() {
        configureSession()

        engine = AVAudioEngine().also { eng ->
            // inputNode antes de mais nada
            inputNode = eng.inputNode

            // 🔧 Setup do TonePlayer **antes** de ligar o engine
            TonePlayer.setup(eng)

            // formatação do tap para Float32
            val hwFmt   = inputNode!!.inputFormatForBus(0u)
            val tapFmt  = AVAudioFormat(
                commonFormat = AVAudioPCMFormatFloat32.toULong(),
                sampleRate   = hwFmt.sampleRate,
                channels     = 1u,
                interleaved  = false
            )

            // instala o tap para capturar
            inputNode!!.installTapOnBus(0u, BUFFER_SIZE, tapFmt) { buf, _ ->
                processAudioBuffer(buf)
            }

            // prepara e inicia
            eng.prepare()
            eng.startAndReturnError(null)
        }
    }

    /** Para captura */
    actual fun stop() {
        inputNode?.removeTapOnBus(0u)
        engine?.stop()
    }

    /** Extrai amostras, detecta pitch e envia callback */
    @OptIn(ExperimentalForeignApi::class)
    private fun processAudioBuffer(buffer: AVAudioPCMBuffer?) {
        if (buffer == null) return

        val channelData = buffer.floatChannelData?.get(0) ?: return
        val len = buffer.frameLength.toInt()
        val samples = FloatArray(len) { i -> channelData[i] }

        // auto-correlação + interpolação parabólica
        val freq = detectPitch(samples, buffer.format.sampleRate.toInt())
        if (freq in 20f..5_000f) {
            val smooth = smoothFrequency(freq)
            onFrequencyDetected(smooth)
        }
    }

    /** Auto-correlação + interp. parabólica */
    private fun detectPitch(samples: FloatArray, sampleRate: Int): Float {
        val minFreq = 60
        val maxFreq = 1_000
        val minLag  = sampleRate / maxFreq
        val maxLag  = sampleRate / minFreq
        if (samples.size < maxLag + 2) return 0f

        // remove DC
        val mean = samples.average().toFloat()
        samples.indices.forEach { samples[it] -= mean }

        var bestLag = -1
        var bestCorr = 0f
        val corr = FloatArray(maxLag + 3)

        for (lag in minLag..maxLag) {
            var sum = 0f
            for (j in 0 until samples.size - lag) {
                sum += samples[j] * samples[j + lag]
            }
            corr[lag] = sum
            if (sum > bestCorr) {
                bestCorr = sum
                bestLag = lag
            }
        }
        if (bestLag <= 0 || bestCorr < 0.01f) return 0f

        val r1 = corr.getOrElse(bestLag - 1) { corr[bestLag] }
        val r2 = corr[bestLag]
        val r3 = corr.getOrElse(bestLag + 1) { corr[bestLag] }
        val denom = 2f * r2 - r1 - r3
        val delta = if (denom == 0f) 0f else (r1 - r3) / denom
        return sampleRate.toFloat() / (bestLag + delta)
    }

    /** Média móvel simples para suavizar */
    private fun smoothFrequency(f: Float): Float {
        if (freqBuffer.size >= SMOOTH_WINDOW) freqBuffer.removeAt(0)
        freqBuffer.add(f)
        return freqBuffer.average().toFloat()
    }
}