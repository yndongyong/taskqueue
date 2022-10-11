package com.yndongyong.queuetask

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.postDelayed
import com.blankj.utilcode.util.ToastUtils
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.PopTip
import com.yndongyong.queuetask.databinding.ActivityMainBinding
import com.yndongyong.taskqueue.Task
import com.yndongyong.taskqueue.TaskQueueManager
import java.util.concurrent.atomic.AtomicInteger


class MainActivity : AppCompatActivity() {

    private var mAtomicInteger = AtomicInteger(0)
    private var mSecondAtomicInteger = AtomicInteger(0)

    lateinit var mViewBinding: ActivityMainBinding

    private var mQueueManager = TaskQueueManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //初始化
        DialogX.init(this)
        mViewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)
        initView()
    }


    override fun onDestroy() {
        super.onDestroy()
        //        TaskQueueManager.instance.clear()
        mQueueManager.clear()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {

        mViewBinding.btnShot.text = "Toast infinite shot: ${mAtomicInteger.get()}"
        mViewBinding.btnShot.setOnClickListener {
            val curCount = mAtomicInteger.incrementAndGet()
            mViewBinding.btnShot.text = "Toast infinite shot: $curCount"

            //全局单例
            //            TaskQueueManager.instance.sendTask(Task(3000){
            //                if (curCount == 3) {
            //                    throw RuntimeException("dong,不影响下一个task的执行")
            //                }
            //                ToastUtils.showShort("btn click: $curCount ,counts")
            //            })
            //局部单例
            mQueueManager.sendTask(Task(3000, true) {
                if (curCount == 3) {
                    throw RuntimeException("不影响下一个task的执行")
                }
                //                内部处理了线程切换
                ToastUtils.showShort("btn click: $curCount ,counts")
                //                在子线程上执行会报错
                //                Toast.makeText(this@MainActivity, "btn click: $curCount ,counts", Toast.LENGTH_SHORT).show()
                //                可以执行io 配合callInManinThread= false执行
                //                Thread.sleep(3000)
                //                println("io op success")
            })
        }
        mViewBinding.btnShotInfinite.text = "dialog infinite shot: ${mSecondAtomicInteger.get()}"
        mViewBinding.btnShotInfinite.setOnClickListener {
            val curCount = mSecondAtomicInteger.incrementAndGet()
            mViewBinding.btnShotInfinite.text = "dialog infinite shot: $curCount"

            /*TaskQueueManager.instance.sendTask(Task {
                PopTip.build().autoDismiss(2000).setMessage("弹框提示: $curCount").setDialogLifecycleCallback(object : DialogLifecycleCallback<PopTip?>() {
                    override fun onDismiss(dialog: PopTip?) {
                        super.onDismiss(dialog)
                        //延迟1s展示下一个任务
                        this@MainActivity.mViewBinding.btnShot.postDelayed(1000) {
                            doNextTask()
                        }
                    }
                }).showShort()

            })*/
            mQueueManager.sendTask(Task {
                //                PopTip.build()/*.autoDismiss(2000)*/.setMessage("弹框提示: $curCount")
                //                    .setButton("关闭")
                //                    .noAutoDismiss()
                //                    .setDialogLifecycleCallback(object : DialogLifecycleCallback<PopTip?>() {
                //                        override fun onDismiss(dialog: PopTip?) {
                //                            super.onDismiss(dialog)
                //                            this@MainActivity.mViewBinding.btnShot.postDelayed(1000) {
                //                                doNextTask()
                //                            }
                //                        }
                //                    }).show()
                PopTip.show("弹框提示: $curCount", "关闭").noAutoDismiss()
                    .setButton { popTip, v -> //点击“撤回”按钮回调
                        //延迟1s展示下一个任务
                        this@MainActivity.mViewBinding.btnShot.postDelayed(1000) {
                            doNextTask()
                        }
                        false
                    }
            })
        }

    }


}