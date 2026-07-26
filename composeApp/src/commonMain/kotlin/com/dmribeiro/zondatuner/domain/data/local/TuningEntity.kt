package com.dmribeiro.zondatuner.domain.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tunings")
data class TuningEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val string6Frequency: Float,
    val string6Note: String,
    val string6OctaveShift: Int = 0,
    val string5Frequency: Float,
    val string5Note: String,
    val string5OctaveShift: Int = 0,
    val string4Frequency: Float,
    val string4Note: String,
    val string4OctaveShift: Int = 0,
    val string3Frequency: Float,
    val string3Note: String,
    val string3OctaveShift: Int = 0,
    val string2Frequency: Float,
    val string2Note: String,
    val string2OctaveShift: Int = 0,
    val string1Frequency: Float,
    val string1Note: String,
    val string1OctaveShift: Int = 0,
    val lastUsedAt: Long = 0,
)
