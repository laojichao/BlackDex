package top.niunaijun.blackdex.view.main

import androidx.lifecycle.MutableLiveData
import top.niunaijun.blackdex.data.DexDumpRepository
import top.niunaijun.blackdex.data.entity.AppInfo
import top.niunaijun.blackdex.data.entity.DumpInfo
import top.niunaijun.blackdex.view.base.BaseViewModel

/**
 * 主界面 ViewModel。
 *
 * 通过 [DexDumpRepository] 执行应用列表加载和 DEX dump 操作，
 * 结果通过 LiveData 通知 UI 层。
 *
 * @param repo DEX dump 数据仓库
 */
class MainViewModel(private val repo: DexDumpRepository) : BaseViewModel() {

    /** 已安装应用列表数据 */
    val mAppListLiveData = MutableLiveData<List<AppInfo>>()

    /** DEX dump 操作状态和结果 */
    val mDexDumpLiveData = MutableLiveData<DumpInfo>()

    /**
     * 异步加载已安装应用列表。
     * 结果通过 [mAppListLiveData] 发送。
     */
    fun getAppList() {
        launchOnUI {
            repo.getAppList(mAppListLiveData)
        }
    }

    /**
     * 发起 DEX dump 操作。
     *
     * @param source dump 来源（包名 / 文件路径 / URL）
     */
    fun startDexDump(source: String) {
        launchOnUI {
            repo.dumpDex(source, mDexDumpLiveData)
        }
    }

    /**
     * 通知 dump 成功，取消等待中的倒计时。
     */
    fun dexDumpSuccess() {
        launchOnUI {
            repo.dumpSuccess()
        }
    }

}