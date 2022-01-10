# Android ASM实现Application 组件化

## 1.简介

Android很多项目用到组件化。但是组件化项目，每个组件想要使用主工程的application都比较麻烦，而且无法很好的解耦。

很多写法是在各个组将项目中实现一个封装了application生命周期的接口。然后在app的application中实例化每个模块的对象。这就导致每次增加模块，都得去app中添加注册代码，耦合性太高。

## 2.解决方案对比

然后我看到有很多文章用反射，或者注解apt的方式解决解耦问题。

### 反射的方式

影响启动性能，在做app启动优化的时候肯定不建议用此方案

### apt注解的方式

每个model都要写上注解，比较麻烦

### ASM方式

无需反射，无需注解。编译器自动将model中的实例注册到app中。方便快捷

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
    //或者base模块build.gradle 用api引入，这样所以模块都能使用
    //api "io.github.cnoke.startup:api:?"
}
```

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

ASM 基于[AutoRegister](https://github.com/luckybilly/AutoRegister.git)修改。在此感谢AutoRegister作者的分享

## 5.Application 启动组件（异步初始化第三方SDK）
[README](https://github.com/cnoke/startup/blob/main/INIT_TASK_README.md)
