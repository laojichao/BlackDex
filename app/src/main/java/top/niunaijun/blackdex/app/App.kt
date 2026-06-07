package top.niunaijun.blackdex.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.umeng.commonsdk.UMConfigure

/**
 * BlackDex 应用程序入口类。
 *
 * 负责全局 Context 的初始化、[AppManager] 的生命周期调度，
 * 以及友盟（UMeng）统计 SDK 的初始化。
 *
 * @author wukaicheng
 */
class App : Application() {

    companion object {

        /** 全局应用 Context，使用 [SuppressLint] 抑制静态 Context 泄漏告警 */
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var mContext: Context

        /**
         * 获取全局 [Context] 实例。
         *
         * @return 应用级 Context，可在任意位置调用
         */
        @JvmStatic
        fun getContext(): Context {
            return mContext
        }
    }

    /**
     * 在应用进程创建最早阶段保存全局 Context，
     * 并委托 [AppManager] 完成 BlackBox 框架的 attachBaseContext 初始化。
     *
     * @param base 基础 Context
     */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mContext = base!!
        AppManager.doAttachBaseContext(base)
    }

    /**
     * 应用创建回调：初始化 BlackBox 框架、第三方服务（友盟等）。
     */
    override fun onCreate() {
        super.onCreate()
        AppManager.doOnCreate(mContext)
        UMConfigure.init(this, "60b373136c421a3d97d23c29", "Github", UMConfigure.DEVICE_TYPE_PHONE, null)
    }
}