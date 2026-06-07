package top.niunaijun.blackdex.view.widget

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import top.niunaijun.blackdex.R
import top.niunaijun.blackdex.databinding.DialogProgressBinding
import top.niunaijun.blackdex.util.inflate

/**
 * DEX dump 进度弹窗。
 *
 * 以 DialogFragment 形式展示不可取消的进度条对话框，
 * 实时显示当前处理的类数量进度（如 "已处理 50/200 个类"）。
 *
 * @author wukaicheng
 */
class ProgressDialog : DialogFragment() {

    private val TAG = "ProgressDialog"

    /** ViewBinding 实例 */
    private val viewBinding: DialogProgressBinding by inflate()

    /**
     * 创建不可取消的 AlertDialog，包含进度条和标题。
     *
     * @param savedInstanceState 保存的实例状态
     * @return 配置好的 Dialog 实例
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext())
            .setView(viewBinding.root)
            .setTitle(getString(R.string.classes_progress,1,1))
            .setCancelable(false)
            .show()

        dialog.setCanceledOnTouchOutside(false)
        // 拦截返回键，防止用户意外关闭进度弹窗
        dialog.setOnKeyListener { _, keyCode, _ ->
            return@setOnKeyListener keyCode == KeyEvent.KEYCODE_BACK
        }

        return dialog
    }

    /**
     * 更新进度条的当前值和最大值。
     *
     * 必须在 UI 线程调用。当 [progress] 为 0 时会重置最大值。
     *
     * @param progress 当前已完成的数量
     * @param maxProgress 总数量
     */
    fun setProgress(progress: Int, maxProgress: Int) {
        requireActivity().runOnUiThread {
            if (progress == 0) {
                viewBinding.progress.max = maxProgress
            }
            viewBinding.progress.progress = progress
            dialog?.setTitle(getString(R.string.classes_progress,progress,maxProgress))

        }
    }

}