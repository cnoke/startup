package com.cnoke.startup

import android.content.Context
import android.content.res.Configuration
import com.cnoke.startup.application.IApplication

/**
 * @author huanghui
 * @date on 2021/12/27
 * @title
 * @describe
 */
internal class FinalAppRegister{

    private val applist : MutableList<IApplication> = mutableListOf()

    init {
        init()
    }

    private fun init() {
    }

    fun register(app : IApplication) {
        applist.add(app)
    }

    fun attachBaseContext(context: Context) {
        applist.forEach {
            it.attachBaseContext(context)
        }
    }

    fun onCreate() {
        applist.forEach {
            it.onCreate()
        }
    }

    fun onTerminate() {
        applist.forEach {
            it.onTerminate()
        }
    }

    fun onLowMemory() {
        applist.forEach {
            it.onLowMemory()
        }
    }

    fun onTrimMemory(level: Int) {
        applist.forEach {
            it.onTrimMemory(level)
        }
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        applist.forEach {
            it.onConfigurationChanged(newConfig)
        }
    }
}