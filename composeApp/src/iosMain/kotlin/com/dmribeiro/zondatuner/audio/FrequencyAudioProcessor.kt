package com.dmribeiro.zondatuner.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import platform.AVFAudio.*
import platform.CoreAudioTypes.kLinearPCMFormatFlagIsFloat
import kotlin.math.*

actual class FrequencyAudioProcessor actual constructor(
    private val onFrequencyDetected: (Float) -> Unit
) {
    private var audioEngine: AVAudioEngine? = null
    private var inputNode : AVAudioInputNode? = null
    private var audioFormat: AVAudioFormat? = null
    private val bus: AVAudioNodeBus = 0u
    private var currentSampleRate = 44_100

    private val frequencyBuffer = mutableListOf<Float>()

    /* ---------- sessão de áudio com latência baixa ---------- */
    @OptIn(ExperimentalForeignApi::class)
    private fun configureSession() {
        val s = AVAudioSession.sharedInstance()

        s.setCategory(
            category    = AVAudioSessionCategoryPlayAndRecord,
            withOptions = AVAudioSessionCategoryOptionAllowBluetooth,
            error       = null
        )
        s.setPreferredSampleRate(SAMPLE_RATE.toDouble(),           error = null)
        s.setPreferredIOBufferDuration(IO_BUFFER_MS,               error = null)
        s.setActive(true,                                          error = null)
    }

    /* ---------- iniciar captura ---------- */
    @OptIn(ExperimentalForeignApi::class)
    actual fun start() {

        configureSession()                        // mantém

        audioEngine = AVAudioEngine()
        inputNode   = audioEngine!!.inputNode

        // ► formato REAL que o hardware abriu
        val hwFormat = inputNode!!.inputFormatForBus(bus)
        val hwSampleRate = hwFormat.sampleRate.toInt()      // 44 100 ou 48 000

        // ► guarda para usar na autocorrelação
        currentSampleRate = hwSampleRate

        // ► se quiser forçar Float32, crie novo formato **com o mesmo sample-rate**
        val tapFormat = AVAudioFormat(
            commonFormat = AVAudioPCMFormatFloat32.toULong(),
            sampleRate   = hwFormat.sampleRate,
            channels     = 1u,
            interleaved  = false
        )
        inputNode!!.installTapOnBus(
            bus,
            bufferSize = BUFFER_SIZE.toUInt(),
            format     = tapFormat          // ← sample-rate agora coincide
        ) { buffer, _ -> processAudioBuffer(buffer) }

        audioEngine!!.prepare()
        audioEngine!!.startAndReturnError(null)
    }

    actual fun stop() {
        inputNode?.removeTapOnBus(bus)
        audioEngine?.stop()
    }

    /* ---------- processamento FFT/autocorrelação ---------- */
    @OptIn(ExperimentalForeignApi::class)
    private fun processAudioBuffer(buffer: AVAudioPCMBuffer?) {
        buffer ?: return
        val channelData = buffer.floatChannelData ?: return
        val frameLength = buffer.frameLength.toInt()          // **NÃO** cortar!

        val samples = FloatArray(frameLength) { i ->
            channelData[0]!!.get(i)
        }

        val freq = detectPitch(samples, SAMPLE_RATE)
        if (freq in 20f..5_000f) {
            val smooth = smoothFrequency(freq)
            onFrequencyDetected(smooth)
        }
    }

    /* ---------- autocorrelação com interpolação parabólica ---------- */
    /* ------------- autocorrelação + interpolação segura ------------- */
    private fun detectPitch(samples: FloatArray, sampleRate: Int): Float {

        val minFreq = 60
        val maxFreq = 1_000
        val minLag  = sampleRate / maxFreq        // 44
        val maxLag  = sampleRate / minFreq        // 735
        val size    = samples.size
        if (size < maxLag + 2) return 0f

        // remove DC
        val mean = samples.average().toFloat()
        for (i in samples.indices) samples[i] -= mean

        var bestLag  = -1
        var bestCorr = 0f
        val corr = FloatArray(maxLag + 3)         // +3 evita estouro (+1 e -1)

        /* --------- correlação direta (não normalizada) --------- */
        for (lag in minLag..maxLag) {
            var sum = 0f
            var j   = 0
            while (j < size - lag) {
                sum += samples[j] * samples[j + lag]
                j++
            }
            corr[lag] = sum
            if (sum > bestCorr) {
                bestCorr = sum
                bestLag  = lag
            }
        }

        if (bestLag <= 0 || bestCorr < 0.01f) return 0f

        /* --------- interpolação parabólica – proteção nas bordas --------- */
        val r1 = if (bestLag > 0)       corr[bestLag - 1] else corr[bestLag]
        val r2 =                         corr[bestLag]
        val r3 = if (bestLag < maxLag) corr[bestLag + 1] else corr[bestLag]

        val denom = 2f * r2 - r1 - r3            // pode ser 0
        val delta = if (denom == 0f) 0f else (r1 - r3) / denom   // –0.5…+0.5

        val refinedLag = bestLag + delta
        return sampleRate.toFloat() / refinedLag
    }

    /* ---------- média móvel p/ suavizar ---------- */
    private fun smoothFrequency(f: Float): Float {
        if (frequencyBuffer.size >= SMOOTH_WINDOW) frequencyBuffer.removeAt(0)
        frequencyBuffer.add(f)
        return frequencyBuffer.average().toFloat()
    }

    companion object {
        private const val SAMPLE_RATE  = 44_100          // Hz
        private const val BUFFER_SIZE  = 2_048           // amostras
        private const val IO_BUFFER_MS = 0.008           // 8 ms
        private const val SMOOTH_WINDOW = 3
    }
}