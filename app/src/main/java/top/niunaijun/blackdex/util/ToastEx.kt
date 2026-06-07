package top.niunaijun.blackdex.util

import android.content.Context
import android.widget.Toast

/**
 * Context 扩展函数集合。
 */

/**
 * 在当前 Context 上显示一个长时间 Toast 消息。
 *
 * @param msg 要显示的文本内容
 */
fun Context.toast(msg:String){
    Toast.makeText(this,msg,Toast.LENGTH_LONG).show()
}
