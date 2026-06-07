package top.niunaijun.blackdex.util

import top.niunaijun.blackdex.data.DexDumpRepository
import top.niunaijun.blackdex.view.main.MainFactory


/**
 * 依赖注入工具（手动 DI 容器）。
 *
 * 集中管理 Repository 和 ViewModelFactory 的创建，
 * 避免在各 Activity/Fragment 中重复构造。
 */
object InjectionUtil {

    /** DEX dump 数据仓库单例 */
    private val dexDumpRepository = DexDumpRepository()

    /**
     * 获取主界面的 ViewModel 工厂。
     *
     * @return 绑定了 [DexDumpRepository] 的 [MainFactory] 实例
     */
    fun getMainFactory() : MainFactory {
        return MainFactory(dexDumpRepository)
    }

}