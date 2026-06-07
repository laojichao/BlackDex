package top.niunaijun.blackbox.utils.compat;

import android.content.Context;
import android.content.ContextWrapper;

import reflection.android.app.ContextImpl;
import top.niunaijun.blackbox.BlackBoxCore;

/**
 * Context兼容性修复工具类。
 * <p>
 * 通过反射修复虚拟化进程中ContextImpl内部的包名和PackageManager缓存，
 * 使应用获取的包名信息与宿主应用一致，从而绕过系统的包名校验。
 * <p>
 * 主要修复内容：
 * <ul>
 *   <li>清除mPackageManager缓存，使其重新初始化</li>
 *   <li>设置mBasePackageName为宿主包名</li>
 *   <li>设置mOpPackageName为宿主包名</li>
 * </ul>
 *
 * @author Milk
 */
public class ContextCompat {
    /** 日志标签 */
    public static final String TAG = "ContextFixer";

    /**
     * 修复Context的包名信息。
     * <p>
     * 递归解包ContextWrapper获取底层ContextImpl，
     * 最大解包深度为10层，避免无限循环。
     *
     * @param context 待修复的Context
     */
    public static void fix(Context context) {
        try {
            int deep = 0;
            while (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
                deep++;
                if (deep >= 10) {
                    return;
                }
            }
            ContextImpl.mPackageManager.set(context, null);
            try {
                context.getPackageManager();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            ContextImpl.mBasePackageName.set(context, BlackBoxCore.getHostPkg());
            ContextImpl.mOpPackageName.set(context, BlackBoxCore.getHostPkg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
