package top.niunaijun.blackdex.view.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

/**
 * ViewModel 基类。
 *
 * 提供基于协程的 IO 线程任务调度能力，统一处理异常捕获，
 * 并在 ViewModel 销毁时自动取消所有协程。
 *
 * @author wukaicheng
 */
open class BaseViewModel : ViewModel() {

    /**
     * 在 IO 线程上执行挂起任务。
     *
     * 通过 [viewModelScope] 启动协程，自动切换到 [Dispatchers.IO]，
     * 内部捕获所有 [Throwable] 防止应用崩溃。
     *
     * @param block 要在 IO 线程执行的挂起函数
     */
    fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    block()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

            }
        }
    }

    /**
     * ViewModel 销毁时取消所有未完成的协程任务。
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

}