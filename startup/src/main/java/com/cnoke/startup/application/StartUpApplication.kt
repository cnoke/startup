package com.cnoke.startup.application

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.cnoke.startup.FinalAppRegister

/**
 * @author huanghui
 * @date on 2021/12/27
 * @title
 * @describe
 */
open class StartUpApplication : Application() {

    private val appRegister by lazy { FinalAppRegister() }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        appRegister.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        appRegister.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
        appRegister.onTerminate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        appRegister.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        appRegister.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        appRegister.onTrimMemory(level)
    }

    fun initDefaultTask(){
        appRegister.defaultTask()
    }
}