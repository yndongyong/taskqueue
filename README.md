# TaskQueueManager

### 简介

基于`kotlin coroutine`的`Channel`，实现的一个非阻塞式的任务队列，按先进先出的规则执行任务。

### 适用场景

适用于多个任务没有依赖关系，需要排队执行的情况。

比如：多个UI展示任务，需要排队一一展示的情况

1. 弹框任务编排，例如，app首页有多个弹框（隐私弹框、权限弹框、更新弹框、运营弹框），需要按一定顺序排队展示，一个接一个展示，并且上一个主动关闭之后才能下一个。
2. 直播间打赏礼物展示，打赏礼物动画需要一个一个展示的场景，动画展示的时间固定的。

### 引用

库在`jitpack`上

```groovy
maven { url 'https://jitpack.io' }
```

```groovy
dependencies {
    implementation 'com.github.yndongyong:taskqueue:v0.0.1'
}
```

### 特性

1. 基于`kotlin coroutine`实现。
2. First-In,First-out处理任务
3. 支持指定task执行在主线程或是子线程

### 使用

1. 创建任务队列管理类

   ```kotlin
   mQueueManager = TaskQueueManager()
   ```

2. 添加任务（有固定执行时间的）

   ```kotlin
   mQueueManager.sendTask(Task(3000) {
   	ToastUtils.showShort("btn click: $curCount ,counts")
   })
   ```

3. 添加执行时间不固定的任务(展示一个弹框)

   ```kotlin
   mQueueManager.sendTask(Task {
   	PopTip.show("弹框提示: $curCount", "关闭").noAutoDismiss()
       .setButton { popTip, v -> //点击“撤回”按钮回调
            //延迟1s展示下一个任务
            this@MainActivity.mViewBinding.btnShot.postDelayed(1000) {
                doNextTask()
            }
             false
        }
   })
   ```

执行时间不确定的任务,需要在任务执行完后主动调用 `doNextTask()` 方法去执行下一个task，否则消费者会一直挂起。

注意：还提供了一个全局单例`TaskQueueManager.instance`

### 分析

#### task定义

一类`task`是有固定执行时间的，比如展示一个`toast`，每次都是展示2s；一类`task`是没有固定的执行时间的，比如，app首页各种类型的弹框，权限弹框没有依赖信息，直接展示，如果不关闭的话，会一直展示，表示任务一直在执行中，只有当用户手动关闭弹框时，任务才结束。`task`可以执行在主线程，也可以执行在子线程处理耗时任务。执行完当前任务之后会执行下一个任务。

`task`支持指定要执行在主线程（默认），还是子线程，如果是执行时间固定的支持指定执行时间，默认是执行时间不固定。

```kotlin
/**
 * Created by yndongyong on 2022/10/11.
 * @Description: 任务队列中的任务
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
```

#### 队列处理

使用`channel`构造的生产者，消费者的多对一模型，消费者在消费`task`时，生产者被挂起。使用`RENDEZVOUS`类型的`channel`，没有缓冲区，同一时刻只能执行一个`task`，当执行`taks`，挂起`send`操作。执行`task`时切换到指定的`Dispatchers.MAIN`或者`Dispatchers.IO`协程上下文执行，如果task是执行时间不固定，使用`Mutex`挂起`channel`的`receive`操作所在的`coroutine`，当`task`执行完毕的恢复`channel`的`receive`操作所在的`coroutine`，同时`send`操作所在`coroutine`被恢复执行。



