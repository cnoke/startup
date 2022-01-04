package com.cnoke.startup.task

import android.app.Application
import com.cnoke.startup.task.Process.PROCESS_ALL

/**
 * @date on 2022/1/4
 * @author huanghui
 * @title
 * @describe
 */
open interface InitTask {
    /**
     * 任务名称，需唯一
     */
    fun name() : String
    /**
     * 是否在后台线程执行
     */
    fun background() : Boolean = true
    /**
     * 锚点 锚点任务结束才能启动activity
     */
    fun anchor() : Boolean = false
    /**
     * 任务进程
     */
    fun process() : Array<String> = arrayOf(PROCESS_ALL)
    /**
     * 依赖的任务
     */
    fun depends() : Array<String> = arrayOf()

    suspend fun execute(application: Application)
}