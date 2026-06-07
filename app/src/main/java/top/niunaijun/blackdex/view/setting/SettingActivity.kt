package top.niunaijun.blackdex.view.setting

import android.os.Bundle
import top.niunaijun.blackdex.R
import top.niunaijun.blackdex.databinding.ActivitySettingBinding
import top.niunaijun.blackdex.util.inflate
import top.niunaijun.blackdex.view.base.BaseActivity
import top.niunaijun.blackdex.view.base.PermissionActivity

/**
 * 设置页面 Activity。
 *
 * 承载 [SettingFragment]，并提供权限请求桥接方法供 Fragment 调用。
 */
class SettingActivity : PermissionActivity() {

    /** ViewBinding 实例 */
    private val viewBinding: ActivitySettingBinding by inflate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.app_setting,true)
        supportFragmentManager.beginTransaction().replace(R.id.fragment,SettingFragment()).commit()
    }

    /**
     * 供 Fragment 调用的权限请求桥接方法。
     *
     * 设置权限回调后立即发起存储权限请求，
     * 请求结果通过 [callback] 回传给调用方。
     *
     * @param callback 权限请求结果回调，true 为已授权，false 为拒绝
     */
    fun setRequestCallback(callback:((Boolean)->Unit)?){
        this.requestPermissionCallback = callback
        requestStoragePermission()
    }
}