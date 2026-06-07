package top.niunaijun.blackbox.core.system;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.env.AppSystemEnv;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.core.system.am.BActivityManagerService;
import top.niunaijun.blackbox.core.system.os.BStorageManagerService;
import top.niunaijun.blackbox.core.system.pm.BPackageInstallerService;
import top.niunaijun.blackbox.core.system.pm.BPackageManagerService;
import top.niunaijun.blackbox.core.system.user.BUserHandle;
import top.niunaijun.blackbox.core.system.user.BUserManagerService;
import top.niunaijun.blackbox.utils.FileUtils;

import static top.niunaijun.blackbox.core.env.BEnvironment.EMPTY_JAR;
import static top.niunaijun.blackbox.core.env.BEnvironment.JUNIT_JAR;
import static top.niunaijun.blackbox.core.env.BEnvironment.VM_JAR;

/**
 * BlackBox 虚拟化框架的核心系统类，负责整个虚拟系统的初始化和启动。
 * <p>
 * 在系统启动时依次初始化：环境配置、包管理服务、用户管理服务、Activity管理服务、
 * 存储管理服务、包安装服务，然后执行预安装应用和JAR环境的初始化。
 * </p>
 *
 * @author Milk
 * @see BPackageManagerService
 * @see BActivityManagerService
 * @see BUserManagerService
 */
public class BlackBoxSystem {
    private static BlackBoxSystem sBlackBoxSystem;

    /**
     * 获取系统实例（双重检查锁定单例模式）。
     *
     * @return BlackBoxSystem 单例实例
     */
    public static BlackBoxSystem getSystem() {
        if (sBlackBoxSystem == null) {
            synchronized (BlackBoxSystem.class) {
                if (sBlackBoxSystem == null) {
                    sBlackBoxSystem = new BlackBoxSystem();
                }
            }
        }
        return sBlackBoxSystem;
    }

    /**
     * 启动虚拟系统。按顺序初始化所有子系统服务，
     * 并执行预安装应用的安装和JAR运行环境的初始化。
     */
    public void startup() {
        BEnvironment.load();

        BPackageManagerService.get().systemReady();
        BUserManagerService.get().systemReady();
        BActivityManagerService.get().systemReady();
        BStorageManagerService.get().systemReady();
        BPackageInstallerService.get().systemReady();

        List<String> preInstallPackages = AppSystemEnv.getPreInstallPackages();
        for (String preInstallPackage : preInstallPackages) {
            try {
                if (!BPackageManagerService.get().isInstalled(preInstallPackage, BUserHandle.USER_ALL)) {
                    PackageInfo packageInfo = BlackBoxCore.getPackageManager().getPackageInfo(preInstallPackage, 0);
                    BPackageManagerService.get().installPackageAsUser(packageInfo.applicationInfo.sourceDir, InstallOption.installBySystem(), BUserHandle.USER_ALL);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        initJarEnv();
    }

    private void initJarEnv() {
        try {
            InputStream junit = BlackBoxCore.getContext().getAssets().open("junit.jar");
            FileUtils.copyFile(junit, JUNIT_JAR);

            InputStream empty = BlackBoxCore.getContext().getAssets().open("empty.jar");
            FileUtils.copyFile(empty, EMPTY_JAR);

            InputStream vm = BlackBoxCore.getContext().getAssets().open("vm.jar");
            FileUtils.copyFile(vm, VM_JAR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
