package com.dmribeiro.zondatuner.audio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dmribeiro.zondatuner.AppLogger
import com.dmribeiro.zondatuner.utils.alignFrequencyToTargetOctave
import com.dmribeiro.zondatuner.utils.centsToGaugeAngleDegrees
import com.dmribeiro.zondatuner.utils.frequencyDeltaCents
import com.dmribeiro.zondatuner.utils.formatTunerHz
import kotlin.math.abs
import kotlin.math.round
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Diagnóstico temporário do afinador — logs (Logcat / Xcode) + overlay na UI.
 * Filtrar por tag `TunerNeedle` no Android Studio ou console do Xcode.
 */
class TunerDiagnostics(
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    data class Snapshot(
        val rawHz: Float = 0f,
        val smoothedHz: Float = 0f,
        val heldHz: Float = 0f,
        val uiHz: Float = 0f,
        val targetHz: Float = 0f,
        val alignedHz: Float = 0f,
        val rawCents: Float = 0f,
        val cents: Float = 0f,
        val clampedCents: Float = 0f,
        val needleCents: Float = 0f,
        val needleAngleDeg: Float = 270f,
        val signalActive: Boolean = false,
        val inTune: Boolean = false,
        val lastReason: String = "",
    )

    var snapshot by mutableStateOf(Snapshot())
        private set

    private var lastLogMark: TimeMark? = null
    private var lastLoggedCents = 0f
    private var lastLoggedNeedle = 0f

    fun onPipeline(
        rawHz: Float,
        smoothedHz: Float,
        heldHz: Float,
        uiHz: Float,
        targetHz: Float,
        signalActive: Boolean,
        inTune: Boolean,
    ) {
        if (!isEnabled) return

        val alignedHz = alignFrequencyToTargetOctave(uiHz, targetHz)
        val rawCents = if (uiHz > 0f && targetHz > 0f) frequencyDeltaCents(uiHz, targetHz) else 0f
        val cents = if (alignedHz > 0f && targetHz > 0f) frequencyDeltaCents(alignedHz, targetHz) else 0f

        snapshot = snapshot.copy(
            rawHz = rawHz,
            smoothedHz = smoothedHz,
            heldHz = heldHz,
            uiHz = uiHz,
            targetHz = targetHz,
            alignedHz = alignedHz,
            rawCents = rawCents,
            cents = cents,
            signalActive = signalActive,
            inTune = inTune,
        )

        val octaveCorrected = abs(rawCents - cents) > 8f
        val reason = when {
            !signalActive && uiHz <= 0f -> "sem_sinal"
            octaveCorrected -> "oitava_corrigida"
            else -> "pipeline"
        }
        maybeLog(reason)
    }

    fun onNeedleTarget(clampedCents: Float, snapped: Boolean) {
        if (!isEnabled) return
        snapshot = snapshot.copy(clampedCents = clampedCents)
        maybeLog(if (snapped) "ponteiro_snap" else "ponteiro_alvo")
    }

    fun onNeedleFrame(needleCents: Float) {
        if (!isEnabled) return
        snapshot = snapshot.copy(
            needleCents = needleCents,
            needleAngleDeg = centsToGaugeAngleDegrees(needleCents),
        )
        maybeLog("ponteiro_frame")
    }

    fun formatOverlay(): String {
        val s = snapshot
        return buildString {
            append("raw ${s.rawHz.formatTunerHz()} → ui ${s.uiHz.formatTunerHz()}")
            append(" | aln ${s.alignedHz.formatTunerHz()} / tgt ${s.targetHz.formatTunerHz()}")
            append('\n')
            append("cents bruto ${s.rawCents.formatCents()} → alinh ${s.cents.formatCents()}")
            append(" | clamp ${s.clampedCents.formatCents()}")
            append('\n')
            append("ponteiro ${s.needleCents.formatCents()} (${s.needleAngleDeg.formatAngle()}°)")
            append(" | sinal=${s.signalActive.toOnOff()} afinado=${s.inTune.toOnOff()}")
            if (s.lastReason.isNotEmpty()) append(" | ${s.lastReason}")
        }
    }

    private fun maybeLog(reason: String) {
        val now = timeSource.markNow()
        val centsJump = abs(snapshot.cents - lastLoggedCents)
        val needleJump = abs(snapshot.needleCents - lastLoggedNeedle)
        val elapsed = lastLogMark?.elapsedNow() ?: 1_000.milliseconds
        val urgent = reason.contains("snap") ||
            reason == "sem_sinal" ||
            reason == "oitava_corrigida" ||
            centsJump >= 8f ||
            needleJump >= 12f

        if (!urgent && elapsed < logInterval) return

        lastLogMark = now
        lastLoggedCents = snapshot.cents
        lastLoggedNeedle = snapshot.needleCents
        snapshot = snapshot.copy(lastReason = reason)

        AppLogger.d(TAG, formatLogLine(reason))
    }

    private fun formatLogLine(reason: String): String {
        val s = snapshot
        return buildString {
            append("[$reason] ")
            append("raw=${s.rawHz.formatTunerHz()} ")
            append("smooth=${s.smoothedHz.formatTunerHz()} ")
            append("hold=${s.heldHz.formatTunerHz()} ")
            append("ui=${s.uiHz.formatTunerHz()} ")
            append("tgt=${s.targetHz.formatTunerHz()} ")
            append("aln=${s.alignedHz.formatTunerHz()} ")
            append("rawCents=${s.rawCents.formatCents()} ")
            append("cents=${s.cents.formatCents()} ")
            append("clamp=${s.clampedCents.formatCents()} ")
            append("needle=${s.needleCents.formatCents()} ")
            append("angle=${s.needleAngleDeg.formatAngle()} ")
            append("signal=${s.signalActive.toOnOff()} ")
            append("inTune=${s.inTune.toOnOff()}")
        }
    }

    private fun Float.formatCents(): String {
        val rounded = round(this * 10f) / 10f
        return if (rounded >= 0f) "+$rounded" else "$rounded"
    }

    private fun Float.formatAngle(): String {
        val rounded = round(this * 10f) / 10f
        return "$rounded"
    }
    private fun Boolean.toOnOff(): String = if (this) "1" else "0"

    companion object {
        const val TAG = "TunerNeedle"
        const val isEnabled = true
        private val logInterval = 250.milliseconds
    }
}
