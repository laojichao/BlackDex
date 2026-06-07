package top.niunaijun.blackdex.util

import java.io.File

/**
 * 文件工具类。
 *
 * 提供文件过滤等常用文件操作辅助方法。
 */
object FileUtil {

    /**
     * 判断文件是否为 APK 文件或目录。
     *
     * 用于文件选择器的过滤回调，仅允许选择 .apk 文件和目录。
     *
     * @param file 待判断的文件
     * @return true 表示是 APK 文件或目录
     */
    fun filterApk(file: File):Boolean{
        return (file.extension == "apk") or file.isDirectory
    }
}