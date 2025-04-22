package com.dmribeiro.zondatuner.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import platform.AVFAudio.*
import platform.CoreAudioTypes.kLinearPCMFormatFlagIsFloat
import platform.Foundation.NSLog
import kotlin.math.*

actual class FrequencyAudioProcessor actual constructor(
    private val onFrequencyDetected: (Float) -> Unit
) {

    private var audioEngine: AVAudioEngine? = null
    private var inputNode: AVAudioInputNode? = null
    private var audioFormat: AVAudioFormat? = null
    private val bus: AVAudioNodeBus = 0u

    private val frequencyBuffer = mutableListOf<Float>() // 🔹 Buffer para suavização dos dados

    @OptIn(ExperimentalForeignApi::class)
    actual fun start() {
        try {
            audioEngine = AVAudioEngine()
            inputNode = audioEngine?.inputNode

            // Configura um formato de áudio válido manualmente
            audioFormat = AVAudioFormat(
                commonFormat = kLinearPCMFormatFlagIsFloat.toULong(),
                sampleRate = SAMPLE_RATE.toDouble(),
                channels = 1u,
                interleaved = false
            )
            if (inputNode == null || audioFormat == null) {
                NSLog("❌ Erro: inputNode ou audioFormat está nulo!")
                return
            }

            // 🔹 Adiciona um Tap no inputNode para capturar áudio
            inputNode?.installTapOnBus(bus, bufferSize = BUFFER_SIZE.toUInt(), format = audioFormat) { buffer, _ ->
                processAudioBuffer(buffer)
            }

            NSLog("🔹 Iniciando processamento de áudio no iOS...")
            audioEngine?.prepare()
            try {
                audioEngine?.startAndReturnError(null)
                NSLog("✅ AudioEngine iniciado com sucesso!")
            } catch (e: Exception) {
                NSLog("❌ Erro ao iniciar o AudioEngine: ${e.message}")
            }
        } catch (e: Exception) {
            NSLog("❌ Erro ao configurar o AVAudioEngine: ${e.message}")
        }
    }

    actual fun stop() {
        inputNode?.removeTapOnBus(bus)
        audioEngine?.stop()
        NSLog("🛑 AudioEngine parado!")
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun processAudioBuffer(buffer: AVAudioPCMBuffer?) {
        buffer ?: return
        val channelData = buffer.floatChannelData ?: return
        val frameLength = buffer.frameLength.toInt()

        val samples = FloatArray(frameLength) { i ->
            channelData[0]?.get(i) ?: 0.0f
        }

        // 🔹 Calcula a frequência dominante
        val detectedFrequency = detectPitch(samples, SAMPLE_RATE)

        // 🔹 Filtro de ruído - ignora frequências irreais
        if (detectedFrequency < 20 || detectedFrequency > 5000) return

        // 🔹 Aplica suavização com média móvel para evitar jitter
        val smoothedFrequency = smoothFrequency(detectedFrequency)
        println("📡 Frequência Detectada: $detectedFrequency Hz")

        // 🔹 Somente envia se houver uma frequência válida
        if (smoothedFrequency > 0) {
            NSLog("🎵 Frequência suavizada detectada: ${smoothedFrequency} Hz")
            onFrequencyDetected(smoothedFrequency)
        }
    }

    private fun detectPitch(samples: FloatArray, sampleRate: Int): Float {
        var maxIndex = 0
        var maxAmplitude = 0f

        for (i in samples.indices) {
            val amplitude = abs(samples[i])
            if (amplitude > maxAmplitude) {
                maxAmplitude = amplitude
                maxIndex = i
            }
        }

        return if (maxAmplitude > 0.01f) { // 🔹 Filtro básico de ruído
            sampleRate.toFloat() / maxIndex.toFloat()
        } else {
            0f
        }
    }

    private fun smoothFrequency(frequency: Float): Float {
        // 🔹 Mantém um histórico das últimas 5 medições
        if (frequencyBuffer.size >= 5) {
            frequencyBuffer.removeAt(0)
        }
        frequencyBuffer.add(frequency)

        // 🔹 Calcula a média das últimas medições
        return frequencyBuffer.average().toFloat()
    }

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
    }
}