package top.niunaijun.blackdex.data

import android.content.pm.ApplicationInfo
import android.net.Uri
import android.webkit.URLUtil
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.BlackBoxCore.getPackageManager
import top.niunaijun.blackbox.BlackDexCore
import top.niunaijun.blackbox.entity.pm.InstallResult
import top.niunaijun.blackbox.utils.AbiUtils
import top.niunaijun.blackdex.R
import top.niunaijun.blackdex.app.App
import top.niunaijun.blackdex.app.AppManager
import top.niunaijun.blackdex.data.entity.AppInfo
import top.niunaijun.blackdex.data.entity.DumpInfo
import java.io.File

/**
 * DEX dump 数据仓库。
 *
 * 负责应用列表的获取（过滤系统应用和不支持 ABI 的应用），
 * 以及 DEX dump 任务的调度与结果监控。
 *
 * @author wukaicheng
 */
class DexDumpRepository {

    /** 当前 dump 任务的自增 ID，用于取消过期的倒计时协程 */
    private var dumpTaskId = 0

    /**
     * 异步获取设备上已安装的第三方应用列表。
     *
     * 过滤条件：
     * - 排除系统应用（FLAG_SYSTEM）
     * - 排除不支持当前设备 ABI 的应用
     *
     * @param mAppListLiveData 用于通知 UI 层的应用列表 LiveData
     */
    fun getAppList(mAppListLiveData: MutableLiveData<List<AppInfo>>) {

        val installedApplications: List<ApplicationInfo> =
                getPackageManager().getInstalledApplications(0)
        val installedList = mutableListOf<AppInfo>()

        for (installedApplication in installedApplications) {
            val file = File(installedApplication.sourceDir)

            // 跳过系统应用
            if ((installedApplication.flags and ApplicationInfo.FLAG_SYSTEM) != 0) continue

            // 跳过 ABI 不兼容的应用
            if (!AbiUtils.isSupport(file)) continue


            val info = AppInfo(
                    installedApplication.loadLabel(getPackageManager()).toString(),
                    installedApplication.packageName,
                    installedApplication.loadIcon(getPackageManager())
            )
            installedList.add(info)
        }

        mAppListLiveData.postValue(installedList)
    }

    /**
     * 执行 DEX dump 操作。
     *
     * 根据 [source] 的格式自动识别输入类型：
     * - URL 格式 → 按 URI 解析
     * - 包含 "/" → 按文件路径处理
     * - 其他 → 按包名处理
     *
     * @param source dump 来源（URL / 文件路径 / 包名）
     * @param dexDumpLiveData 用于通知 UI 层 dump 进度和结果的 LiveData
     */
    fun dumpDex(source: String, dexDumpLiveData: MutableLiveData<DumpInfo>) {
        dexDumpLiveData.postValue(DumpInfo(DumpInfo.LOADING))
        val result = if (URLUtil.isValidUrl(source)) {
            BlackDexCore.get().dumpDex(Uri.parse(source))
        } else if (source.contains("/")) {
            BlackDexCore.get().dumpDex(File(source))
        } else {
            BlackDexCore.get().dumpDex(source)
        }

        if (result != null) {
            dumpTaskId++
            startCountdown(result, dexDumpLiveData)
        } else {
            dexDumpLiveData.postValue(DumpInfo(DumpInfo.TIMEOUT))
        }
    }

    /**
     * 通知 dump 成功，递增任务 ID 以取消等待中的倒计时。
     */
    fun dumpSuccess() {
        dumpTaskId++
    }

    /**
     * 启动 dump 结果等待协程。
     *
     * 每隔 20 秒检查 BlackDexCore 是否仍在运行：
     * - 若启用了 fixCodeItem 模式则持续等待（耗时较长）
     * - 普通模式下框架停止运行即退出等待
     *
     * 等待结束后通过 [dumpTaskId] 判断任务是否被取消，
     * 未取消时检查 DEX 文件是否存在并通知 UI。
     *
     * @param installResult 安装结果信息
     * @param dexDumpLiveData 用于通知 UI 层 dump 结果的 LiveData
     */
    private fun startCountdown(installResult: InstallResult, dexDumpLiveData: MutableLiveData<DumpInfo>) {
        GlobalScope.launch {
            val tempId = dumpTaskId
            while (BlackDexCore.get().isRunning) {
                delay(20000)
                // fixCodeItem 需要长时间运行，普通内存 dump 不需要
                if (!AppManager.mBlackBoxLoader.isFixCodeItem()) {
                    break
                }
            }
            // 任务未被取消时才发送结果
            if (tempId == dumpTaskId) {
                if (BlackDexCore.get().isExistDexFile(installResult.packageName)) {
                    dexDumpLiveData.postValue( DumpInfo(
                            DumpInfo.SUCCESS,
                            App.getContext().getString(R.string.dex_save, File(BlackBoxCore.get().dexDumpDir, installResult.packageName).absolutePath)
                    ))
                } else {
                    dexDumpLiveData.postValue(DumpInfo(DumpInfo.TIMEOUT))
                }
            }
        }
    }
}