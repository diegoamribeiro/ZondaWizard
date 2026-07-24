// iosMain/kotlin/com/dmribeiro/zondatuner/audio/FrequencyAudioProcessor.kt
package com.dmribeiro.zondatuner.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import platform.AVFAudio.*
import kotlin.math.sqrt

actual class FrequencyAudioProcessor actual constructor(
    private val onFrequencyDetected: (Float) -> Unit
) {
    private var engine: AVAudioEngine? = null
    private var inputNode: AVAudioInputNode? = null
    private var yinPitchDetector: YinPitchDetector? = null

    companion object {
        private const val SAMPLE_RATE = 44_100.0
        private const val BUFFER_SIZE = 2_048u
        private const val MIN_RMS = 0.0015f
        private const val MIN_FREQUENCY = 40f
        private const val MAX_FREQUENCY = 1_500f
    }

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

    @OptIn(ExperimentalForeignApi::class)
    actual fun start() {
        configureSession()

        engine = AVAudioEngine().also { eng ->
            inputNode = eng.inputNode

            val hwFmt = inputNode!!.inputFormatForBus(0u)
            yinPitchDetector = YinPitchDetector(hwFmt.sampleRate.toFloat())
            val tapFmt = AVAudioFormat(
                commonFormat = AVAudioPCMFormatFloat32.toULong(),
                sampleRate = hwFmt.sampleRate,
                channels = 1u,
                interleaved = false
            )

            inputNode!!.installTapOnBus(0u, BUFFER_SIZE, tapFmt) { buf, _ ->
                processAudioBuffer(buf)
            }

            eng.prepare()
            eng.startAndReturnError(null)
        }
    }

    actual fun stop() {
        inputNode?.removeTapOnBus(0u)
        engine?.stop()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun processAudioBuffer(buffer: AVAudioPCMBuffer?) {
        if (buffer == null) return

        val channelData = buffer.floatChannelData?.get(0) ?: return
        val len = buffer.frameLength.toInt()
        if (len == 0) return

        val samples = FloatArray(len) { i -> channelData[i] }
        val rms = calculateRms(samples)
        if (rms < MIN_RMS) return

        val detector = yinPitchDetector ?: YinPitchDetector(buffer.format.sampleRate.toFloat())
        val freq = detector.detectPitch(samples)
        if (freq in MIN_FREQUENCY..MAX_FREQUENCY) {
            onFrequencyDetected(freq)
        }
    }

    private fun calculateRms(samples: FloatArray): Float {
        var sum = 0.0
        for (sample in samples) {
            sum += sample * sample
        }
        return sqrt(sum / samples.size).toFloat()
    }
}
