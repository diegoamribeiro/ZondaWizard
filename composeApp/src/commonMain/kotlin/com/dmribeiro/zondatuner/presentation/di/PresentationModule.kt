package com.dmribeiro.zondatuner.presentation.di

import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUiMapper
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel
import org.koin.dsl.module


val presentationModule = module {
    factory { TuningDataUiMapper() }
    factory { HomeScreenModel(get(), get(), get(), get(), get()) }
}