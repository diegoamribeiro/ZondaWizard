package com.dmribeiro.zondatuner.presentation.dataui

import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.domain.model.Tuning
import com.dmribeiro.zondatuner.domain.data.local.TuningEntity
import com.dmribeiro.zondatuner.utils.normalizeNote

class TuningDataUiMapper {

    fun fromEntity(entity: TuningEntity): Tuning {
        return Tuning(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            strings = listOf(
                GuitarString(6, entity.string6Frequency, normalizeNote(entity.string6Note), entity.string6OctaveShift),
                GuitarString(5, entity.string5Frequency, normalizeNote(entity.string5Note), entity.string5OctaveShift),
                GuitarString(4, entity.string4Frequency, normalizeNote(entity.string4Note), entity.string4OctaveShift),
                GuitarString(3, entity.string3Frequency, normalizeNote(entity.string3Note), entity.string3OctaveShift),
                GuitarString(2, entity.string2Frequency, normalizeNote(entity.string2Note), entity.string2OctaveShift),
                GuitarString(1, entity.string1Frequency, normalizeNote(entity.string1Note), entity.string1OctaveShift),
            ),
            lastUsedAt = entity.lastUsedAt,
        )
    }

    fun toEntity(tuning: Tuning): TuningEntity {
        return TuningEntity(
            id = tuning.id,
            name = tuning.name,
            description = tuning.description ?: "",
            string6Frequency = tuning.strings[0].frequency,
            string6Note = normalizeNote(tuning.strings[0].note),
            string6OctaveShift = tuning.strings[0].octaveShift,
            string5Frequency = tuning.strings[1].frequency,
            string5Note = normalizeNote(tuning.strings[1].note),
            string5OctaveShift = tuning.strings[1].octaveShift,
            string4Frequency = tuning.strings[2].frequency,
            string4Note = normalizeNote(tuning.strings[2].note),
            string4OctaveShift = tuning.strings[2].octaveShift,
            string3Frequency = tuning.strings[3].frequency,
            string3Note = normalizeNote(tuning.strings[3].note),
            string3OctaveShift = tuning.strings[3].octaveShift,
            string2Frequency = tuning.strings[4].frequency,
            string2Note = normalizeNote(tuning.strings[4].note),
            string2OctaveShift = tuning.strings[4].octaveShift,
            string1Frequency = tuning.strings[5].frequency,
            string1Note = normalizeNote(tuning.strings[5].note),
            string1OctaveShift = tuning.strings[5].octaveShift,
        )
    }

    fun toObject(domainModel: Tuning): TuningDataUi {
        return TuningDataUi(
            id = domainModel.id,
            name = domainModel.name,
            description = domainModel.description ?: "",
            strings = domainModel.strings.map { guitarString ->
                GuitarString(
                    number = guitarString.number,
                    frequency = guitarString.frequency,
                    note = normalizeNote(guitarString.note),
                    octaveShift = guitarString.octaveShift
                )
            },
            lastUsedAt = domainModel.lastUsedAt,
        )
    }

    fun fromObject(dataUi: TuningDataUi): Tuning {
        return Tuning(
            id = dataUi.id,
            name = dataUi.name,
            description = dataUi.description,
            strings = dataUi.strings.map { guitarString ->
                GuitarString(
                    number = guitarString.number,
                    frequency = guitarString.frequency,
                    note = normalizeNote(guitarString.note),
                    octaveShift = guitarString.octaveShift
                )
            }
        )
    }
}
