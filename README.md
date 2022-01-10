# Android Kotlin协程版本Application启动组件

## 1.简介

开发应用都需要引入许多的第三方SDK,很多SDK需要在application启动，如果不进行很好的管理，就会导致Application启动速度非常慢。

Startup是基于Kotlin协程编写的启动框架。框架支持

1. 异步在主线程，子线程执行
2. 支持组件化
3. 支持多进程
4. 支持有向无环检测
5. 支持锚点（所有锚点任务结束，就解除Application阻塞启动MainActivity。非锚点任务MainActivity启动后会继续异步执行）用来区分哪些任务一定要在MainActivity启动前初始化完成

## 2.优缺点

缺点不支持java项目

优点启动组件该有的功能都有，使用协程性能更好

## 3.源码地址

[GitHub](https://github.com/cnoke/startup)

## 4.使用方式

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

## 组件中感知Application生命周期


#### 开始使用

1. 项目application继承com.cnoke.startup.application.StartUpApplication

```kotlin
class MyApplication : StartUpApplication() {
}
```

2. Module中写一个类实现com.cnoke.startup.application.IApplication接口(该类暂时必须用kotlin实现)

```kotlin
open class Test1 private constructor(): IApplication{

    /**
     * 必须用此方法实现单例。否则工程会报错
     */
    companion object {
        val instance: Test1 by lazy {
            Test1()
        }
        const val TAG = "test1"
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
}
```

完成以上步骤Module中的Test1就会跟随项目的application生命周期

#### Application不想继承StartUpApplication

在Application中实例化FinalAppRegister，在各个生命周期调用FinalAppRegister的对应方法。可以参考StartUpApplication的实现方式

```kotlin
open class StartUpApplication : Application() {

    private val appRegister by lazy { FinalAppRegister() }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        appRegister.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        appRegister.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
        appRegister.onTerminate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        appRegister.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        appRegister.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        appRegister.onTrimMemory(level)
    }

    fun initDefaultTask(){
        appRegister.defaultTask()
    }
}
```

![startup.gif](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e83c6437fe5c40a3bb7b5cafc521239d~tplv-k3u1fbpfcp-zoom-1.image)

可以看到Test1和Test2中的类在application启动时候都被调用了

