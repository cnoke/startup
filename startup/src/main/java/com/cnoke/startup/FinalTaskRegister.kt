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

    /**
     * 用于代码插入。会以register(TestTask())格式插入到init中，
     * 最终所有实现initTask的接口的类都会被收集到taskList中
     */
    private fun init() {
    }

    fun register(initTask : InitTask) {
        taskList.add(initTask)
    }
}