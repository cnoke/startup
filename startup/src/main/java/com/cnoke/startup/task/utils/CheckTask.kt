package com.cnoke.startup.task.utils

import com.cnoke.startup.task.TaskInfo

/**
 * @date on 2022/1/4
 * @author huanghui
 * @title
 * @describe
 */
object CheckTask {

    /**
     * 检测名称是否有重复
     */
    fun checkDuplicateName(taskList: List<TaskInfo>) {
        val set: MutableSet<String> = mutableSetOf()
        taskList.forEach {
            check(set.contains(it.name).not()) {
                "Task名称有重复: [${it.name}]"
            }
            set.add(it.name)
        }
    }

    /**
     * 检测是否循环依赖
     */
    fun checkCircularDependency(
        chain: List<String>,
        depends: Set<String>,
        taskMap: Map<String, TaskInfo>
    ) {
        depends.forEach { depend ->
            check(chain.contains(depend).not()) {
                var result = "\n 有循环依赖 : \n"
                for(r in chain){
                    result += "   $r  依赖--> ${taskMap[r]?.depends} \n"
                }
                result
            }
            taskMap[depend]?.let { task ->
                checkCircularDependency(chain + depend, task.depends, taskMap)
            }
        }
    }

}