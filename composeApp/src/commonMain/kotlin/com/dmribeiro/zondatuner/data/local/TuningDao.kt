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
    suspend fun insertTuning(tuning: TuningEntity)

    @Query("DELETE FROM tunings WHERE id = :id")
    suspend fun deleteTuning(id: Long)

    @Query("UPDATE tunings SET name = :name, description = :description WHERE id = :id")
    suspend fun updateTuning(id: Long, name: String, description: String)
}