package com.dmribeiro.zondatuner.di

import com.dmribeiro.zondatuner.data.local.getDatabaseBuilder
import org.koin.dsl.module

actual val platformModule = module {
    single { getDatabaseBuilder(get()) }
}