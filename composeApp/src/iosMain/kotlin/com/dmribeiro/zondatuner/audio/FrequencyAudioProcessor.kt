package com.dmribeiro.zondatuner.audio



import com.dmribeiro.zondatuner.utils.TonePlayer

import kotlinx.cinterop.ExperimentalForeignApi

import kotlinx.cinterop.ObjCObjectVar

import kotlinx.cinterop.get

import platform.AVFAudio.AVAudioEngine

import platform.AVFAudio.AVAudioFormat

import platform.AVFAudio.AVAudioInputNode

import platform.AVFAudio.AVAudioNodeBus

import platform.AVFAudio.AVAudioPCMBuffer

import platform.AVFAudio.AVAudioPCMFormatFloat32

import platform.AVFAudio.AVAudioSession

import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetooth

import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers

import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord

import platform.AVFAudio.AVAudioSessionPortOverrideNone

import platform.AVFAudio.AVAudioSessionPortOverrideSpeaker

import platform.AVFAudio.setActive

import platform.AVFAudio.setPreferredIOBufferDuration

import platform.AVFAudio.setPreferredSampleRate

import platform.Foundation.NSError



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

        val session = AVAudioSession.sharedInstance()



        session.setCategory(

            category = AVAudioSessionCategoryPlayAndRecord,

            withOptions = AVAudioSessionCategoryOptionAllowBluetooth or

                    AVAudioSessionCategoryOptionMixWithOthers,

            error = null

        )



        session.setPreferredSampleRate(SAMPLE_RATE.toDouble(), error = null)

        session.setPreferredIOBufferDuration(IO_BUFFER_MS, error = null)



// Ativa a sessão primeiro

        session.setActive(true, error = null)



// Só depois força o viva-voz

        session.overrideOutputAudioPort(AVAudioSessionPortOverrideSpeaker, error = null)

    }



    /* ---------- iniciar captura ---------- */

    @OptIn(ExperimentalForeignApi::class)

    actual fun start() {

        configureSession()



        audioEngine = AVAudioEngine()

        inputNode = audioEngine!!.inputNode



        val hwFormat = inputNode!!.inputFormatForBus(bus)

        currentSampleRate = hwFormat.sampleRate.toInt()



        val tapFormat = AVAudioFormat(

            commonFormat = AVAudioPCMFormatFloat32,

            sampleRate = hwFormat.sampleRate,

            channels = 1u,

            interleaved = false

        )



        inputNode!!.installTapOnBus(bus, BUFFER_SIZE.toUInt(), tapFormat) { buf, _ ->

            processAudioBuffer(buf)

        }



// 🔑 Compartilha o engine

        audioEngine!!.prepare()

        audioEngine!!.startAndReturnError(null)



// 🔑 Inicializa player

//TonePlayer.setup(audioEngine!!)

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

        val frameLength = buffer.frameLength.toInt()



        val samples = FloatArray(frameLength) { i ->

            channelData[0]!!.get(i)

        }



        val freq = detectPitch(samples, currentSampleRate)

        println("Detected raw freq: $freq Hz")



        if (freq in 20f..5_000f) {

            val smooth = smoothFrequency(freq)

            onFrequencyDetected(smooth)

        }

    }



    /* ------------- autocorrelação + interpolação segura ------------- */

    private fun detectPitch(samples: FloatArray, sampleRate: Int): Float {



        val minFreq = 60

        val maxFreq = 1_000

        val minLag = sampleRate / maxFreq // 44

        val maxLag = sampleRate / minFreq // 735

        val size = samples.size

        if (size < maxLag + 2) return 0f



// remove DC

        val mean = samples.average().toFloat()

        for (i in samples.indices) samples[i] -= mean



        var bestLag = -1

        var bestCorr = 0f

        val corr = FloatArray(maxLag + 3) // +3 evita estouro (+1 e -1)



        /* --------- correlação direta (não normalizada) --------- */

        for (lag in minLag..maxLag) {

            var sum = 0f

            var j = 0

            while (j < size - lag) {

                sum += samples[j] * samples[j + lag]

                j++

            }

            corr[lag] = sum

            if (sum > bestCorr) {

                bestCorr = sum

                bestLag = lag

            }

        }



        if (bestLag <= 0 || bestCorr < 0.01f) return 0f



        /* --------- interpolação parabólica – proteção nas bordas --------- */

        val r1 = if (bestLag > 0) corr[bestLag - 1] else corr[bestLag]

        val r2 = corr[bestLag]

        val r3 = if (bestLag < maxLag) corr[bestLag + 1] else corr[bestLag]



        val denom = 2f * r2 - r1 - r3 // pode ser 0

        val delta = if (denom == 0f) 0f else (r1 - r3) / denom // –0.5…+0.5



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

        private const val SAMPLE_RATE = 44_100 // Hz

        private const val BUFFER_SIZE = 2_048 // amostras

        private const val IO_BUFFER_MS = 0.008 // 8 ms

        private const val SMOOTH_WINDOW = 3

    }

}