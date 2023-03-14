package com.madalin.notelo

import android.app.Application
import android.content.Context

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
        context = applicationContext
    }
}