package com.madalin.notelo

import android.app.Application
import android.content.Context
import com.madalin.notelo.di.appModule
import com.madalin.notelo.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Class to provide a single point of entry for initialization and configuration tasks,
 * as well as to share data and resources between different components in the app.
 */
class ApplicationClass : Application() {
    companion object {
        lateinit var context: Context // used to provide a context where needed
    }

    override fun onCreate() {
        super.onCreate()

        // initialize Koin with the defined modules
        startKoin {
            androidLogger() // Koin logger
            androidContext(this@ApplicationClass)
            modules(appModule, viewModelModule)
        }

        context = applicationContext
    }
}