package com.dmribeiro.zondatuner.data

import com.dmribeiro.zondatuner.data.local.AppDatabase
import com.dmribeiro.zondatuner.domain.model.Tuning
import com.dmribeiro.zondatuner.domain.repository.TuningRepository
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUiMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TuningRepositoryImpl(

    db: AppDatabase,
    private val mapper: TuningDataUiMapper
) : TuningRepository {

    private val dao = db.tuningDao()

    override fun getAllTunings(): Flow<List<Tuning>> {
        return dao.getAllTunings().map { entityList ->
            entityList.map { entity -> mapper.fromEntity(entity) }
        }
    }

    override suspend fun insertTuning(tuning: Tuning) {
        dao.insertTuning(mapper.toEntity(tuning))
    }

    override suspend fun deleteTuning(id: Long) {
        dao.deleteTuning(id)
    }

    override suspend fun updateTuning(tuning: Tuning) {
        dao.updateTuning(tuning.id, tuning.name, tuning.description ?: "No description")
    }

}