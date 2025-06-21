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

    override suspend fun deleteTuning(id: Long) {
        dao.deleteTuning(id)
    }

    override suspend fun insertTuning(tuning: Tuning) {
        val entity = mapper.toEntity(tuning).copy(id = 0)
        dao.insertTuning(entity)
    }

    override suspend fun updateTuning(tuning: Tuning) {
        val entity = mapper.toEntity(tuning)
        dao.updateTuning(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            string6Frequency = entity.string6Frequency,
            string6Note = entity.string6Note,
            string5Frequency = entity.string5Frequency,
            string5Note = entity.string5Note,
            string4Frequency = entity.string4Frequency,
            string4Note = entity.string4Note,
            string3Frequency = entity.string3Frequency,
            string3Note = entity.string3Note,
            string2Frequency = entity.string2Frequency,
            string2Note = entity.string2Note,
            string1Frequency = entity.string1Frequency,
            string1Note = entity.string1Note
        )
    }

}