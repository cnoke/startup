package com.cnoke.startup.task

import com.cnoke.startup.FinalTaskRegister
import com.cnoke.startup.log.SLog
import com.cnoke.startup.task.comparator.AnchorComparator
import com.cnoke.startup.task.utils.CheckTask
import com.cnoke.startup.task.utils.ThreadUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis

/**
 * @date on 2022/1/4
 * @author huanghui
 * @title
 * @describe
 */
internal class TaskManager private constructor(
    private val startup: StartUp
) {

    private val completedTasks: MutableSet<String> = mutableSetOf()
    private lateinit var taskMap : Map<String,TaskInfo>
    private val mutex = Mutex()

    private fun start() {
        if(startup.isDebug){
            val cost = measureTimeMillis {
                startTask()
            }
            SLog.d("锚点任务 总完成 时间 : ${cost}ms  activity启动")
        }else{
            startTask()
        }
    }

    private fun startTask(){
        runBlocking {
            //获取task 并过滤不符合进程的数据
            val taskList = FinalTaskRegister().taskList.toTaskInfo(startup.app,startup.processName)
            //判断是否有重名
            CheckTask.checkDuplicateName(taskList)
            taskMap = taskList.map { it.name to it }.toMap()
            val singleSyncTasks: MutableSet<TaskInfo> = mutableSetOf()
            val singleAsyncTasks: MutableSet<TaskInfo> = mutableSetOf()
            val anchorTasks : MutableSet<TaskInfo> = mutableSetOf()

            taskList.forEach { task ->
                if(task.anchor){
                    //锚点任务
                    anchorTasks.add(task)
                }
                when {
                    task.depends.isNotEmpty() -> {
                        //判断是否循环依赖
                        CheckTask.checkCircularDependency(listOf(task.name), task.depends, taskMap)
                        task.depends.forEach {
                            val depend = taskMap[it]
                            checkNotNull(depend) {
                                "找不到任务 [${task.name}] 的依赖任务 [$it]"
                            }
                            depend.children.add(task)
                        }
                    }
                    task.background -> {
                        singleAsyncTasks.add(task)
                    }
                    else -> {
                        singleSyncTasks.add(task)
                    }
                }
            }

            //锚点任务依赖的任务都转化为锚点任务
            for(anchorTask in anchorTasks){
                anchor(anchorTask)
            }

            // 无依赖的异步任务
            singleAsyncTasks.sortedWith(AnchorComparator()).forEach { task ->
                launchTask(task)
            }

            // 无依赖的同步任务
            singleSyncTasks.sortedWith(AnchorComparator()).forEach { task ->
                launchTask(task)
            }
        }
    }

    private suspend fun CoroutineScope.launchTask(task : TaskInfo){
        val dispatcher = if (task.background) {
            //子线程任务用子线程
            Dispatchers.Default
        } else if (!task.background && ThreadUtils.isInMainThread()) {
            //主线程任务并且当前线程为主线程用 Unconfined
            Dispatchers.Unconfined
        } else {
            //主线程任务，当前线程不为主线程，切换到主线程
            Dispatchers.Main
        }

        if(task.anchor){
            //锚点任务跟随主线程生命周期
            launch(dispatcher) { execute(task) }
        }else{
            //非锚点任务生命周期独立
            GlobalScope.launch(dispatcher) { execute(task) }
        }
    }

    /**
     * 锚点任务依赖的任务也标志为锚点
     */
    private fun anchor(anchorTask : TaskInfo){
        for(dependName in anchorTask.depends){
            val depend = taskMap[dependName]
            if(depend != null){
                depend.anchor = true
                anchor(depend)
            }
        }
    }

    private suspend fun CoroutineScope.execute(task: TaskInfo) {
        if(startup.isDebug){
            SLog.d("任务 [${task.name}] 开始 运行进程: [${startup.processName}] " +
                    "运行线程: [${Thread.currentThread().name}]"
            )
            val cost = measureTimeMillis {
                kotlin.runCatching {
                    task.task.execute(startup.app)
                }.onFailure {
                    SLog.e( "任务 [${task.name}] error $it")
                }
            }
            SLog.d("任务 [${task.name}] 锚点 ${task.anchor} 完成 运行进程: [${startup.processName}] " +
                    "运行线程: [${Thread.currentThread().name}], 使用时间 : ${cost}ms"
            )
        }else{
            kotlin.runCatching {
                task.task.execute(startup.app)
            }.onFailure {
                SLog.e( "任务 [${task.name}] error $it")
            }
        }
        afterExecute(task.name, task.children)
    }

    private suspend fun CoroutineScope.afterExecute(name: String, children: Set<TaskInfo>) {
        val allowTasks = mutex.withLock {
            completedTasks.add(name)
            children.filter { completedTasks.containsAll(it.depends) }
        }
        if (ThreadUtils.isInMainThread()) {
            // 如果是主线程，先将异步任务放入队列，再执行同步任务
            allowTasks.sortedWith(AnchorComparator()).filter { it.background }.forEach {
                launchTask(it)
            }
            allowTasks.filter { it.background.not() }.sortedWith(AnchorComparator()).forEach { execute(it) }
        } else {
            allowTasks.sortedWith(AnchorComparator()).forEach {
                launchTask(it)
            }
        }
    }

    companion object {

        /**
         * 启动任务
         */
        fun start(startUp: StartUp) {
            SLog.init(startUp.isDebug,"startUp")
            TaskManager(startUp).start()
        }
    }
}