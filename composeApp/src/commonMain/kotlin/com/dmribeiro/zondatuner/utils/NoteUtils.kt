package com.dmribeiro.zondatuner.utils

private val CHROMATIC_NOTES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

private val ENHARMONIC_TO_SHARP = mapOf(
    "Db" to "C#",
    "Eb" to "D#",
    "Fb" to "E",
    "Gb" to "F#",
    "Ab" to "G#",
    "Bb" to "A#",
    "Cb" to "B",
    "E#" to "F",
    "B#" to "C",
)

fun normalizeNote(note: String): String = ENHARMONIC_TO_SHARP[note] ?: note

fun chromaticIndex(note: String): Int {
    val normalized = normalizeNote(note)
    return CHROMATIC_NOTES.indexOf(normalized).takeIf { it >= 0 } ?: 0
}

fun chromaticNotes(): List<String> = CHROMATIC_NOTES
