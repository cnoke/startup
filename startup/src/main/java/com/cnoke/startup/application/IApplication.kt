package com.cnoke.startup.application

import android.content.Context
import android.content.res.Configuration

open interface IApplication {

    fun attachBaseContext(context: Context)

    fun onCreate()

    fun onTerminate()

    fun onLowMemory()

    fun onTrimMemory(level: Int)

    fun onConfigurationChanged(newConfig: Configuration)
}