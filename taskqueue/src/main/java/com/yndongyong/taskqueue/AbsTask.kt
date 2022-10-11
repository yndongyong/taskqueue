package com.yndongyong.taskqueue

import kotlinx.coroutines.sync.Mutex

/**
 * Created by yndongyong on 2022/10/10.
 * @Description: 任务队列中的任务
 */
abstract class AbsTask {

    /**
     * 任务执行的时间，为0表示执行完成的时间不固定
     */
    abstract val durationTimeMillis: Long

    abstract val callInMainThread: Boolean

    /**
     * 用于执行task时，挂起当前coroutine
     */
    private var mutex: Mutex? = null

    fun setMutexLock(mutex: Mutex){
        this.mutex = mutex
    }

    /**
     * 执行任务
     */
    abstract fun doTask()

    /**
     * 恢复当前coroutine，执行下一个task
     */
    fun doNextTask() {
        if (mutex?.isLocked == true) {
            mutex?.unlock()
        }
        mutex = null
    }


}