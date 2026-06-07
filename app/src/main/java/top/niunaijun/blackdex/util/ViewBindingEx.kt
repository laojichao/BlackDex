package top.niunaijun.blackdex.util

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding 扩展函数集合。
 *
 * 通过反射调用 ViewBinding 生成类的 `inflate` 静态方法，
 * 为 Activity、Fragment、Dialog 提供统一的 ViewBinding 延迟加载方式。
 *
 * @author wukaicheng
 */

/**
 * 为 Activity 提供 ViewBinding 的延迟加载委托。
 *
 * @param T ViewBinding 实现类
 * @return 懒加载的 ViewBinding 实例
 */
inline fun <reified T : ViewBinding> Activity.inflate(): Lazy<T> = lazy {
    inflateBinding(layoutInflater)
}

/**
 * 为 Fragment 提供 ViewBinding 的延迟加载委托。
 *
 * @param T ViewBinding 实现类
 * @return 懒加载的 ViewBinding 实例
 */
inline fun <reified T : ViewBinding> Fragment.inflate(): Lazy<T> = lazy {
    inflateBinding(layoutInflater)
}

/**
 * 为 Dialog 提供 ViewBinding 的延迟加载委托。
 *
 * @param T ViewBinding 实现类
 * @return 懒加载的 ViewBinding 实例
 */
inline fun <reified T : ViewBinding> Dialog.inflate(): Lazy<T> = lazy {
    inflateBinding(layoutInflater)
}

/**
 * 通过反射调用 ViewBinding 的静态 `inflate(LayoutInflater)` 方法创建实例。
 *
 * @param T ViewBinding 实现类
 * @param layoutInflater 布局加载器
 * @return 创建的 ViewBinding 实例
 */
inline fun <reified T : ViewBinding> inflateBinding(layoutInflater: LayoutInflater): T {
    val method = T::class.java.getMethod("inflate", LayoutInflater::class.java)
    return method.invoke(null, layoutInflater) as T
}

/**
 * 通过反射调用 ViewBinding 的静态 `inflate(LayoutInflater, ViewGroup, Boolean)` 方法创建实例。
 *
 * 通常用于 RecyclerView.ViewHolder 中需要 attachToParent 参数的场景。
 *
 * @param T ViewBinding 实现类
 * @param viewGroup 父容器，用于获取 Context 和 LayoutParams
 * @param attachToParent 是否附加到父容器，默认 false
 * @return 创建的 ViewBinding 实例
 */
inline fun <reified T : ViewBinding> newBindingViewHolder(viewGroup: ViewGroup, attachToParent:Boolean = false): T {
    val method = T::class.java.getMethod("inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java)
    return method.invoke(null,LayoutInflater.from(viewGroup.context),viewGroup,attachToParent) as T
}
