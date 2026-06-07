package top.niunaijun.blackbox.core.env;

import java.io.File;
import java.util.Locale;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * 虚拟环境文件系统路径管理类。
 * <p>
 * 定义虚拟环境中所有目录结构，模拟 Android 系统的文件布局：
 * <ul>
 *     <li>data/app/ - 应用安装目录</li>
 *     <li>data/user/ - 用户数据目录</li>
 *     <li>data/user_de/ - 设备加密用户数据目录</li>
 *     <li>storage/emulated/ - 外部存储目录</li>
 * </ul>
 *
 * @author Milk
 */
public class BEnvironment {
    private static final File sVirtualRoot = new File(BlackBoxCore.getContext().getCacheDir().getParent(), "virtual");
    private static final File sExternalVirtualRoot = BlackBoxCore.getContext().getExternalFilesDir("virtual");

    /** JUnit 测试 APK */
    public static File JUNIT_JAR = new File(getCacheDir(), "junit.apk");
    /** 空 DEX APK，用于加载空 cookie */
    public static File EMPTY_JAR = new File(getCacheDir(), "empty.apk");
    /** 虚拟机 APK */
    public static File VM_JAR = new File(getCacheDir(), "vm.apk");

    /**
     * 初始化虚拟环境目录结构，创建所有必要的根目录。
     */
    public static void load() {
        FileUtils.mkdirs(sVirtualRoot);
        FileUtils.mkdirs(sExternalVirtualRoot);
        FileUtils.mkdirs(getSystemDir());
        FileUtils.mkdirs(getCacheDir());
    }

    /** @return 虚拟环境根目录 */
    public static File getVirtualRoot() {
        return sVirtualRoot;
    }

    /** @return 虚拟环境外部存储根目录 */
    public static File getExternalVirtualRoot() {
        return sExternalVirtualRoot;
    }

    /** @return 系统配置目录 */
    public static File getSystemDir() {
        return new File(sVirtualRoot, "system");
    }

    /** @return 缓存目录 */
    public static File getCacheDir() {
        return new File(sVirtualRoot, "cache");
    }

    /** @return 用户信息配置文件 */
    public static File getUserInfoConf() {
        return new File(getSystemDir(), "user.conf");
    }

    /** @return UID 配置文件 */
    public static File getUidConf() {
        return new File(getSystemDir(), "uid.conf");
    }

    /** @return Xposed 模块配置文件 */
    public static File getXPModuleConf() {
        return new File(getSystemDir(), "xposed-module.conf");
    }

    /**
     * @param packageName 包名
     * @return 应用包配置文件
     */
    public static File getPackageConf(String packageName) {
        return new File(getAppDir(packageName), "package.conf");
    }

    /**
     * @param userId 用户 ID
     * @return 外部存储用户目录
     */
    public static File getExternalUserDir(int userId) {
        return new File(sExternalVirtualRoot, String.format(Locale.CHINA, "storage/emulated/%d/", userId));
    }

    /**
     * @param userId 用户 ID
     * @return 用户数据目录
     */
    public static File getUserDir(int userId) {
        return new File(sVirtualRoot, String.format(Locale.CHINA, "data/user/%d", userId));
    }

    /**
     * @param packageName 包名
     * @param userId      用户 ID
     * @return 设备加密数据目录
     */
    public static File getDeDataDir(String packageName, int userId) {
        return new File(sVirtualRoot, String.format(Locale.CHINA, "data/user_de/%d/%s", userId, packageName));
    }

    /**
     * @param packageName 包名
     * @param userId      用户 ID
     * @return 外部存储数据目录
     */
    public static File getExternalDataDir(String packageName, int userId) {
        return new File(getExternalUserDir(userId), String.format(Locale.CHINA, "Android/data/%s", packageName));
    }


    /**
     * @param packageName 包名
     * @param userId      用户 ID
     * @return 应用数据目录
     */
    public static File getDataDir(String packageName, int userId) {
        return new File(sVirtualRoot, String.format(Locale.CHINA, "data/user/%d/%s", userId, packageName));
    }

    public static File getExternalDataFilesDir(String packageName, int userId) {
        return new File(getExternalDataDir(packageName, userId), "files");
    }

    public static File getDataFilesDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "files");
    }

    public static File getExternalDataCacheDir(String packageName, int userId) {
        return new File(getExternalDataDir(packageName, userId), "cache");
    }

    public static File getDataCacheDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "cache");
    }

    public static File getDataLibDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "lib");
    }

    public static File getDataDatabasesDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "databases");
    }

    /** @return 应用安装根目录 */
    public static File getAppRootDir() {
        return getAppDir("");
    }

    /**
     * @param packageName 包名
     * @return 应用安装目录
     */
    public static File getAppDir(String packageName) {
        return new File(sVirtualRoot, "data/app/" + packageName);
    }

    /**
     * @param packageName 包名
     * @return base.apk 文件路径
     */
    public static File getBaseApkDir(String packageName) {
        return new File(sVirtualRoot, "data/app/" + packageName + "/base.apk");
    }

    /**
     * @param packageName 包名
     * @return 应用 Native 库目录
     */
    public static File getAppLibDir(String packageName) {
        return new File(getAppDir(packageName), "lib");
    }
}
