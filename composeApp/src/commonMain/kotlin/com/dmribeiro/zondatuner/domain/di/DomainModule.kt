package com.dmribeiro.zondatuner.domain.di

import com.dmribeiro.zondatuner.domain.repository.TuningRepository
import com.dmribeiro.zondatuner.domain.usecase.DeleteTuningUseCase
import com.dmribeiro.zondatuner.domain.usecase.GetTuningsUseCase
import com.dmribeiro.zondatuner.domain.usecase.InsertTuningUseCase
import com.dmribeiro.zondatuner.domain.usecase.UpdateTuningUseCase
import org.koin.dsl.module


val domainModule = module {
    factory { GetTuningsUseCase(get<TuningRepository>()) }
    factory { InsertTuningUseCase(get<TuningRepository>()) }
    factory { DeleteTuningUseCase(get<TuningRepository>()) }
    factory { UpdateTuningUseCase(get<TuningRepository>()) }
}