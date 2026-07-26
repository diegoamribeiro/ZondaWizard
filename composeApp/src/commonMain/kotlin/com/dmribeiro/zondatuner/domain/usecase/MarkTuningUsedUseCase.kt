package com.dmribeiro.zondatuner.domain.usecase

import com.dmribeiro.zondatuner.domain.repository.TuningRepository

class MarkTuningUsedUseCase(private val repository: TuningRepository) {
    suspend operator fun invoke(id: Long) = repository.markTuningUsed(id)
}
