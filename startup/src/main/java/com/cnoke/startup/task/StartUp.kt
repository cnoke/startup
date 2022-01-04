package com.cnoke.startup.task

import android.app.Application
import com.cnoke.startup.task.utils.ProcessUtils

/**
 * @date on 2022/1/4
 * @author huanghui
 * @title
 * @describe
 */
class StartUp constructor(val app: Application,
              val processName: String = ProcessUtils.getProcessName(app)) {

    internal var isDebug : Boolean = false

    fun isDebug(isDebug : Boolean) : StartUp{
        this.isDebug = isDebug
        return this
    }

    fun start(){
        TaskManager.start(this)
    }
}