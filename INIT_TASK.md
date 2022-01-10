# Android Kotlin 协程实现Application异步启动框架

## 1.简介

实现一个异步启动框架需要支持哪些基本功能

1. 指定运行线程
2. 支持组件化
3. 支持多进程
4. 支持依赖（名称依赖，方便模块化）
5. 支持有向无环检测
6. 支持主线程等待（需要等待的都被执行完成，才能继续application的启动）

## 2.源码地址

[GitHub](https://github.com/cnoke/startup)

## 3.定义task接口

```kotlin
open interface InitTask {
    /**
     * 任务名称，需唯一
     */
    fun name() : String
    /**
     * 是否在后台线程执行(1.指定运行线程)
     */
    fun background() : Boolean = true
    /**
     * 锚点 锚点任务结束才能启动activity(6.支持主线程等待)
     */
    fun anchor() : Boolean = false
    /**
     * 任务进程(3.支持多进程)
     */
    fun process() : Array<String> = arrayOf(PROCESS_ALL)
    /**
     * 依赖的任务（4.支持依赖（名称依赖，方便模块化））
     */
    fun depends() : Array<String> = arrayOf()

    suspend fun execute(application: Application)
}
```

接口的定义对应了上诉的所有需求

## 4.收集实现initTask接口的类

我们用ASM方式收集所有实现initTask接口的类

ASM 使用[AutoRegister](https://github.com/luckybilly/AutoRegister.git)实现。在此感谢AutoRegister作者的分享（ARouter,收集生成类时候也使用了此框架。不过做了定制化，无需额外配置）

1.首先写一个收集类

```kotlin
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
```

在编译期间编辑器会遍历所有的文件，检测到实现了initTask接口的类，都会被以register(TestTask())格式插入到init方法中。

最终所有实现initTask接口的类都会被收集到taskList中

2.修改AutoRegister代码（也可以按照atuoRegister使用说明，在build.gradle中配置）。由于我们为了方便他人使用，和ARouter直接修改代码定制化无需配置。



## 5.Task归类

#### 过滤不符合进程数据

```kotlin
fun List<InitTask>.toTaskInfo(app: Application, processName : String) : List<TaskInfo>{
    val result = mutableListOf<TaskInfo>()
    for(initTask in this){
        if(ProcessUtils.isMatchProgress(app,processName,initTask)){
            result.add(TaskInfo(initTask.name(),initTask.background(),initTask.anchor(),HashSet(listOf(*initTask.process())),HashSet(listOf(*initTask.depends())),initTask))
        }
    }
    return result
}
```



#### 整理依赖关系，过滤依赖循环

我们要遍历所有的task，然后将他们的依赖关系，以及是否需要主线程等待进行整理。

1.有依赖关系的，只有在依赖任务完成后才能开始运行。那么无依赖的就是根节点。

```kotlin
private fun startTask(){
        runBlocking {//使用runBlocking。runBlocking会一直阻塞当前线程，直到runBlocking中创建的协程运行完毕才释放
            //获取task 并过滤不符合进程的数据
            val taskList = FinalTaskRegister().taskList.toTaskInfo(startup.app,startup.processName)
            //判断是否有重名,重名抛出异常
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
                        //判断是否循环依赖，抛出异常
                        CheckTask.checkCircularDependency(listOf(task.name), task.depends, taskMap)
                        task.depends.forEach {
                            val depend = taskMap[it]
                            checkNotNull(depend) {
                                "找不到任务 [${task.name}] 的依赖任务 [$it]"
                            }
                            //将此任务放入依赖任务的children中，当依赖任务完成后，会遍历children进行启动
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
```

2.需要主线程等待的，那么他的依赖任务也需要主线程等待。因此从该节点迭代到根节点都要重置为主线程等待。

```kotlin
 /**
     * 迭代锚点任务，锚点任务依赖的任务也标志为锚点
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
```

## 6.启动协程运行Task

因此最外层我们使用runBlocking。runBlocking有个特点是会阻塞当前线程，我们这里是主线程。正好满足主线程等待的需求。

任务分为四种我们分别实现

1. 主线程任务

   ```kotlin
    GlobalScope.launch(Dispatchers.Main) {  }//用GlobalScope脱离runBlocking的CoroutineContext，runBlocking不会等待此任务
   ```

2. 子线程任务

   ```kotlin
    GlobalScope.launch(Dispatchers.Default) {  }//用GlobalScope脱离runBlocking的CoroutineContext，runBlocking不会等待此任务
   ```

3. 需要主线等待的主线程任务

   ```kotlin
   launch(Dispatchers.Main) {  }//runBlocking的CoroutineContext创建launch。runBlocking会等待此任务完成
   ```

4. 需要主线等待的子线程任务

   ```kotlin
   launch(Dispatchers.Default) {  }//runBlocking的CoroutineContext创建launch。runBlocking会等待此任务完成
   ```

基于上述方式封装一个方法用来根据Task设置启动对应协程

```kotlin
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
```

## 7.任务完成后，启动子任务

任务完成后，遍历查看是否有，任务依赖当前任务

```kotlin
private suspend fun CoroutineScope.afterExecute(name: String, children: Set<TaskInfo>) {
        val allowTasks = mutex.withLock {//同步锁，获取是否有可执行的子任务
            completedTasks.add(name)
            children.filter { completedTasks.containsAll(it.depends) }
        }
        if (ThreadUtils.isInMainThread()) {
            // 如果是主线程，先将异步任务放入队列，再执行同步任务
            allowTasks.filter { it.background }.sortedWith(AnchorComparator()).forEach {
                launchTask(it)
            }
            allowTasks.filter { it.background.not() }.sortedWith(AnchorComparator()).forEach { execute(it) }
        } else {
            allowTasks.sortedWith(AnchorComparator()).forEach {
                launchTask(it)
            }
        }
    }
```
## 8.使用方式

#### 添加依赖

module|register|api
---|---|---
version|[![Download](https://maven-badges.herokuapp.com/maven-central/io.github.cnoke.startup/register/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.cnoke.startup/register)|[![Download](https://maven-badges.herokuapp.com/maven-central/io.github.cnoke.startup/api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.cnoke.startup/api)

- 在工程根目录的build.gradle中添加依赖：

```groovy
repositories {
    mavenCentral()
}
dependencies {
    classpath 'io.github.cnoke.startup:register:?'
}
```


- 在主工程（一般为app）build.gradle中添加

```groovy
plugins {
    id 'com.android.application'
    id 'startup-register'//添加此插件
}
```


- 在使用到的工程build.gradle中添加依赖：

```groovy
dependencies {
    implementation "io.github.cnoke.startup:api:?"
    //或者base模块build.gradle 用api引入，这样所有模块都能使用
    //api "io.github.cnoke.startup:api:?"
}
```

#### 开始使用

1. 在项目application的onCreate方法调用如下方法

```kotlin
StartUp(this).isDebug(BuildConfig.DEBUG).start()
```

2. 实现com.cnoke.startup.task.InitTask接口

```kotlin
class Task1 : InitTask{

    override fun name() = "Task1"//TASK名称，需要唯一 必须实现
    
    override fun background() = true//是否在后台线程执行 默认是

    override fun anchor() = true//是否是锚点任务，默认否（锚点任务结束才能启动activity）

    override fun process() = arrayOf(PROCESS_ALL)//任务进程 默认所有进程
    
    override fun depends() = arrayOf("Task2")//依赖的任务 依赖任务的name
    
    //必须实现
    override suspend fun execute(application: Application) {
       //第三方SDK初始化任务
    }
}

class Task2 : InitTask{

    override fun name() = "Task2"//TASK名称，需要唯一
    
    override suspend fun execute(application: Application) {
       //第三方SDK初始化任务
    }
}
```

InitTask可以分散到组件化项目的每个module中。启动框架会通过asm收集所有实现InitTask接口的类，注册进启动队列。

完成以上简单步骤，就能对各种复杂的启动逻辑进行管理
