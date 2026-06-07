package top.niunaijun.blackdex.view.setting

import android.os.Bundle
import android.os.Environment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.folderChooser
import top.niunaijun.blackdex.app.App
import top.niunaijun.blackdex.R
import top.niunaijun.blackdex.app.AppManager
import top.niunaijun.blackdex.app.BlackDexLoader
import java.io.File


/**
 * 设置页面 Fragment。
 *
 * 基于 [PreferenceFragmentCompat] 展示应用设置项：
 * - 保存路径：自定义 DEX dump 输出目录
 * - 保存开关：启用/禁用自定义保存路径
 * - fixCodeItem：启用代码项修复模式（耗时较长，需二次确认）
 * - Hook dump：启用 Hook 方式进行 dump
 *
 * 各设置项的变更实时持久化到 [BlackDexLoader]。
 *
 * @author wukaicheng
 */
class SettingFragment : PreferenceFragmentCompat() {

    /** 保存路径偏好设置项 */
    private lateinit var savePathPreference: Preference

    /** 自定义保存路径开关 */
    private lateinit var saveEnablePreference: SwitchPreferenceCompat

    /** fixCodeItem 修复模式开关 */
    private lateinit var fixCodeItemPreference: SwitchPreferenceCompat

    /** Hook dump 开关 */
    private lateinit var hookDumpPreference: SwitchPreferenceCompat

    /** 初始目录路径，用于文件夹选择器的默认打开位置 */
    private val initialDirectory = AppManager.mBlackBoxLoader.getSavePath()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.setting)
        savePathPreference = findPreference("save_path")!!
        savePathPreference.onPreferenceClickListener = mSavedPathClick
        savePathPreference.summary = initialDirectory

        saveEnablePreference = findPreference("save_enable")!!
        saveEnablePreference.onPreferenceChangeListener = mSaveEnableChange
        saveEnablePreference.isChecked = AppManager.mBlackBoxLoader.saveEnable()

        fixCodeItemPreference = findPreference("fix_code_item")!!
        fixCodeItemPreference.onPreferenceChangeListener = mFixCodeItemChange
        fixCodeItemPreference.isChecked = AppManager.mBlackBoxLoader.isFixCodeItem()

        hookDumpPreference = findPreference("hook_dump")!!
        hookDumpPreference.onPreferenceChangeListener = mHookDumpChange
        hookDumpPreference.isChecked = AppManager.mBlackBoxLoader.isHookDump()

    }

    /** 保存路径点击事件：弹出文件夹选择器选择自定义 dump 目录 */
    private val mSavedPathClick = Preference.OnPreferenceClickListener {
        val initialFile = with(initialDirectory) {
            if (initialDirectory.isEmpty()) {
                Environment.getExternalStorageDirectory()
            } else {
                File(this)
            }
        }

        MaterialDialog(requireContext()).show {
            folderChooser(
                requireContext(),
                initialDirectory = initialFile,
                allowFolderCreation = true
            ) { _, file ->
                AppManager.mBlackBoxLoader.setSavePath(file.absolutePath)
                savePathPreference.summary = file.absolutePath
            }
            negativeButton(res = R.string.cancel)
        }
        return@OnPreferenceClickListener true
    }

    /**
     * 保存路径开关变更事件。
     *
     * 关闭时触发权限请求（需要存储权限才能自定义路径），
     * 开启时直接保存设置。
     */
    private val mSaveEnableChange = Preference.OnPreferenceChangeListener { _, newValue ->
        if (newValue == false) {
            (requireActivity() as SettingActivity).setRequestCallback(requestResult)
        } else {
            AppManager.mBlackBoxLoader.saveEnable(true)
            saveEnablePreference.isChecked = true
        }
        return@OnPreferenceChangeListener true
    }

    /** Hook dump 开关变更事件，直接保存到 BlackDexLoader */
    private val mHookDumpChange = Preference.OnPreferenceChangeListener { _, newValue ->
        AppManager.mBlackBoxLoader.setHookDump(newValue as Boolean)
        return@OnPreferenceChangeListener true
    }

    /**
     * fixCodeItem 开关变更事件。
     *
     * 启用时弹出警告对话框确认（该模式耗时较长），
     * 禁用时直接保存。
     */
    private val mFixCodeItemChange = Preference.OnPreferenceChangeListener { _, newValue ->
        if (newValue as Boolean) {

            MaterialDialog(requireContext()).show {
                title(R.string.warn)
                message(R.string.fix_code_item_message)
                positiveButton(R.string.confirm) {
                    AppManager.mBlackBoxLoader.setFixCodeItem(true)
                }
                negativeButton(R.string.cancel) {
                    fixCodeItemPreference.isChecked = false
                    AppManager.mBlackBoxLoader.setFixCodeItem(false)
                }
            }

        } else {
            AppManager.mBlackBoxLoader.setFixCodeItem(newValue)
        }
        return@OnPreferenceChangeListener true
    }


    /**
     * 权限请求结果回调。
     *
     * 根据授权结果反向设置 saveEnable 状态，
     * 并在路径为空时自动设置默认 dump 目录。
     */
    private val requestResult = { hasPermission: Boolean ->
        AppManager.mBlackBoxLoader.saveEnable(!hasPermission)
        saveEnablePreference.isChecked = !hasPermission

        if (AppManager.mBlackBoxLoader.getSavePath().isEmpty()) {
            val path = BlackDexLoader.getDexDumpDir(App.getContext())
            AppManager.mBlackBoxLoader.setSavePath(path)
            savePathPreference.summary = path
        }
    }
}
