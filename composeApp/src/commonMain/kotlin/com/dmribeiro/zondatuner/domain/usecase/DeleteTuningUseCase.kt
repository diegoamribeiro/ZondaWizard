package com.dmribeiro.zondatuner.domain.usecase

import com.dmribeiro.zondatuner.domain.repository.TuningRepository

class DeleteTuningUseCase(
    private val repository: TuningRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteTuning(id)
    }
}