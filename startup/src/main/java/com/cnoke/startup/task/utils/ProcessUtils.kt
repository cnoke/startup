package com.cnoke.startup.task.utils

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import com.cnoke.startup.task.InitTask
import com.cnoke.startup.task.Process.PROCESS_ALL
import com.cnoke.startup.task.Process.PROCESS_MAIN
import com.cnoke.startup.task.Process.PROCESS_NOT_MAIN
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * @date on 2022/1/4
 * @author huanghui
 * @title
 * @describe
 */
object ProcessUtils {

    fun isMainProcess(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val packageName = context.applicationContext.packageName
        val processName = getProcessName(context)
        return packageName == processName
    }

    fun getProcessName(context: Context): String {
        var processName = getProcessFromFile()
        if (processName == null || processName.isEmpty()) {
            // 如果装了xposed一类的框架，上面可能会拿不到，回到遍历迭代的方式
            processName = getProcessNameByAM(context)
        }
        return processName
    }

    private fun getProcessFromFile(): String? {
        var reader: BufferedReader? = null
        return try {
            val pid = Process.myPid()
            val file = "/proc/$pid/cmdline"
            reader = BufferedReader(InputStreamReader(FileInputStream(file), "iso-8859-1"))
            var c: Int
            val processName = StringBuilder()
            while (reader.read().also { c = it } > 0) {
                processName.append(c.toChar())
            }
            processName.toString()
        } catch (e: Exception) {
            null
        } finally {
            try {
                reader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getProcessNameByAM(context: Context): String {
        var processName: String? = null
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        while (true) {
            val plist = am.runningAppProcesses
            if (plist != null) {
                for (info in plist) {
                    if (info.pid == Process.myPid()) {
                        processName = info.processName
                        break
                    }
                }
            }
            if (processName != null && processName.isNotEmpty()) {
                return processName
            }
            try {
                Thread.sleep(100L) // take a rest and again
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
            }
        }
    }

    fun isMatchProgress(app: Application ,processName : String, task: InitTask): Boolean {
        val mainProgressName = app.applicationInfo.processName
        task.process().forEach {
            if (it == PROCESS_ALL) {
                return true
            } else if (it == PROCESS_MAIN) {
                if (processName == mainProgressName) {
                    return true
                }
            } else if (it == PROCESS_NOT_MAIN) {
                if (processName != mainProgressName) {
                    return true
                }
            } else if (it.startsWith(":")) {
                if (processName == mainProgressName + it) {
                    return true
                }
            } else {
                if (it == processName) {
                    return true
                }
            }
        }
        return false
    }
}