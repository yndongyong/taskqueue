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
     * 锁，用于执行耗时不固定的任务时，阻塞当前队列，执行完成时候释放当前队列
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
     * 解除阻塞，执行下一个
     */
    fun doNextTask() {
        if (mutex?.isLocked == true) {
            mutex?.unlock()
        }
        mutex = null
    }


}