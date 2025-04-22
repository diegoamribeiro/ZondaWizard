package com.dmribeiro.zondatuner.di

import com.dmribeiro.zondatuner.domain.di.domainModule
import com.dmribeiro.zondatuner.presentation.di.presentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin{
        config?.invoke(this)
        modules(dataModule, platformModule, presentationModule, domainModule)
    }
}