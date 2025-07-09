package com.dmribeiro.zondatuner

import android.app.Application
import com.dmribeiro.zondatuner.di.initKoin
import org.koin.android.ext.koin.androidContext

class TunerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin{
            androidContext(this@TunerApp)
        }
    }
}