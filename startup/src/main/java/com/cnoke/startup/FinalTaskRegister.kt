package com.cnoke.startup

import com.cnoke.startup.task.InitTask

/**
 * @date on 2022/1/4
 * @author huanghui
 * @title
 * @describe
 */
internal class FinalTaskRegister {

    val taskList : MutableList<InitTask> = mutableListOf()

    init {
        init()
    }

    private fun init() {
    }

    fun register(initTask : InitTask) {
        taskList.add(initTask)
    }
}