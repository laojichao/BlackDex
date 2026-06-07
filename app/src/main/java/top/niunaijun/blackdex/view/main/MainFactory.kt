package top.niunaijun.blackdex.view.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import top.niunaijun.blackdex.data.DexDumpRepository

/**
 * [MainViewModel] 的 ViewModelProvider 工厂。
 *
 * 将 [DexDumpRepository] 注入到 ViewModel 的构造函数中。
 *
 * @param repo DEX dump 数据仓库实例
 */
@Suppress("UNCHECKED_CAST")
class MainFactory(private val repo:DexDumpRepository): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(repo) as T
    }
}