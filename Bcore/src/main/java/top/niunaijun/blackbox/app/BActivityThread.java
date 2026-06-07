package top.niunaijun.blackbox.app;

import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import reflection.android.app.ActivityThread;
import reflection.android.app.ContextImpl;
import reflection.android.app.LoadedApk;
import top.niunaijun.blackbox.core.IBActivityThread;
import top.niunaijun.blackbox.core.VMCore;
import top.niunaijun.blackbox.entity.AppConfig;
import top.niunaijun.blackbox.core.IOCore;
import top.niunaijun.blackbox.entity.dump.DumpResult;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.utils.Slog;
import top.niunaijun.blackbox.BlackBoxCore;

/**
 * 虚拟应用进程线程管理类。
 * <p>
 * 负责虚拟环境中应用的完整生命周期管理，包括：
 * <ul>
 *     <li>进程初始化与绑定（{@link #initProcess}、{@link #bindApplication}）</li>
 *     <li>Application 创建与 LoadedApk 配置</li>
 *     <li>ContentProvider 注册与 DEX Dump 触发</li>
 *     <li>应用信息查询（包名、进程名、UID 等）</li>
 * </ul>
 *
 * @author Milk
 * @see BlackBoxCore
 * @see VMCore
 */
public class BActivityThread extends IBActivityThread.Stub {
    /** 日志标签 */
    public static final String TAG = "BActivityThread";

    private static BActivityThread sBActivityThread;
    private AppBindData mBoundApplication;
    private Application mInitialApplication;
    private AppConfig mAppConfig;
    private final List<ProviderInfo> mProviders = new ArrayList<>();

    /**
     * 获取 BActivityThread 单例（双重检查锁定）。
     *
     * @return BActivityThread 实例
     */
    public static BActivityThread currentActivityThread() {
        if (sBActivityThread == null) {
            synchronized (BActivityThread.class) {
                if (sBActivityThread == null) {
                    sBActivityThread = new BActivityThread();
                }
            }
        }
        return sBActivityThread;
    }

    /**
     * 获取当前虚拟应用的配置信息。
     *
     * @return AppConfig 配置，未初始化时返回 {@code null}
     */
    public static synchronized AppConfig getAppConfig() {
        return currentActivityThread().mAppConfig;
    }

    /**
     * 获取当前虚拟应用注册的 ContentProvider 列表。
     *
     * @return ProviderInfo 列表
     */
    public static List<ProviderInfo> getProviders() {
        return currentActivityThread().mProviders;
    }

    /** @return 当前虚拟应用进程名，未初始化时返回 {@code null} */
    public static String getAppProcessName() {
        if (getAppConfig() != null) {
            return getAppConfig().processName;
        } else if (currentActivityThread().mBoundApplication != null) {
            return currentActivityThread().mBoundApplication.processName;
        } else {
            return null;
        }
    }

    /** @return 当前虚拟应用包名，未初始化时返回 {@code null} */
    public static String getAppPackageName() {
        if (getAppConfig() != null) {
            return getAppConfig().packageName;
        } else if (currentActivityThread().mInitialApplication != null) {
            return currentActivityThread().mInitialApplication.getPackageName();
        } else {
            return null;
        }
    }

    /** @return 当前虚拟应用的 Application 实例 */
    public static Application getApplication() {
        return currentActivityThread().mInitialApplication;
    }

    /** @return 虚拟应用 PID，未初始化时返回 -1 */
    public static int getAppPid() {
        return getAppConfig() == null ? -1 : getAppConfig().bpid;
    }

    /** @return 虚拟应用 UID，未初始化时返回 10000 */
    public static int getAppUid() {
        return getAppConfig() == null ? 10000 : getAppConfig().buid;
    }

    /** @return 虚拟应用基础 UID，未初始化时返回 10000 */
    public static int getBaseAppUid() {
        return getAppConfig() == null ? 10000 : getAppConfig().baseBUid;
    }

    /** @return 真实 UID，未初始化时返回 -1 */
    public static int getUid() {
        return getAppConfig() == null ? -1 : getAppConfig().uid;
    }

    /** @return 用户 ID，未初始化时返回 0 */
    public static int getUserId() {
        return getAppConfig() == null ? 0 : getAppConfig().userId;
    }

    /**
     * 初始化虚拟进程。
     * <p>每个虚拟进程只能初始化一次，重复调用将抛出异常。</p>
     *
     * @param appConfig 应用配置信息
     * @throws RuntimeException 如果进程已初始化
     */
    public void initProcess(AppConfig appConfig) {
        if (this.mAppConfig != null) {
            throw new RuntimeException("reject init process: " + appConfig.processName + ", this process is : " + this.mAppConfig.processName);
        }
        this.mAppConfig = appConfig;
    }

    /** @return 应用是否已完成绑定（bindApplication） */
    public boolean isInit() {
        return mBoundApplication != null;
    }

    /**
     * 绑定应用到虚拟环境。
     * <p>如果当前不在主线程，会自动切换到主线程执行并阻塞等待完成。</p>
     *
     * @param packageName 目标应用包名
     * @param processName 目标进程名
     */
    public void bindApplication(final String packageName, final String processName) {
        if (mAppConfig == null) {
            return;
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ConditionVariable conditionVariable = new ConditionVariable();
            new Handler(Looper.getMainLooper()).post(() -> {
                handleBindApplication(packageName, processName);
                conditionVariable.open();
            });
            conditionVariable.block();
        } else {
            handleBindApplication(packageName, processName);
        }
    }

    /**
     * 在主线程中处理应用绑定的核心逻辑。
     * <p>
     * 该方法完成以下关键步骤：
     * 1. 查询包信息和 Provider 列表
     * 2. 创建包上下文并配置 LoadedApk
     * 3. 初始化 IO 重定向和 VMCore
     * 4. 创建 Application 实例
     * 5. 触发 DEX Dump（仅主进程）
     * </p>
     */
    private synchronized void handleBindApplication(String packageName, String processName) {
        DumpResult result = new DumpResult();
        result.packageName = packageName;
        result.dir = new File(BlackBoxCore.get().getDexDumpDir(), packageName).getAbsolutePath();
        try {
            PackageInfo packageInfo = BlackBoxCore.getBPackageManager().getPackageInfo(packageName, PackageManager.GET_PROVIDERS, BActivityThread.getUserId());
            if (packageInfo == null)
                return;
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if (packageInfo.providers == null) {
                packageInfo.providers = new ProviderInfo[]{};
            }
            mProviders.addAll(Arrays.asList(packageInfo.providers));

            Object boundApplication = ActivityThread.mBoundApplication.get(BlackBoxCore.mainThread());

            Context packageContext = createPackageContext(applicationInfo);
            Object loadedApk = ContextImpl.mPackageInfo.get(packageContext);
            LoadedApk.mSecurityViolation.set(loadedApk, false);
            // fix applicationInfo
            LoadedApk.mApplicationInfo.set(loadedApk, applicationInfo);

            // clear dump file
            FileUtils.deleteDir(new File(BlackBoxCore.get().getDexDumpDir(), packageName));

            // init vmCore
            VMCore.init(Build.VERSION.SDK_INT);
            assert packageContext != null;
            IOCore.get().enableRedirect(packageContext);

            AppBindData bindData = new AppBindData();
            bindData.appInfo = applicationInfo;
            bindData.processName = processName;
            bindData.info = loadedApk;
            bindData.providers = mProviders;

            ActivityThread.AppBindData.instrumentationName.set(boundApplication,
                    new ComponentName(bindData.appInfo.packageName, Instrumentation.class.getName()));
            ActivityThread.AppBindData.appInfo.set(boundApplication, bindData.appInfo);
            ActivityThread.AppBindData.info.set(boundApplication, bindData.info);
            ActivityThread.AppBindData.processName.set(boundApplication, bindData.processName);
            ActivityThread.AppBindData.providers.set(boundApplication, bindData.providers);

            mBoundApplication = bindData;

            Application application = null;
            BlackBoxCore.get().getAppLifecycleCallback().beforeCreateApplication(packageName, processName, packageContext);
            try {
                ClassLoader call = LoadedApk.getClassloader.call(loadedApk);
                application = LoadedApk.makeApplication.call(loadedApk, false, null);
            } catch (Throwable e) {
                Slog.e(TAG, "Unable to makeApplication");
                e.printStackTrace();
            }
            mInitialApplication = application;
            ActivityThread.mInitialApplication.set(BlackBoxCore.mainThread(), mInitialApplication);
            if (Objects.equals(packageName, processName)) {
                ClassLoader loader;
                if (application == null) {
                    loader = LoadedApk.getClassloader.call(loadedApk);
                } else {
                    loader = application.getClassLoader();
                }
                handleDumpDex(packageName, result, loader);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            mAppConfig = null;
            BlackBoxCore.getBDumpManager().noticeMonitor(result.dumpError(e.getMessage()));
            BlackBoxCore.get().uninstallPackage(packageName);
        }
    }

    /**
     * 在独立线程中执行 DEX Dump 操作。
     * <p>等待 500ms 确保应用初始化完成后，通过 VMCore 进行 cookie Dump，最终通知监听器结果。</p>
     */
    private void handleDumpDex(String packageName, DumpResult result, ClassLoader classLoader) {
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            try {
                VMCore.cookieDumpDex(classLoader, packageName);
            } finally {
                mAppConfig = null;
                File dir = new File(result.dir);
                if (!dir.exists() || dir.listFiles().length == 0) {
                    BlackBoxCore.getBDumpManager().noticeMonitor(result.dumpError("not found dex file"));
                } else {
                    BlackBoxCore.getBDumpManager().noticeMonitor(result.dumpSuccess());
                }
                BlackBoxCore.get().uninstallPackage(packageName);
            }
        }).start();
    }

    private Context createPackageContext(ApplicationInfo info) {
        try {
            return BlackBoxCore.getContext().createPackageContext(info.packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IBinder getActivityThread() {
        return ActivityThread.getApplicationThread.call(BlackBoxCore.mainThread());
    }

    @Override
    public void bindApplication() {
        if (!isInit()) {
            bindApplication(getAppPackageName(), getAppProcessName());
        }
    }

    /** 应用绑定数据内部类，存储进程名、ApplicationInfo、LoadedApk 和 Provider 列表 */
    public static class AppBindData {
        String processName;
        ApplicationInfo appInfo;
        List<ProviderInfo> providers;
        Object info;
    }
}
