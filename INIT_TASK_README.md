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

- 在工程根目录的build.gradle中添加依赖：

```groovy
repositories {
    mavenCentral()
}
dependencies {
    classpath 'io.github.cnoke.startup:register:1.1.2'
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
    implementation "io.github.cnoke.startup:api:1.1.2"
    //或者base模块build.gradle 用api引入，这样所有模块都能使用
    //api "io.github.cnoke.startup:api:1.1.2"
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

