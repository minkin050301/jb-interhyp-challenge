package de.tum.hack.jb.interhyp.challenge

import android.app.Application
import de.tum.hack.jb.interhyp.challenge.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@MainApplication)
            modules(appModule)
        }
    }
}

