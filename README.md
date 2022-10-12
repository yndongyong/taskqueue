# TaskQueueManager

### 简介

基于kotlin coroutine的Channel，封装一个非阻塞式的任务队列，按先进先出的规则执行任务。

### 适用场景

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

   

### 分析

#### task定义

一类`task`是有固定执行时间的，比如展示一个`toast`，每次都是展示2s；一类`task`是没有固定的执行时间的，比如，app首页各种类型的弹框，权限弹框没有依赖信息，直接展示，如果不关闭的话，会一直展示，表示任务一直在执行中，只有当用户手动关闭弹框时，任务才结束。`task`可以执行在主线程，也可以执行在子线程处理耗时任务。执行完当前任务之后会执行下一个任务。

#### 队列处理

使用`channel`构造的生产者，消费者模型，消费者在消费`task`时，生产者被挂起。使用`RENDEZVOUS`类型的`channel`，没有缓冲区，同一时刻只能执行一个`task`，当执行taks，挂起`send`操作。执行`task`时切换到`Dispatchers.MAIN`上下文执行，同时使用`Mutex`挂起`channel`的`receive`操作所在的`coroutine`，当task执行完毕的恢复`channel`的`receive`操作所在的`coroutine`，同时`send`操作所在`coroutine`被恢复执行。



