package com.cnoke.startup.task.utils

import android.os.Looper

/**
 * @date on 2022/1/4
 * @author huanghui
 * @title
 * @describe
 */
object ThreadUtils {

    fun isInMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
}