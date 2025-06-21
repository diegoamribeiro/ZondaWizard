package com.dmribeiro.zondatuner.data

import com.dmribeiro.zondatuner.data.local.AppDatabase
import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.domain.model.Tuning
import com.dmribeiro.zondatuner.domain.repository.TuningRepository
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUiMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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

    override suspend fun ensureInitialTuningsInserted() {
        val currentTunings = getAllTunings().firstOrNull() ?: emptyList()
        if (currentTunings.isEmpty()) {
            getDefaultTunings().forEach { insertTuning(it) }
        }
    }

    private fun getDefaultTunings(): List<Tuning> {
        return listOf(
            Tuning(
                id = 0,
                name = "Padrão (EADGBE)",
                description = "Afinação padrão da guitarra",
                strings = listOf(
                    GuitarString(6, 82.41f, "E", 0),
                    GuitarString(5, 110.00f, "A", 0),
                    GuitarString(4, 146.83f, "D", 0),
                    GuitarString(3, 196.00f, "G", 0),
                    GuitarString(2, 246.94f, "B", 0),
                    GuitarString(1, 329.63f, "E", 0)
                )
            ),
            Tuning(
                id = 0,
                name = "Drop D# (Charlie Brown Jr)",
                description = "Drop D# do Charlie Brown Jr",
                strings = listOf(
                    GuitarString(6, 77.78f, "D#", 0),
                    GuitarString(5, 116.54f, "G#", 0),
                    GuitarString(4, 155.56f, "C#", 0),
                    GuitarString(3, 207.65f, "F#", 0),
                    GuitarString(2, 233.08f, "A#", 0),
                    GuitarString(1, 311.13f, "D#", 0)
                )
            ),
            Tuning(
                id = 0,
                name = "Aerials (System of a Down)",
                description = "C G C F A D",
                strings = listOf(
                    GuitarString(6, 65.41f, "C", 0),
                    GuitarString(5, 98.00f, "G", 0),
                    GuitarString(4, 130.81f, "C", 0),
                    GuitarString(3, 174.61f, "F", 0),
                    GuitarString(2, 220.00f, "A", 0),
                    GuitarString(1, 293.66f, "D", 0)
                )
            ),
            Tuning(
                id = 0,
                name = "My Sacrifice (Creed)",
                description = "D A D A D D",
                strings = listOf(
                    GuitarString(6, 73.42f, "D", 0),
                    GuitarString(5, 110.00f, "A", 0),
                    GuitarString(4, 146.83f, "D", 0),
                    GuitarString(3, 110.00f, "A", 0),
                    GuitarString(2, 146.83f, "D", 0),
                    GuitarString(1, 146.83f, "D", 0)
                )
            )
        )
    }

}