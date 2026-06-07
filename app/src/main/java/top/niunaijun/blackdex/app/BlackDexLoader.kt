package top.niunaijun.blackdex.app

import android.content.Context
import top.niunaijun.blackbox.BlackDexCore
import top.niunaijun.blackbox.app.configuration.ClientConfiguration
import top.niunaijun.blackbox.utils.FileUtils
import top.niunaijun.blackbox.utils.compat.BuildCompat
import top.niunaijun.blackdex.biz.cache.AppSharedPreferenceDelegate
import java.io.File

/**
 * BlackDex 核心加载器。
 *
 * 负责封装 [BlackDexCore] 的初始化配置，包括：
 * - DEX dump 输出目录的选择（默认路径 / 自定义路径）
 * - fixCodeItem 与 Hook dump 开关
 * - SharedPreferences 持久化用户设置
 *
 * @author wukaicheng
 */
class BlackDexLoader {

    /** 用户自定义的 DEX dump 保存路径 */
    private var mSavePath by AppSharedPreferenceDelegate(App.getContext(), "")

    /** 是否启用自定义保存路径；为 false 时使用默认 dump 目录 */
    private var mSaveEnable by AppSharedPreferenceDelegate(App.getContext(), true)

    /** 是否启用 fixCodeItem 修复模式（需要更长运行时间） */
    private var mFixCodeItem by AppSharedPreferenceDelegate(App.getContext(),false)

    /** 是否启用 Hook 方式进行 dump */
    private var mHookDump by AppSharedPreferenceDelegate(App.getContext(),true)

    /** 根据 [mSaveEnable] 决定的实际 dump 目录 */
    private var mDir = if (mSaveEnable) {
        getDexDumpDir(App.getContext())
    } else {
        mSavePath
    }

    /** 添加生命周期回调（预留扩展） */
    fun addLifecycleCallback() {

    }

    /**
     * 配置并初始化 BlackBox 框架的 attachBaseContext 阶段。
     *
     * 通过匿名 [ClientConfiguration] 将当前设置（包名、dump 目录、
     * fixCodeItem、hookDump）注入到框架中。
     *
     * @param context 应用基础 Context
     */
    fun attachBaseContext(context: Context) {
        BlackDexCore.get().doAttachBaseContext(context, object : ClientConfiguration() {
            override fun getHostPackageName(): String {
                return context.packageName
            }

            override fun getDexDumpDir(): String {
                return mDir
            }

            override fun isFixCodeItem(): Boolean {
                return mFixCodeItem
            }

            override fun isEnableHookDump(): Boolean {
                return mHookDump
            }
        })
    }

    /**
     * 完成 BlackBox 框架的 onCreate 阶段初始化。
     *
     * @param context 应用 Context
     */
    fun doOnCreate(context: Context) {
        BlackDexCore.get().doCreate()
    }

    /**
     * 查询是否启用自定义保存路径。
     *
     * @return true 表示使用自定义路径，false 表示使用默认 dump 目录
     */
    fun saveEnable(): Boolean {
        return mSaveEnable
    }

    /**
     * 设置是否启用自定义保存路径。
     *
     * @param state true 启用自定义路径，false 使用默认目录
     */
    fun saveEnable(state: Boolean) {
        this.mSaveEnable = state
    }

    /**
     * 获取用户设置的自定义保存路径。
     *
     * @return 自定义保存路径字符串
     */
    fun getSavePath(): String {
        return mSavePath
    }

    /**
     * 设置自定义保存路径。
     *
     * @param path 完整的文件系统路径
     */
    fun setSavePath(path: String) {
        this.mSavePath = path
    }

    /**
     * 设置是否启用 fixCodeItem 修复模式。
     *
     * @param enable true 启用，false 禁用
     */
    fun setFixCodeItem(enable:Boolean){
        this.mFixCodeItem = enable
    }

    /**
     * 查询是否启用 fixCodeItem 修复模式。
     *
     * @return true 表示已启用
     */
    fun isFixCodeItem():Boolean{
        return this.mFixCodeItem
    }

    /**
     * 设置是否启用 Hook 方式 dump。
     *
     * @param enable true 启用 Hook dump，false 禁用
     */
    fun setHookDump(enable: Boolean){
        this.mHookDump = enable
    }

    /**
     * 查询是否启用 Hook 方式 dump。
     *
     * @return true 表示已启用
     */
    fun isHookDump(): Boolean {

        return this.mHookDump
    }


    companion object {

        val TAG: String = BlackDexLoader::class.java.simpleName

        /**
         * 根据 Android 版本获取默认的 DEX dump 输出目录。
         *
         * Android 11（R）及以上使用 `Download/dexDump` 目录以兼容分区存储，
         * 低版本使用 `externalCacheDir/../dump` 目录。
         *
         * @param context 应用 Context
         * @return dump 目录的绝对路径
         */
        fun getDexDumpDir(context: Context): String {
            return if (BuildCompat.isR()) {
                val dump = File(
                    context.externalCacheDir?.parentFile?.parentFile?.parentFile?.parentFile,
                    "Download/dexDump"
                )
                FileUtils.mkdirs(dump)
                dump.absolutePath
            } else {
                val dump = File(context.externalCacheDir?.parentFile, "dump")
                FileUtils.mkdirs(dump)
                dump.absolutePath
            }
        }
    }
}