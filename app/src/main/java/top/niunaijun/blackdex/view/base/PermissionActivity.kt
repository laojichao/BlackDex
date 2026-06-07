package top.niunaijun.blackdex.view.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.afollestad.materialdialogs.MaterialDialog
import top.niunaijun.blackbox.utils.compat.BuildCompat
import top.niunaijun.blackdex.R

/**
 * 运行时权限请求基类。
 *
 * 封装了外部存储权限的申请逻辑，兼容 Android 6.0（M）到
 * Android 11（R）的分区存储模型：
 * - Android R 及以上：先申请 MANAGE_EXTERNAL_STORAGE，再申请 WRITE_EXTERNAL_STORAGE
 * - Android M 及以上：直接申请 WRITE_EXTERNAL_STORAGE
 * - 更低版本：无需申请
 *
 * 子类设置 [requestPermissionCallback] 后在 [onStart] 中自动触发权限请求流程。
 *
 * @author wukaicheng
 */
open class PermissionActivity:BaseActivity() {

    /**
     * 权限请求结果回调。
     * 设置后在 [onStart] 时自动发起权限请求。
     * 参数 true 表示已授权，false 表示被拒绝。
     */
    protected var requestPermissionCallback: ((Boolean) -> Unit)? = null

    /**
     * 发起外部存储权限请求。
     *
     * 根据 Android 版本走不同的授权路径：
     * - Android R：需先获取所有文件访问权限（ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION），
     *   然后再申请 WRITE_EXTERNAL_STORAGE
     * - Android M ~ Q：直接申请 WRITE_EXTERNAL_STORAGE
     * - 更低版本：直接回调已授权
     */
    protected fun requestStoragePermission() {
        @RequiresApi(Build.VERSION_CODES.R)
        if (BuildCompat.isR()) {
            if (Environment.isExternalStorageManager()) {
                // Android R 已有全部文件权限，仍需申请普通读写权限
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                MaterialDialog(this).show {
                    title(R.string.grant_permission)
                    message(res = R.string.request_storage_msg)
                    negativeButton(res = R.string.request_later) {
                        if(requestPermissionCallback!=null){
                            requestPermissionCallback!!(false)
                        }
                    }
                    positiveButton(res = R.string.jump_grant) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                    }
                }
            }
        } else if (BuildCompat.isM() && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }else{
            if(requestPermissionCallback!=null){
                requestPermissionCallback!!(true)
            }
        }
    }

    /**
     * 权限请求 ActivityResult 回调。
     *
     * 授权成功时直接回调 [requestPermissionCallback]；
     * 被拒绝时弹出对话框，根据是否应展示权限说明决定是重新申请还是跳转设置页。
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                if(requestPermissionCallback!=null){
                    requestPermissionCallback!!(true)
                }
            } else {
                MaterialDialog(this).show {
                    title(res = R.string.request_fail)
                    message(res = R.string.denied_msg)
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        positiveButton(res = R.string.request_again) {
                            requestStoragePermission()
                        }

                    } else {
                        positiveButton(res = R.string.jump_grant) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                    }
                    negativeButton(res = R.string.request_later) {
                        if(requestPermissionCallback!=null){
                            requestPermissionCallback!!(false)
                        }
                    }
                }
            }
        }

    /**
     * Activity 可见时，若已设置权限回调则自动发起权限请求。
     */
    override fun onStart() {
        super.onStart()
        if(requestPermissionCallback!=null){
            requestStoragePermission()
        }
    }

}