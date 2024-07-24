package com.madalin.notelo.core.domain

import android.app.Application
import android.content.Context
import com.madalin.notelo.core.di.appModule
import com.madalin.notelo.core.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Class to provide a single point of entry for initialization and configuration tasks,
 * as well as to share data and resources between different components in the app.
 */
class NoteloApplication : Application() {
    companion object {
        lateinit var context: Context // used to provide a context where needed
    }

    override fun onCreate() {
        super.onCreate()

        // initialize Koin with the defined modules
        startKoin {
            androidLogger() // Koin logger
            androidContext(this@NoteloApplication)
            modules(appModule, viewModelModule)
        }

        context = applicationContext
    }
}