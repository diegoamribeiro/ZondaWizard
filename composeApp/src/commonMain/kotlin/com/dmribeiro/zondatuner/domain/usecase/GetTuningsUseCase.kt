package com.dmribeiro.zondatuner.domain.usecase

import com.dmribeiro.zondatuner.domain.model.Tuning
import com.dmribeiro.zondatuner.domain.repository.TuningRepository
import kotlinx.coroutines.flow.Flow


class GetTuningsUseCase(
    private val repository: TuningRepository
) {
    operator fun invoke(): Flow<List<Tuning>> = repository.getAllTunings()
}