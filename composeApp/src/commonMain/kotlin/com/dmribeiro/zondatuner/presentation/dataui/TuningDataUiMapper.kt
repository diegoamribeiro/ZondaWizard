package com.dmribeiro.zondatuner.presentation.dataui

import com.dmribeiro.zondatuner.domain.model.GuitarString
import com.dmribeiro.zondatuner.domain.model.Tuning
import com.dmribeiro.zondatuner.domain.data.local.TuningEntity

class TuningDataUiMapper {

    fun fromEntity(entity: TuningEntity): Tuning {
        return Tuning(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            strings = listOf(
                GuitarString(6, entity.string6Frequency, entity.string6Note),
                GuitarString(5, entity.string5Frequency, entity.string5Note),
                GuitarString(4, entity.string4Frequency, entity.string4Note),
                GuitarString(3, entity.string3Frequency, entity.string3Note),
                GuitarString(2, entity.string2Frequency, entity.string2Note),
                GuitarString(1, entity.string1Frequency, entity.string1Note)
            )
        )
    }

    // 🔹 Converte de `Tuning` (domínio) para `TuningEntity` (banco de dados)
    fun toEntity(tuning: Tuning): TuningEntity {
        return TuningEntity(
            id = tuning.id,
            name = tuning.name,
            description = tuning.description ?: "",
            string6Frequency = tuning.strings[0].frequency,
            string6Note = tuning.strings[0].note,
            string5Frequency = tuning.strings[1].frequency,
            string5Note = tuning.strings[1].note,
            string4Frequency = tuning.strings[2].frequency,
            string4Note = tuning.strings[2].note,
            string3Frequency = tuning.strings[3].frequency,
            string3Note = tuning.strings[3].note,
            string2Frequency = tuning.strings[4].frequency,
            string2Note = tuning.strings[4].note,
            string1Frequency = tuning.strings[5].frequency,
            string1Note = tuning.strings[5].note
        )
    }

    // 🔹 Converte `Tuning` (domínio) para `TuningDataUi` (UI)
    fun toObject(domainModel: Tuning): TuningDataUi {
        return TuningDataUi(
            id = domainModel.id,
            name = domainModel.name,
            description = domainModel.description ?: "",
            strings = domainModel.strings.map { guitarString ->
                GuitarString(
                    number = guitarString.number,
                    frequency = guitarString.frequency,
                    note = guitarString.note
                )
            }
        )
    }

    // 🔹 Converte `TuningDataUi` (UI) para `Tuning` (domínio)
    fun fromObject(dataUi: TuningDataUi): Tuning {
        return Tuning(
            id = dataUi.id,
            name = dataUi.name,
            description = dataUi.description,
            strings = dataUi.strings.map { guitarString ->
                GuitarString(
                    number = guitarString.number,
                    frequency = guitarString.frequency,
                    note = guitarString.note
                )
            }
        )
    }
}