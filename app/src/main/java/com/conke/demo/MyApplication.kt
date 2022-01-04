package com.conke.demo

import android.util.Log
import com.cnoke.startup.application.StartUpApplication
import com.cnoke.startup.task.StartUp

/**
 * @date on 2021/12/31
 * @author huanghui
 * @title
 * @describe
 */
class MyApplication : StartUpApplication() {
    override fun onCreate() {
        super.onCreate()
        StartUp(this).isDebug(true).start()
        Log.e("FinalTaskRegister","onCreate Finish")
    }
}