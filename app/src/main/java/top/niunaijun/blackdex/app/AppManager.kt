package top.niunaijun.blackdex.app

import android.content.Context

/**
 * 全局应用管理器（单例）。
 *
 * 统一管理 [BlackDexLoader] 实例的生命周期调度（attachBaseContext / onCreate），
 * 并预留第三方服务初始化入口。
 */
object AppManager {

    /** BlackDex 核心加载器，延迟初始化 */
    @JvmStatic
    val mBlackBoxLoader by lazy {
        BlackDexLoader()
    }

    /**
     * 在 Application.attachBaseContext 阶段调用，
     * 完成 BlackBox 框架的早期初始化并注册生命周期回调。
     *
     * @param context 应用基础 Context
     */
    fun doAttachBaseContext(context: Context) {
        try {
            mBlackBoxLoader.attachBaseContext(context)
            mBlackBoxLoader.addLifecycleCallback()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 在 Application.onCreate 阶段调用，
     * 完成 BlackBox 框架的创建初始化及第三方服务的启动。
     *
     * @param context 应用 Context
     */
    fun doOnCreate(context: Context) {
        mBlackBoxLoader.doOnCreate(context)
        initThirdService(context)
    }

    /** 初始化第三方服务（预留扩展点） */
    private fun initThirdService(context: Context) {}
}