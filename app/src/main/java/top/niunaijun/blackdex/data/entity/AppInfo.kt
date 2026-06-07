package top.niunaijun.blackdex.data.entity

import android.graphics.drawable.Drawable

/**
 * 已安装应用的摘要信息，用于在列表中展示。
 *
 * @property name 应用显示名称（可通过 loadLabel 获取）
 * @property packageName 应用包名
 * @property icon 应用图标 Drawable
 *
 * @author wukaicheng
 */
data class AppInfo(
        val name:String,
        val packageName:String,
        val icon:Drawable
)
