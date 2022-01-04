package com.cnoke.test2

import android.app.Application
import com.cnoke.startup.task.InitTask
import kotlinx.coroutines.delay

/**
 * @date on 2022/1/4
 * @author huanghui
 * @title
 * @describe
 */
class Task2  : InitTask {

    override fun name() = "Task2"

    override fun depends() = arrayOf("Task21")

    override suspend fun execute(application: Application) {
        delay(17000)
    }
}

class Task21  : InitTask {

    override fun name() = "Task21"

    override fun anchor() = true

    override fun depends() = arrayOf("Task1")

    override suspend fun execute(application: Application) {
        delay(1000)
    }
}

class Task22  : InitTask {

    override fun name() = "Task22"

    override fun background() = false

    override fun depends() = arrayOf("Task11")

    override suspend fun execute(application: Application) {
        delay(16000)
    }
}