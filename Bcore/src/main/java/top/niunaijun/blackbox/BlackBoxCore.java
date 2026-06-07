package top.niunaijun.blackbox;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Process;

import top.niunaijun.blackbox.app.configuration.ClientConfiguration;
import top.niunaijun.blackbox.fake.delegate.ContentProviderDelegate;
import top.niunaijun.blackbox.fake.frameworks.BDumpManager;
import top.niunaijun.blackbox.proxy.ProxyManifest;
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback;
import top.niunaijun.blackbox.fake.hook.HookManager;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.entity.pm.InstallResult;
import top.niunaijun.blackbox.core.system.DaemonService;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.utils.ShellUtils;
import top.niunaijun.blackbox.utils.compat.BuildCompat;
import top.niunaijun.blackbox.utils.compat.BundleCompat;
import top.niunaijun.blackbox.utils.provider.ProviderCall;
import top.niunaijun.blackbox.fake.frameworks.BActivityManager;
import top.niunaijun.blackbox.fake.frameworks.BPackageManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import me.weishu.reflection.Reflection;
import reflection.android.app.ActivityThread;
import top.niunaijun.blackbox.fake.frameworks.BStorageManager;
import top.niunaijun.blackbox.core.system.ServiceManager;

/**
 * BlackBox 核心管理类，负责整个虚拟化框架的初始化和生命周期管理。
 * <p>
 * 该类采用单例模式，作为虚拟应用环境的入口点，提供以下核心功能：
 * <ul>
 *     <li>进程类型识别（主进程、服务进程、虚拟应用进程）</li>
 *     <li>APK 安装、卸载和启动管理</li>
 *     <li>系统服务代理（Activity、Package、Storage、Dump）</li>
 *     <li>客户端配置管理与生命周期回调</li>
 * </ul>
 * <p>
 * 该类同时继承 {@link ClientConfiguration}，支持默认配置和自定义配置两种模式。
 *
 * @author Milk
 * @see ClientConfiguration
 * @see AppLifecycleCallback
 */
@SuppressLint("StaticFieldLeak")
public class BlackBoxCore extends ClientConfiguration {
    /** 日志标签 */
    public static final String TAG = "BlackBoxCore";
    /** 默认虚拟用户 ID */
    public static final int USER_ID = 0;

    private static final BlackBoxCore sBlackBoxCore = new BlackBoxCore();
    private static Context sContext;
    private ProcessType mProcessType;
    private final Map<String, IBinder> mServices = new HashMap<>();
    private ClientConfiguration mClientConfiguration;
    private AppLifecycleCallback mAppLifecycleCallback = AppLifecycleCallback.EMPTY;

    /**
     * 获取 BlackBoxCore 单例实例。
     *
     * @return BlackBoxCore 单例
     */
    public static BlackBoxCore get() {
        return sBlackBoxCore;
    }

    /**
     * 获取宿主应用的 PackageManager。
     *
     * @return PackageManager 实例
     */
    public static PackageManager getPackageManager() {
        return sContext.getPackageManager();
    }

    /**
     * 获取宿主应用包名。
     *
     * @return 宿主应用包名
     */
    public static String getHostPkg() {
        return get().getHostPackageName();
    }

    /**
     * 获取全局应用上下文。
     *
     * @return 应用 Context
     */
    public static Context getContext() {
        return sContext;
    }

    /**
     * 初始化虚拟化框架。
     * <p>
     * 该方法在 Application.attachBaseContext() 中调用，完成以下初始化：
     * <ol>
     *     <li>解除 Android 反射限制（Reflection.unseal）</li>
     *     <li>识别当前进程类型（主进程/服务进程/虚拟应用进程）</li>
     *     <li>初始化 Hook 管理器</li>
     * </ol>
     *
     * @param context              应用上下文
     * @param clientConfiguration 客户端配置，不可为 {@code null}
     * @throws IllegalArgumentException 当 clientConfiguration 为 null 时
     */
    public void doAttachBaseContext(Context context, ClientConfiguration clientConfiguration) {
        if (clientConfiguration == null) {
            throw new IllegalArgumentException("ClientConfiguration is null!");
        }
        Reflection.unseal(context);
        sContext = context;
        mClientConfiguration = clientConfiguration;
        mClientConfiguration.init();
        String processName = getProcessName(getContext());
        if (processName.equals(BlackBoxCore.getHostPkg())) {
            mProcessType = ProcessType.Main;
            startLogcat();
        } else if (processName.endsWith(getContext().getString(R.string.black_box_service_name))) {
            mProcessType = ProcessType.Server;
        } else {
            mProcessType = ProcessType.BAppClient;
        }
        if (BlackBoxCore.get().isVirtualProcess()) {
            if (processName.endsWith("p0")) {
//                android.os.Debug.waitForDebugger();
            }
//            android.os.Debug.waitForDebugger();
        }
        if (isServerProcess()) {
//            Intent intent = new Intent();
//            intent.setClass(getContext(), DaemonService.class);
//            if (BuildCompat.isOreo()) {
//                getContext().startForegroundService(intent);
//            } else {
//                getContext().startService(intent);
//            }
        }
        HookManager.get().init();
    }

    /**
     * 完成虚拟环境创建。
     * <p>在 Application.onCreate() 中调用，初始化 ContentProvider 代理和系统服务连接。</p>
     */
    public void doCreate() {
        if (isVirtualProcess()) {
            ContentProviderDelegate.init();
        }
        if (!isServerProcess()) {
            initService();
        }
    }

    private void initService() {
        get().getService(ServiceManager.ACTIVITY_MANAGER);
        get().getService(ServiceManager.PACKAGE_MANAGER);
        get().getService(ServiceManager.STORAGE_MANAGER);
        get().getService(ServiceManager.DUMP_MANAGER);
    }

    /**
     * 获取宿主线程对象（ActivityThread）。
     *
     * @return 当前进程的 ActivityThread 实例
     */
    public static Object mainThread() {
        return ActivityThread.currentActivityThread.call();
    }

    /**
     * 启动虚拟环境中的 Activity。
     *
     * @param intent   启动 Intent
     * @param userId   虚拟用户 ID
     */
    public void startActivity(Intent intent, int userId) {
        getBActivityManager().startActivity(intent, userId);
    }

    /** @return 虚拟包管理器实例 */
    public static BPackageManager getBPackageManager() {
        return BPackageManager.get();
    }

    /** @return 虚拟 Activity 管理器实例 */
    public static BActivityManager getBActivityManager() {
        return BActivityManager.get();
    }

    /** @return 虚拟存储管理器实例 */
    public static BStorageManager getBStorageManager() {
        return BStorageManager.get();
    }

    /** @return DEX Dump 管理器实例 */
    public static BDumpManager getBDumpManager() {
        return BDumpManager.get();
    }

    /**
     * 启动指定包名的虚拟应用。
     *
     * @param packageName 目标应用包名
     * @return 启动成功返回 {@code true}，未找到启动 Intent 则返回 {@code false}
     */
    public boolean launchApk(String packageName) {
        Intent launchIntentForPackage = getBPackageManager().getLaunchIntentForPackage(packageName, USER_ID);
        if (launchIntentForPackage == null) {
            return false;
        }
        startActivity(launchIntentForPackage, USER_ID);
        return true;
    }

    /**
     * 检查指定包是否已安装在虚拟环境中。
     *
     * @param packageName 包名
     * @return 已安装返回 {@code true}
     */
    public boolean isInstalled(String packageName) {
        return getBPackageManager().isInstalled(packageName, USER_ID);
    }

    /**
     * 从虚拟环境中卸载指定包。
     *
     * @param packageName 要卸载的包名
     */
    public void uninstallPackage(String packageName) {
        getBPackageManager().uninstallPackageAsUser(packageName, USER_ID);
    }

    /**
     * 通过宿主包名安装应用到虚拟环境（从系统已安装应用获取 APK 路径）。
     *
     * @param packageName 宿主已安装应用的包名
     * @return 安装结果
     * @throws PackageManager.NameNotFoundException 当包名在宿主中不存在时
     */
    public InstallResult installPackage(String packageName) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            return getBPackageManager().installPackageAsUser(packageInfo.applicationInfo.sourceDir, InstallOption.installBySystem(), USER_ID);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return new InstallResult().installError(e.getMessage());
        }
    }

    /**
     * 通过 APK 文件安装应用到虚拟环境。
     *
     * @param apk APK 文件
     * @return 安装结果
     */
    public InstallResult installPackage(File apk) {
        return getBPackageManager().installPackageAsUser(apk.getAbsolutePath(), InstallOption.installByStorage(), USER_ID);
    }

    /**
     * 通过 URI 安装应用到虚拟环境。
     *
     * @param apk APK 的 URI 地址
     * @return 安装结果
     */
    public InstallResult installPackage(Uri apk) {
        return getBPackageManager().installPackageAsUser(apk.toString(), InstallOption.installByStorage().makeUriFile(), USER_ID);
    }

    /** @return 当前应用生命周期回调 */
    public AppLifecycleCallback getAppLifecycleCallback() {
        return mAppLifecycleCallback;
    }

    /**
     * 设置应用生命周期回调。
     *
     * @param appLifecycleCallback 回调实例，不可为 {@code null}
     * @throws IllegalArgumentException 当参数为 null 时
     */
    public void setAppLifecycleCallback(AppLifecycleCallback appLifecycleCallback) {
        if (appLifecycleCallback == null) {
            throw new IllegalArgumentException("AppLifecycleCallback is null!");
        }
        mAppLifecycleCallback = appLifecycleCallback;
    }

    /**
     * 获取指定名称的系统服务 Binder 代理。
     * <p>通过 ContentProvider 与服务进程通信获取 Binder，结果会缓存以提高后续调用效率。</p>
     *
     * @param name 服务名称（参见 {@link ServiceManager} 常量）
     * @return 服务的 IBinder 代理
     */
    public IBinder getService(String name) {
        IBinder binder = mServices.get(name);
        if (binder != null && binder.isBinderAlive()) {
            return binder;
        }
        Bundle bundle = new Bundle();
        bundle.putString("_VM_|_server_name_", name);
        Bundle vm = ProviderCall.callSafely(ProxyManifest.getBindProvider(), "VM", null, bundle);
        assert vm != null;
        binder = BundleCompat.getBinder(vm, "_VM_|_server_");
        mServices.put(name, binder);
        return binder;
    }

    /** 进程类型枚举 */
    private enum ProcessType {
        Server,
        BAppClient,
        Main,
    }

    /** @return 当前进程是否为虚拟应用进程 */
    public boolean isVirtualProcess() {
        return mProcessType == ProcessType.BAppClient;
    }

    /** @return 当前进程是否为主进程 */
    public boolean isMainProcess() {
        return mProcessType == ProcessType.Main;
    }

    /** @return 当前进程是否为服务进程 */
    public boolean isServerProcess() {
        return mProcessType == ProcessType.Server;
    }

    @Override
    public String getHostPackageName() {
        return mClientConfiguration.getHostPackageName();
    }

    @Override
    public String getDexDumpDir() {
        return mClientConfiguration.getDexDumpDir();
    }

    @Override
    public boolean isFixCodeItem() {
        return mClientConfiguration.isFixCodeItem();
    }

    @Override
    public boolean isEnableHookDump() {
        return mClientConfiguration.isEnableHookDump();
    }

    private void startLogcat() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getContext().getPackageName() + "_logcat.txt");
        FileUtils.deleteDir(file);
        ShellUtils.execCommand("logcat -c", false);
        ShellUtils.execCommand("logcat >> " + file.getAbsolutePath() + " &", false);
    }

    private static String getProcessName(Context context) {
        int pid = Process.myPid();
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == pid) {
                processName = info.processName;
                break;
            }
        }
        if (processName == null) {
            throw new RuntimeException("processName = null");
        }
        return processName;
    }

    /**
     * 判断当前进程是否为 64 位。
     * <p>Android M 及以上使用 {@link Process#is64Bit()}，低版本通过 CPU ABI 判断。</p>
     *
     * @return 64 位进程返回 {@code true}
     */
    public static boolean is64Bit() {
        if (BuildCompat.isM()) {
            return Process.is64Bit();
        } else {
            return Build.CPU_ABI.equals("arm64-v8a");
        }
    }
}
