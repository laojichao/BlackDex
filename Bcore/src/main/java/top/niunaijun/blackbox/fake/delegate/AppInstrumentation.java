package top.niunaijun.blackbox.fake.delegate;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import reflection.android.app.ActivityThread;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.VMCore;
import top.niunaijun.blackbox.fake.hook.HookManager;
import top.niunaijun.blackbox.fake.hook.IInjectHook;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.utils.compat.ContextCompat;
import top.niunaijun.blackbox.fake.service.HCallbackProxy;

/**
 * 应用级 Instrumentation 代理，拦截 Android 应用的生命周期回调。
 * <p>
 * 通过替换 ActivityThread 中的 mInstrumentation 字段，拦截 Application 和 Activity 的
 * 创建过程，实现虚拟环境下的包名修正、Dex Dump 支持以及 HCallback 环境检查。
 * 采用双重检查锁的单例模式，确保全局唯一实例。
 * </p>
 *
 * @author Milk
 * @see BaseInstrumentationDelegate
 * @see IInjectHook
 * @see HookManager
 */
public final class AppInstrumentation extends BaseInstrumentationDelegate implements IInjectHook {

    private static final String TAG = AppInstrumentation.class.getSimpleName();

    private static AppInstrumentation sAppInstrumentation;

    /**
     * 获取 AppInstrumentation 单例实例（双重检查锁）。
     *
     * @return AppInstrumentation 全局唯一实例
     */
    public static AppInstrumentation get() {
        if (sAppInstrumentation == null) {
            synchronized (AppInstrumentation.class) {
                if (sAppInstrumentation == null) {
                    sAppInstrumentation = new AppInstrumentation();
                }
            }
        }
        return sAppInstrumentation;
    }

    public AppInstrumentation() {
    }

    /**
     * 注入 Hook：将当前 ActivityThread 的 Instrumentation 替换为本实例。
     * <p>
     * 如果当前 Instrumentation 已经是本实例或已包含本实例的委托链，则跳过替换。
     * 替换成功后，mBaseInstrumentation 保存原始 Instrumentation 引用。
     * </p>
     */
    @Override
    public void injectHook() {
        try {
            Instrumentation mInstrumentation = getCurrInstrumentation();
            if (mInstrumentation == this || checkInstrumentation(mInstrumentation))
                return;
            mBaseInstrumentation = (Instrumentation) mInstrumentation;
            ActivityThread.mInstrumentation.set(BlackBoxCore.mainThread(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Instrumentation getCurrInstrumentation() {
        Object currentActivityThread = BlackBoxCore.mainThread();
        return ActivityThread.mInstrumentation.get(currentActivityThread);
    }

    /**
     * 检查当前环境是否异常（Instrumentation 被外部篡改）。
     *
     * @return 如果当前 Instrumentation 不包含本实例则返回 true，表示环境异常
     */
    @Override
    public boolean isBadEnv() {
        return !checkInstrumentation(getCurrInstrumentation());
    }

    /**
     * 检查给定的 Instrumentation 是否已经包含本实例（直接或通过委托字段）。
     * <p>
     * 遍历 Instrumentation 的类层次结构，检查其声明的字段中是否有类型为
     * Instrumentation 且值为 AppInstrumentation 实例的字段。
     * </p>
     *
     * @param instrumentation 待检查的 Instrumentation 实例
     * @return 如果已包含本实例返回 true
     */
    private boolean checkInstrumentation(Instrumentation instrumentation) {
        if (instrumentation instanceof AppInstrumentation) {
            return true;
        }
        Class<?> clazz = instrumentation.getClass();
        if (Instrumentation.class.equals(clazz)) {
            return false;
        }
        do {
            assert clazz != null;
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Instrumentation.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        Object obj = field.get(instrumentation);
                        if ((obj instanceof AppInstrumentation)) {
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Instrumentation.class.equals(clazz));
        return false;
    }

    private void checkHCallback() {
        HookManager.get().checkEnv(HCallbackProxy.class);
    }

    /**
     * 创建 Application 实例时的拦截回调。
     * <p>
     * 在 Application 创建前修正 Context 兼容性问题，如果开启了 Dex Dump 功能，
     * 则通过 VMCore 注入 Dex Dump 钩子将运行时 Dex 文件保存到指定目录。
     * </p>
     *
     * @param cl        应用的 ClassLoader
     * @param className Application 类的全限定名
     * @param context   应用 Context
     * @return 创建的 Application 实例
     * @throws InstantiationException 实例化失败
     * @throws IllegalAccessException   访问权限异常
     * @throws ClassNotFoundException   类未找到
     */
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ContextCompat.fix(context);
        if (BlackBoxCore.get().isEnableHookDump()) {
            String absolutePath = new File(BlackBoxCore.get().getDexDumpDir(), context.getPackageName()).getAbsolutePath();
            FileUtils.mkdirs(absolutePath);
            Class<?> aClass = cl.loadClass(VMCore.class.getName());
            try {
                Method initDumpDex = aClass.getDeclaredMethod("hookDumpDex", String.class);
                initDumpDex.setAccessible(true);
                initDumpDex.invoke(null, absolutePath);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return super.newApplication(cl, className, context);
    }

    /**
     * Activity onCreate 回调拦截。
     * <p>
     * 在 Activity 创建前检查 HCallback 环境，修正 Context 兼容性，
     * 并打印 Activity 创建日志。
     * </p>
     *
     * @param activity 当前创建的 Activity
     * @param icicle   保存的状态 Bundle
     */
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        checkHCallback();
        Log.d(TAG, "callActivityOnCreate: " + activity.getClass().getName());
        ContextCompat.fix(activity);
        super.callActivityOnCreate(activity, icicle);
    }

    /**
     * Application onCreate 回调拦截，在应用创建前检查 HCallback 环境。
     *
     * @param app 当前创建的 Application
     */
    @Override
    public void callApplicationOnCreate(Application app) {
        checkHCallback();
        super.callApplicationOnCreate(app);
    }

    /**
     * 创建 Activity 实例，优先使用虚拟环境的 ClassLoader，失败时回退到原始 Instrumentation。
     *
     * @param cl        ClassLoader
     * @param className Activity 类的全限定名
     * @param intent    启动 Intent
     * @return 创建的 Activity 实例
     * @throws InstantiationException 实例化失败
     * @throws IllegalAccessException   访问权限异常
     * @throws ClassNotFoundException   类未找到
     */
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            return super.newActivity(cl, className, intent);
        } catch (ClassNotFoundException e) {
            return mBaseInstrumentation.newActivity(cl, className, intent);
        }
    }
}
