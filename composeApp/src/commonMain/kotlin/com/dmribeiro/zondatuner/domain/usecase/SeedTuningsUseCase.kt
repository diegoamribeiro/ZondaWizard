package com.dmribeiro.zondatuner.domain.usecase

import com.dmribeiro.zondatuner.domain.repository.TuningRepository

class SeedTuningsUseCase(
    private val repository: TuningRepository
) {
    suspend operator fun invoke() {
        repository.ensureInitialTuningsInserted()
    }
}