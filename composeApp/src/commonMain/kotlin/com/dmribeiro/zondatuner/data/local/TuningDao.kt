package com.dmribeiro.zondatuner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query(
        """
    UPDATE tunings 
    SET name = :name, 
        description = :description,
        string6Frequency = :string6Frequency,
        string6Note = :string6Note,
        string5Frequency = :string5Frequency,
        string5Note = :string5Note,
        string4Frequency = :string4Frequency,
        string4Note = :string4Note,
        string3Frequency = :string3Frequency,
        string3Note = :string3Note,
        string2Frequency = :string2Frequency,
        string2Note = :string2Note,
        string1Frequency = :string1Frequency,
        string1Note = :string1Note
    WHERE id = :id
"""
    )
    suspend fun updateTuning(
        id: Long,
        name: String,
        description: String,
        string6Frequency: Float,
        string6Note: String,
        string5Frequency: Float,
        string5Note: String,
        string4Frequency: Float,
        string4Note: String,
        string3Frequency: Float,
        string3Note: String,
        string2Frequency: Float,
        string2Note: String,
        string1Frequency: Float,
        string1Note: String
    )
}