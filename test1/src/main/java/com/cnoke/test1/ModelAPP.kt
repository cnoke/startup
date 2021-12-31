package com.cnoke.test1

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.cnoke.startup.application.IApplication

/**
 * @date on 2021/12/31
 * @author huanghui
 * @title
 * @describe
 */
open class ModelAPP private constructor(): IApplication{

    companion object {
        val instance: IApplication by lazy {
            ModelAPP()
        }
        const val TAG = "ModelAPP"
    }

    override fun attachBaseContext(context: Context) {
        Log.e(TAG,"attachBaseContext")
    }

    override fun onCreate() {
        Log.e(TAG,"onCreate")
    }

    override fun onTerminate() {
        Log.e(TAG,"onTerminate")
    }

    override fun onLowMemory() {
        Log.e(TAG,"onLowMemory")
    }

    override fun onTrimMemory(level: Int) {
        Log.e(TAG,"onTrimMemory")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.e(TAG,"onConfigurationChanged")
    }

    override fun defaultTask() {
        Log.e(TAG,"defaultTask")
    }
}