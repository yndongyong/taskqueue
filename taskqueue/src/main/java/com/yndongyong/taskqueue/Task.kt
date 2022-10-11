package com.yndongyong.taskqueue

/**
 * Created by yndongyong on 2022/10/11.
 * @Description: 任务队列中的任务，
 * @property durationTimeMillis Long 任务执行的时间，0：表示任务执行时间不固定
 * @property block [@kotlin.ExtensionFunctionType] Function1<Task, Unit> 执行块
 * @constructor
 */
class Task(
    override val durationTimeMillis: Long = 0,
    override val callInMainThread: Boolean = true,
    private val block: Task.() -> Unit
) : AbsTask() {

    /**
     * 执行任务
     */
    override fun doTask() {
        block.invoke(this)
    }

}