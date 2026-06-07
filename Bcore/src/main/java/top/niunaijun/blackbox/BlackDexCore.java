package top.niunaijun.blackbox;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Process;

import java.io.File;
import java.util.List;

import top.niunaijun.blackbox.app.configuration.ClientConfiguration;
import top.niunaijun.blackbox.core.system.dump.IBDumpMonitor;
import top.niunaijun.blackbox.entity.pm.InstallResult;
import top.niunaijun.blackbox.proxy.ProxyManifest;

/**
 * BlackDex 核心入口类，封装 {@link BlackBoxCore} 提供面向 DEX 脱壳的高层 API。
 * <p>
 * 该类负责协调 APK 安装、虚拟环境启动和 DEX Dump 完整流程：
 * <ol>
 *     <li>安装目标 APK 到虚拟环境</li>
 *     <li>启动目标应用触发 DEX 加载</li>
 *     <li>通过 Native Hook 拦截 DEX 并导出</li>
 * </ol>
 *
 * @author Milk
 * @see BlackBoxCore
 */
public class BlackDexCore {
    /** 日志标签 */
    public static final String TAG = "BlackBoxCore";

    private static final BlackDexCore sBlackDexCore = new BlackDexCore();

    /**
     * 获取 BlackDexCore 单例实例。
     *
     * @return BlackDexCore 单例
     */
    public static BlackDexCore get() {
        return sBlackDexCore;
    }

    /**
     * 初始化虚拟环境，委托给 {@link BlackBoxCore}。
     *
     * @param context            应用上下文
     * @param clientConfiguration 客户端配置
     */
    public void doAttachBaseContext(Context context, ClientConfiguration clientConfiguration) {
        BlackBoxCore.get().doAttachBaseContext(context, clientConfiguration);
    }

    /**
     * 创建虚拟环境并清理已安装的包。
     * <p>主进程启动时会卸载所有已安装的虚拟包，确保干净的脱壳环境。</p>
     */
    public void doCreate() {
        BlackBoxCore.get().doCreate();
        // uninstall all pckage
        if (BlackBoxCore.get().isMainProcess()) {
            List<PackageInfo> installedPackages =
                    BlackBoxCore.getBPackageManager().getInstalledPackages(0, BlackBoxCore.USER_ID);
            for (PackageInfo installedPackage : installedPackages) {
                BlackBoxCore.get().uninstallPackage(installedPackage.packageName);
            }
        }
    }

    /**
     * 通过包名执行 DEX 脱壳。
     * <p>安装目标 APK 到虚拟环境后启动应用，触发 DEX 加载并由 Native 层拦截导出。</p>
     *
     * @param packageName 目标应用包名
     * @return 安装结果，安装或启动失败时返回 {@code null}
     */
    public InstallResult dumpDex(String packageName) {
        InstallResult installResult = BlackBoxCore.get().installPackage(packageName);
        if (installResult.success) {
            boolean b = BlackBoxCore.get().launchApk(packageName);
            if (!b) {
                BlackBoxCore.get().uninstallPackage(installResult.packageName);
                return null;
            }
            return installResult;
        } else {
            return null;
        }
    }

    /**
     * 通过 APK 文件执行 DEX 脱壳。
     *
     * @param file 目标 APK 文件
     * @return 安装结果，安装或启动失败时返回 {@code null}
     */
    public InstallResult dumpDex(File file) {
        InstallResult installResult = BlackBoxCore.get().installPackage(file);
        if (installResult.success) {
            boolean b = BlackBoxCore.get().launchApk(installResult.packageName);
            if (!b) {
                BlackBoxCore.get().uninstallPackage(installResult.packageName);
                return null;
            }
            return installResult;
        } else {
            return null;
        }
    }

    /**
     * 通过 URI 执行 DEX 脱壳。
     *
     * @param file 目标 APK 的 URI 地址
     * @return 安装结果，安装或启动失败时返回 {@code null}
     */
    public InstallResult dumpDex(Uri file) {
        InstallResult installResult = BlackBoxCore.get().installPackage(file);
        if (installResult.success) {
            boolean b = BlackBoxCore.get().launchApk(installResult.packageName);
            if (!b) {
                BlackBoxCore.get().uninstallPackage(installResult.packageName);
                return null;
            }
            return installResult;
        } else {
            return null;
        }
    }

    /**
     * 注册 DEX Dump 结果监听器。
     *
     * @param monitor Dump 结果监听器
     */
    public void registerDumpMonitor(IBDumpMonitor monitor) {
        BlackBoxCore.getBDumpManager().registerMonitor(monitor.asBinder());
    }

    /**
     * 注销 DEX Dump 结果监听器。
     *
     * @param monitor Dump 结果监听器
     */
    public void unregisterDumpMonitor(IBDumpMonitor monitor) {
        BlackBoxCore.getBDumpManager().unregisterMonitor(monitor.asBinder());
    }

    /**
     * 判断虚拟应用进程是否正在运行。
     * <p>遍历系统运行中的进程列表，检查是否存在以 "p0"~"p99" 结尾的虚拟进程。</p>
     *
     * @return 如果有虚拟进程运行中则返回 {@code true}
     */
    public boolean isRunning() {
        ActivityManager am = (ActivityManager) BlackBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            for (int i = 0; i < ProxyManifest.FREE_COUNT; i++) {
                if (info.processName.endsWith("p" + i)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断指定包名是否已存在导出的 DEX 文件。
     *
     * @param packageName 目标应用包名
     * @return 如果 Dump 目录下存在该包名的 DEX 文件则返回 {@code true}
     */
    public boolean isExistDexFile(String packageName) {
        File[] files = new File(BlackBoxCore.get().getDexDumpDir(), packageName).listFiles();
        return files != null && files.length > 0;
    }
}
