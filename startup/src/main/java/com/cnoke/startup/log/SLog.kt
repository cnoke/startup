package com.cnoke.startup.log

import android.util.Log

internal object SLog {

    private const val NULL_TIPS = "Log with null object"
    private const val DEFAULT_MESSAGE = "execute"
    private const val TAG_DEFAULT = "StartUp"
    private const val SUFFIX = ".java"
    private const val KT = ".kt"

    const val V = 0x1
    const val D = 0x2
    const val I = 0x3
    const val W = 0x4
    const val E = 0x5

    private const val STACK_TRACE_INDEX_5 = 5
    private const val STACK_TRACE_INDEX_4 = 4

    //LogExt扩展函数
    const val STACK_TRACE_INDEX_7 = 7

    private var mGlobalTag: String? = null
    private var mIsGlobalTagEmpty = true
    private var IS_SHOW_LOG = true

    fun init(isShowLog: Boolean) {
        IS_SHOW_LOG = isShowLog
    }

    fun init(isShowLog: Boolean, tag: String?) {
        IS_SHOW_LOG = isShowLog
        mGlobalTag = tag
        mIsGlobalTagEmpty = mGlobalTag.isNullOrEmpty()
    }

    fun v(msg: String?) {
        printLog(V, null, STACK_TRACE_INDEX_5, msg)
    }

    fun v(tag: String?, msg: String?) {
        logType(V, tag, STACK_TRACE_INDEX_5, msg)
    }

    fun d(msg: String?) {
        printLog(D, null, STACK_TRACE_INDEX_5, msg)
    }

    fun d(tag: String?, msg: String?) {
        logType(D, tag, 6, msg)
    }

    fun i(msg: String?) {
        printLog(I, null, STACK_TRACE_INDEX_5, msg)
    }

    fun i(tag: String?, msg: String?) {
        logType(I, tag, STACK_TRACE_INDEX_5, msg)
    }

    fun w(msg: String?) {
        printLog(W, null, STACK_TRACE_INDEX_5, msg)
    }

    fun w(tag: String?, msg: String?) {
        logType(W, tag, STACK_TRACE_INDEX_5, msg)
    }

    fun e(msg: String?) {
        printLog(E, null, STACK_TRACE_INDEX_5, msg)
    }

    fun e(tag: String?, msg: String?) {
        logType(E, tag, STACK_TRACE_INDEX_5, msg)
    }

    private fun printLog(
        type: Int,
        tagStr: String?,
        stackTraceIndex: Int,
        msg: String?
    ) {
        if (!IS_SHOW_LOG) return
        val contents =
            wrapperContent(stackTraceIndex, tagStr, msg)
        val tag = contents[0]
        val msgValue = contents[1]
        val headString = contents[2]
        when (type) {
            V -> Log.v(tag, headString + msgValue)
            D -> Log.d(tag, headString + msgValue)
            I -> Log.i(tag, headString + msgValue)
            W -> Log.w(tag, headString + msgValue)
            E -> Log.e(tag, headString + msgValue)
            else -> Log.d( tag, headString + msgValue)
        }
    }

    fun logType(type: Int, tag: String?, stackTraceIndex: Int, msg: String?) {
        if (tag.isNullOrEmpty() && msg != null) {
            printLog(type, null, stackTraceIndex, msg)
        } else if (!tag.isNullOrEmpty() && msg != null) {
            printLog(type, tag, stackTraceIndex - 1, msg)
        } else if (!tag.isNullOrEmpty() && msg == null) {
            printLog(type, null, stackTraceIndex, tag)
        } else {
            printLog(type, null, stackTraceIndex, DEFAULT_MESSAGE)
        }
    }

    private fun wrapperContent(
        stackTraceIndex: Int,
        tagStr: String?,
        msg: String?
    ): Array<String?> {
        val stackTrace =
            Thread.currentThread().stackTrace
        val targetElement = stackTrace[stackTraceIndex]
        var className = targetElement.className
        val lastFileType =
            if (targetElement.fileName.endsWith(SUFFIX)) SUFFIX else KT
        val classNameInfo = className.split(".").toTypedArray()
        if (classNameInfo.isNotEmpty()) {
            className = classNameInfo[classNameInfo.size - 1] + lastFileType
        }
        if (className.contains("$")) {
            className = className.split("$").toTypedArray()[0] + lastFileType
        }
        val methodName = targetElement.methodName
        var lineNumber = targetElement.lineNumber
        if (lineNumber < 0) {
            lineNumber = 0
        }
        var tag = tagStr ?: className
        if (mIsGlobalTagEmpty && tag.isNullOrEmpty()) {
            tag = TAG_DEFAULT
        } else if (!mIsGlobalTagEmpty) {
            tag = mGlobalTag
        }
        val msgValue = msg ?: NULL_TIPS
        val headString =
            "[ ($className:$lineNumber)#$methodName ] "
        return arrayOf(tag, msgValue, headString)
    }

}