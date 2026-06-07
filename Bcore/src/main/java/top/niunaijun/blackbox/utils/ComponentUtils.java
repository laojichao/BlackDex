package top.niunaijun.blackbox.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.ProviderInfo;

import java.util.Objects;

import top.niunaijun.blackbox.app.BActivityThread;

import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE;

/**
 * Android组件操作工具类。
 * <p>
 * 提供Intent判断、组件信息比较、TaskAffinity获取等工具方法，
 * 主要用于虚拟化框架中对Activity、Service、Provider等组件的处理和调度。
 */
public class ComponentUtils {

    /**
     * 判断Intent是否为安装APK请求。
     *
     * @param intent 待检查的Intent
     * @return 如果Intent的MIME类型为"application/vnd.android.package-archive"返回true
     */
    public static boolean isRequestInstall(Intent intent) {
        return "application/vnd.android.package-archive".equals(intent.getType());
    }

    /**
     * 判断Intent是否指向当前虚拟化应用自身。
     *
     * @param intent 待检查的Intent
     * @return 如果Intent的组件包名与当前应用包名相同返回true
     */
    public static boolean isSelf(Intent intent) {
        ComponentName component = intent.getComponent();
        if (component == null || BActivityThread.getAppPackageName() == null) return false;
        return component.getPackageName().equals(BActivityThread.getAppPackageName());
    }

    /**
     * 判断Intent数组中的所有Intent是否都指向当前应用自身。
     *
     * @param intent Intent数组
     * @return 如果所有Intent都指向自身返回true
     */
    public static boolean isSelf(Intent[] intent) {
        for (Intent intent1 : intent) {
            if (!isSelf(intent1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断Intent是否指向Google Play Services（GMS）。
     *
     * @param intent 待检查的Intent
     * @return 如果目标包名为"com.google.android.gms"返回true
     */
    public static boolean isGmsService(Intent intent) {
        String aPackage = intent.getPackage();
        return "com.google.android.gms".equals(aPackage);
    }

    /**
     * 获取Activity的TaskAffinity（任务亲和力）。
     * <p>
     * 对于LAUNCH_SINGLE_INSTANCE模式，返回特殊格式的亲和力字符串；
     * 否则优先使用Activity自身的taskAffinity，其次使用Application级别的taskAffinity。
     *
     * @param info ActivityInfo信息
     * @return TaskAffinity字符串
     */
    public static String getTaskAffinity(ActivityInfo info) {
        if (info.launchMode == LAUNCH_SINGLE_INSTANCE) {
            return "-SingleInstance-" + info.packageName + "/" + info.name;
        } else if (info.taskAffinity == null && info.applicationInfo.taskAffinity == null) {
            return info.packageName;
        } else if (info.taskAffinity != null) {
            return info.taskAffinity;
        }
        return info.applicationInfo.taskAffinity;
    }

    /**
     * 获取ProviderInfo的第一个Authority。
     * <p>
     * Provider可能声明多个Authority（以分号分隔），此方法返回第一个。
     *
     * @param info ProviderInfo信息
     * @return 第一个Authority字符串，info为null时返回null
     */
    public static String getFirstAuthority(ProviderInfo info) {
        if (info == null) {
            return null;
        }
        String[] authorities = info.authority.split(";");
        return authorities.length == 0 ? info.authority : authorities[0];
    }

    /**
     * 比较两个Intent的过滤器是否等价。
     * <p>
     * 比较维度包括：Action、Data、Type、Package/Component包名、Component、Categories。
     * 与系统Intent.filterEquals()逻辑一致，但额外考虑了Component中的包名。
     *
     * @param a 第一个Intent
     * @param b 第二个Intent
     * @return 两个Intent的过滤器等价返回true
     */
    public static boolean intentFilterEquals(Intent a, Intent b) {
        if (a != null && b != null) {
            if (!Objects.equals(a.getAction(), b.getAction())) {
                return false;
            }
            if (!Objects.equals(a.getData(), b.getData())) {
                return false;
            }
            if (!Objects.equals(a.getType(), b.getType())) {
                return false;
            }
            Object pkgA = a.getPackage();
            if (pkgA == null && a.getComponent() != null) {
                pkgA = a.getComponent().getPackageName();
            }
            String pkgB = b.getPackage();
            if (pkgB == null && b.getComponent() != null) {
                pkgB = b.getComponent().getPackageName();
            }
            if (!Objects.equals(pkgA, pkgB)) {
                return false;
            }
            if (!Objects.equals(a.getComponent(), b.getComponent())) {
                return false;
            }
            if (!Objects.equals(a.getCategories(), b.getCategories())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取组件的进程名。
     * <p>
     * 如果ComponentInfo未设置processName，则默认使用packageName作为进程名。
     *
     * @param componentInfo 组件信息
     * @return 进程名字符串
     */
    public static String getProcessName(ComponentInfo componentInfo) {
        String processName = componentInfo.processName;
        if (processName == null) {
            processName = componentInfo.packageName;
            componentInfo.processName = processName;
        }
        return processName;
    }

    /**
     * 判断两个组件是否为同一个组件（包名和类名均相同）。
     *
     * @param first  第一个组件信息
     * @param second 第二个组件信息
     * @return 是同一个组件返回true
     */
    public static boolean isSameComponent(ComponentInfo first, ComponentInfo second) {

        if (first != null && second != null) {
            String pkg1 = first.packageName + "";
            String pkg2 = second.packageName + "";
            String name1 = first.name + "";
            String name2 = second.name + "";
            return pkg1.equals(pkg2) && name1.equals(name2);
        }
        return false;
    }

    /**
     * 将ComponentInfo转换为ComponentName。
     *
     * @param componentInfo 组件信息
     * @return 包含包名和类名的ComponentName
     */
    public static ComponentName toComponentName(ComponentInfo componentInfo) {
        return new ComponentName(componentInfo.packageName, componentInfo.name);
    }
}
