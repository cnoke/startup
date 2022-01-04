package com.cnoke.startup.task.comparator

import com.cnoke.startup.task.TaskInfo

/**
 * @date on 2022/1/4
 * @author huanghui
 * @title 根据锚点排序，优先运行锚点任务
 * @describe
 */
class AnchorComparator : Comparator<TaskInfo>{
    override fun compare(p0: TaskInfo, p1: TaskInfo): Int {
        return p1.anchor.compareTo(p0.anchor)
    }
}