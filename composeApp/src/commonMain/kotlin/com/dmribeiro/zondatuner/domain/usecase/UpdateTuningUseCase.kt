package com.dmribeiro.zondatuner.domain.usecase

import com.dmribeiro.zondatuner.domain.model.Tuning
import com.dmribeiro.zondatuner.domain.repository.TuningRepository

class UpdateTuningUseCase(
    private val repository: TuningRepository
) {
    suspend operator fun invoke(tuning: Tuning) {
        repository.updateTuning(tuning)
    }
}