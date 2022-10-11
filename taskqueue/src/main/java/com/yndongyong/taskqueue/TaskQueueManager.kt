package com.yndongyong.taskqueue

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex

/**
 * Created by yndongyong on 2022/10/10.
 * @Description: 任务队列，用于task one by one 执行
 */
class TaskQueueManager {

    private var mChannel: Channel<AbsTask>? = null
    private var mCoroutineScope: CoroutineScope? = null
    private var mLock = Mutex()

    init {
        initLoop()
    }

    private fun initLoop() {
        mChannel = Channel()
        mCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        mCoroutineScope?.launch {
            mChannel?.consumeEach {
                tryHandleTask(it)
            }
        }
    }

    private suspend fun tryHandleTask(task: AbsTask) {
        //防止有task抛出异常，用CoroutineExceptionHandler捕获异常之后父coroutine关闭了，之后的send的Task不执行了
        try {
            task.setMutexLock(mLock)
            mLock.lock()
            if (task.callInMainThread) {
                withContext(Dispatchers.Main) {
                    task.doTask()
                }
            } else {
                task.doTask()
            }
            //固定时间的任务，由管理类解除阻塞，调用下个task，不固定时间的耗时任务，需要在任务介绍时手动调用doNextTask()
            if (task.durationTimeMillis != 0L) {
                delay(task.durationTimeMillis)
                task.doNextTask()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            task.doNextTask()
        }
    }

    /**
     * 开始任务
     * @param task ITask
     */
    fun sendTask(task: AbsTask) {
        if (mCoroutineScope == null && mChannel == null) {
            initLoop()
        }
        mCoroutineScope?.launch {
            mChannel?.send(task)
        }
    }

    /**
     * 关闭并释放资源
     */
    fun clear() {
        mChannel?.close()
        mChannel = null
        mCoroutineScope?.cancel()
        mCoroutineScope = null
    }

    /**
     * 需要全局单例时使用,局部单例时，直接new
     */
    companion object {
        val instance: TaskQueueManager by lazy {
            TaskQueueManager()
        }
    }

}