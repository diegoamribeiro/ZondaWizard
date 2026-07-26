package com.dmribeiro.zondatuner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dmribeiro.zondatuner.domain.data.local.TuningEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TuningDao {
    @Query("SELECT * FROM tunings")
    fun getAllTunings(): Flow<List<TuningEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTuning(tuning: TuningEntity): Long

    @Query("DELETE FROM tunings WHERE id = :id")
    suspend fun deleteTuning(id: Long)

    // Contador crescente em vez de relógio: só a ordem importa.
    @Query("UPDATE tunings SET lastUsedAt = (SELECT IFNULL(MAX(lastUsedAt), 0) + 1 FROM tunings) WHERE id = :id")
    suspend fun markUsed(id: Long)

    @Query(
        """
    UPDATE tunings 
    SET name = :name, 
        description = :description,
        string6Frequency = :string6Frequency,
        string6Note = :string6Note,
        string6OctaveShift = :string6OctaveShift,
        string5Frequency = :string5Frequency,
        string5Note = :string5Note,
        string5OctaveShift = :string5OctaveShift,
        string4Frequency = :string4Frequency,
        string4Note = :string4Note,
        string4OctaveShift = :string4OctaveShift,
        string3Frequency = :string3Frequency,
        string3Note = :string3Note,
        string3OctaveShift = :string3OctaveShift,
        string2Frequency = :string2Frequency,
        string2Note = :string2Note,
        string2OctaveShift = :string2OctaveShift,
        string1Frequency = :string1Frequency,
        string1Note = :string1Note,
        string1OctaveShift = :string1OctaveShift
    WHERE id = :id
"""
    )
    suspend fun updateTuning(
        id: Long,
        name: String,
        description: String,
        string6Frequency: Float,
        string6Note: String,
        string6OctaveShift: Int,
        string5Frequency: Float,
        string5Note: String,
        string5OctaveShift: Int,
        string4Frequency: Float,
        string4Note: String,
        string4OctaveShift: Int,
        string3Frequency: Float,
        string3Note: String,
        string3OctaveShift: Int,
        string2Frequency: Float,
        string2Note: String,
        string2OctaveShift: Int,
        string1Frequency: Float,
        string1Note: String,
        string1OctaveShift: Int,
    )
}
