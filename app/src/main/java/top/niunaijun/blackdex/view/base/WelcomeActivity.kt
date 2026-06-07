package top.niunaijun.blackdex.view.base

import android.content.Intent
import android.os.Bundle
import top.niunaijun.blackdex.view.main.MainActivity

/**
 * 欢迎/闪屏 Activity。
 *
 * 作为启动入口，创建后立即跳转到 [MainActivity] 并销毁自身，
 * 实现无界面中转效果。
 */
class WelcomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}