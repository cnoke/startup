package com.cnoke.test2

import android.content.Context
import android.content.res.Configuration
import com.cnoke.startup.application.IApplication
import android.util.Log

/**
 * @date on 2022/1/1
 * @author huanghui
 * @title
 * @describe
 */
class Test2 private constructor(): IApplication{

    /**
     * 必须用此方法实现单例。否则工程会报错
     */
    companion object {
        val instance: IApplication by lazy {
            Test2()
        }
        const val TAG = "Test2"
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