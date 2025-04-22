package com.dmribeiro.zondatuner.di

import com.dmribeiro.zondatuner.data.TuningRepositoryImpl
import com.dmribeiro.zondatuner.data.local.getRoomDatabase
import com.dmribeiro.zondatuner.domain.repository.TuningRepository
import org.koin.core.module.Module
import org.koin.dsl.module


expect val platformModule: Module

val dataModule = module {
    single { getRoomDatabase(get()) }
    single<TuningRepository> { TuningRepositoryImpl(get(), get()) }
}