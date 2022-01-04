package com.cnoke.test1

import android.app.Application
import com.cnoke.startup.task.InitTask
import kotlinx.coroutines.delay

/**
 * @date on 2022/1/4
 * @author huanghui
 * @title
 * @describe
 */
class Task1 : InitTask{

    override fun name() = "Task1"

    override suspend fun execute(application: Application) {
        delay(1000)
    }
}

class Task11 : InitTask{

    override fun name() = "Task11"

    override suspend fun execute(application: Application) {
        delay(1000)
    }
}

class Task12 : InitTask{

    override fun name() = "Task12"

    override fun anchor() = true

    override suspend fun execute(application: Application) {
        delay(1000)
    }
}

class Task13 : InitTask{

    override fun name() = "Task13"

    override fun background() = true

    override fun anchor() = true

    override suspend fun execute(application: Application) {
        delay(3000)
    }
}