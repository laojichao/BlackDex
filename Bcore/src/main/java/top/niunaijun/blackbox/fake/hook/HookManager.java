package top.niunaijun.blackbox.fake.hook;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import top.niunaijun.blackbox.fake.service.IActivityManagerProxy;
import top.niunaijun.blackbox.fake.service.IActivityTaskManagerProxy;
import top.niunaijun.blackbox.fake.service.HCallbackProxy;
import top.niunaijun.blackbox.fake.service.IAlarmManagerProxy;
import top.niunaijun.blackbox.fake.service.IAppOpsManagerProxy;
import top.niunaijun.blackbox.fake.service.IJobServiceProxy;
import top.niunaijun.blackbox.fake.service.ITelephonyRegistryProxy;
import top.niunaijun.blackbox.fake.service.IDeviceIdentifiersPolicyProxy;
import top.niunaijun.blackbox.fake.service.IStorageManagerProxy;
import top.niunaijun.blackbox.fake.service.ILauncherAppsProxy;
import top.niunaijun.blackbox.fake.service.IPackageManagerProxy;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.delegate.AppInstrumentation;
import top.niunaijun.blackbox.fake.service.libcore.OsStub;
import top.niunaijun.blackbox.fake.service.ITelephonyManagerProxy;
import top.niunaijun.blackbox.utils.compat.BuildCompat;

/**
 * Hook 注册与管理中心，负责虚拟环境中所有服务代理和 Instrumentation 的生命周期管理。
 * <p>
 * Hook 注册生命周期：
 * <ol>
 *   <li>{@link #init()} 初始化阶段：仅在虚拟进程中执行</li>
 *   <li>{@link #addInjector(IInjectHook)} 注册阶段：按 Android 版本条件注册不同的代理</li>
 *   <li>{@link #injectAll()} 注入阶段：遍历所有已注册代理并执行注入</li>
 *   <li>{@link #checkEnv(Class)} 检查阶段：运行时环境异常时重新注入指定代理</li>
 * </ol>
 * </p>
 * <p>
 * 注册的代理包括：OsStub（文件路径重定向）、IActivityManager（Activity/Service 拦截）、
 * IPackageManager（包信息伪装）、ITelephonyManager（设备标识伪装）、HCallback（Activity
 * 启动拦截）、IAlarmManager、IStorageManager 等系统服务代理，以及 AppInstrumentation。
 * 部分代理仅在特定 Android 版本及以上注册。
 * </p>
 *
 * @author Milk
 * @see IInjectHook
 * @see ClassInvocationStub
 * @see BinderInvocationStub
 * @see AppInstrumentation
 */
public class HookManager {
    public static final String TAG = "HookManager";

    private static final HookManager sHookManager = new HookManager();

    /** Hook 代理注册表：类 -> IInjectHook 实例 */
    private final Map<Class<?>, IInjectHook> mInjectors = new HashMap<>();

    /**
     * 获取 HookManager 单例实例。
     *
     * @return HookManager 全局唯一实例
     */
    public static HookManager get() {
        return sHookManager;
    }

    /**
     * 初始化所有 Hook 代理。
     * <p>
     * 仅在虚拟进程（{@link BlackBoxCore#isVirtualProcess()}）中执行。
     * 按 Android API 版本条件注册不同的系统服务代理，最后统一执行注入。
     * </p>
     */
    public void init() {
        if (BlackBoxCore.get().isVirtualProcess()) {
            addInjector(new OsStub());
            addInjector(new IActivityManagerProxy());
            addInjector(new IPackageManagerProxy());
            addInjector(new ITelephonyManagerProxy());
            addInjector(new HCallbackProxy());
            addInjector(new IAppOpsManagerProxy());
            addInjector(new IAlarmManagerProxy());
            addInjector(new IStorageManagerProxy());
            addInjector(new ILauncherAppsProxy());
            addInjector(new IJobServiceProxy());
            addInjector(new ITelephonyRegistryProxy());

            addInjector(AppInstrumentation.get());

            // 11.0
            if (BuildCompat.isR()) {
            }
            // 10.0
            if (BuildCompat.isQ()) {
                addInjector(new IActivityTaskManagerProxy());
            }
            // 9.0
            if (BuildCompat.isPie()) {
            }
            // 8.0
            if (BuildCompat.isOreo()) {
                addInjector(new IDeviceIdentifiersPolicyProxy());
            }
            // 7.1
            if (BuildCompat.isN_MR1()) {
            }
            // 7.0
            if (BuildCompat.isN()) {
            }
            // 6.0
            if (BuildCompat.isM()) {
            }
            // 5.0
            if (BuildCompat.isL()) {
                addInjector(new IJobServiceProxy());
            }
        }
        injectAll();
    }

    /**
     * 检查指定代理的环境是否异常，如果异常则重新注入。
     * <p>
     * 当系统服务代理被外部（如其他 Hook 框架）篡改覆盖时，
     * 通过此方法检测并恢复虚拟环境的代理状态。
     * </p>
     *
     * @param clazz 要检查的代理类
     */
    public void checkEnv(Class<?> clazz) {
        IInjectHook iInjectHook = mInjectors.get(clazz);
        if (iInjectHook != null && iInjectHook.isBadEnv()) {
            Log.d(TAG, "checkEnv: " + clazz.getSimpleName() + " is bad env");
            iInjectHook.injectHook();
        }
    }

    /**
     * 注册一个 Hook 代理到注册表。
     *
     * @param injectHook 待注册的代理实例
     */
    void addInjector(IInjectHook injectHook) {
        mInjectors.put(injectHook.getClass(), injectHook);
    }

    /**
     * 执行所有已注册代理的注入操作。
     * <p>
     * 遍历注册表中所有 {@link IInjectHook} 实例并调用其 {@link IInjectHook#injectHook()} 方法。
     * 单个代理注入失败不影响其他代理的注入。
     * </p>
     */
    void injectAll() {
        for (IInjectHook value : mInjectors.values()) {
            try {
                value.injectHook();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
