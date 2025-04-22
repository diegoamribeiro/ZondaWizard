package com.dmribeiro.zondatuner.domain.repository

import com.dmribeiro.zondatuner.domain.model.Tuning
import kotlinx.coroutines.flow.Flow

interface TuningRepository {
    fun getAllTunings(): Flow<List<Tuning>>
    suspend fun insertTuning(tuning: Tuning)
    suspend fun deleteTuning(id: Long)
    suspend fun updateTuning(tuning: Tuning)
}