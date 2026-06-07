package top.niunaijun.blackdex.view.base

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * 所有 Activity 的基类。
 *
 * 提供通用的 Toolbar 初始化能力，包括标题设置和返回按钮处理。
 *
 * @author wukaicheng
 */
open class BaseActivity : AppCompatActivity() {

    /**
     * 初始化并配置 Toolbar。
     *
     * @param toolbar 要初始化的 Toolbar 控件
     * @param title 标题字符串资源 ID
     * @param showBack 是否显示返回按钮，默认不显示
     * @param onBack 返回按钮点击回调；为 null 时默认调用 [finish]
     */
    protected fun initToolbar(
        toolbar: Toolbar,
        title: Int,
        showBack: Boolean = false,
        onBack: (() -> Unit)? = null
    ) {
        setSupportActionBar(toolbar)
        toolbar.setTitle(title)
        if (showBack) {
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                toolbar.setNavigationOnClickListener {
                    if (onBack != null) {
                        onBack()
                    } else {
                        finish()
                    }
                }
            }
        }
    }


}