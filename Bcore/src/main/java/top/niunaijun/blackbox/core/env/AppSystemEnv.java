package top.niunaijun.blackbox.core.env;

import android.content.ComponentName;

import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackbox.BlackBoxCore;

/**
 * 虚拟环境系统包配置类。
 * <p>
 * 管理虚拟环境中的系统级包白名单、Root 工具包黑名单、Xposed 包检测列表
 * 以及预装包列表。用于控制虚拟应用对系统组件的访问权限。
 *
 * @author Milk
 */
public class AppSystemEnv {
    private static final List<String> sSystemPackages = new ArrayList<>();
    private static final List<String> sSuPackages = new ArrayList<>();
    private static final List<String> sXposedPackages = new ArrayList<>();
    private static final List<String> sPreInstallPackages = new ArrayList<>();

    static {
        sSystemPackages.add("android");
        sSystemPackages.add("com.google.android.webview");
        sSystemPackages.add("com.google.android.webview.dev");
        sSystemPackages.add("com.google.android.webview.beta");
        sSystemPackages.add("com.google.android.webview.canary");
        sSystemPackages.add("com.android.webview");
        sSystemPackages.add("com.android.camera");
        sSystemPackages.add(BlackBoxCore.getHostPkg());

        // 华为
        sSystemPackages.add("com.huawei.webview");

        // oppo
        sSystemPackages.add("com.coloros.safecenter");

        // su
        sSuPackages.add("com.noshufou.android.su");
        sSuPackages.add("com.noshufou.android.su.elite");
        sSuPackages.add("eu.chainfire.supersu");
        sSuPackages.add("com.koushikdutta.superuser");
        sSuPackages.add("com.thirdparty.superuser");
        sSuPackages.add("com.yellowes.su");

        sXposedPackages.add("de.robv.android.xposed.installer");

        sPreInstallPackages.add("com.huawei.hwid");
    }

    /**
     * 判断指定包名是否为系统级白名单包。
     *
     * @param packageName 包名
     * @return 在白名单中返回 {@code true}
     */
    public static boolean isOpenPackage(String packageName) {
        return sSystemPackages.contains(packageName);
    }

    /**
     * 判断指定 ComponentName 的包是否为系统级白名单包。
     *
     * @param componentName 组件名
     * @return 在白名单中返回 {@code true}
     */
    public static boolean isOpenPackage(ComponentName componentName) {
        return componentName != null && isOpenPackage(componentName.getPackageName());
    }

    /**
     * 判断指定包名是否在黑名单中。
     *
     * @param packageName 包名
     * @return 当前始终返回 {@code false}
     */
    public static boolean isBlackPackage(String packageName) {
        return false;
    }

    /** @return 预装应用包名列表 */
    public static List<String> getPreInstallPackages() {
        return sPreInstallPackages;
    }
}
