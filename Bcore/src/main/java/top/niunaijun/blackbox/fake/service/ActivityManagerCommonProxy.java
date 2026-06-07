package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;

/**
 * ActivityManager 公共方法代理集合。
 * <p>
 * 包含 {@link IActivityManagerProxy} 和 {@link IActivityTaskManagerProxy} 共享的
 * 方法钩子，如 startActivity、startActivities、activityResumed 等。
 * 通过 {@link ScanClass} 注解被两个代理类共同引用。
 * </p>
 * <p>
 * 大部分方法被拦截后直接返回默认值（如 0），用于阻止虚拟环境中对真实系统
 * ActivityManager 的直接调用；部分生命周期回调（如 activityResumed）则透传给原始方法。
 * </p>
 *
 * @author Milk
 * @see IActivityManagerProxy
 * @see IActivityTaskManagerProxy
 */
public class ActivityManagerCommonProxy {
    public static final String TAG = "CommonStub";

    /** 拦截 startActivity 调用，返回 0 阻止实际启动 */
    @ProxyMethod(name = "startActivity")
    public static class StartActivity extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    /** 拦截 startActivities 调用，返回 0 阻止实际启动 */
    @ProxyMethod(name = "startActivities")
    public static class StartActivities extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    /** 拦截 activityResumed 回调，透传给原始方法 */
    @ProxyMethod(name = "activityResumed")
    public static class ActivityResumed extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    /** 拦截 activityDestroyed 回调，透传给原始方法 */
    @ProxyMethod(name = "activityDestroyed")
    public static class ActivityDestroyed extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    /** 拦截 finishActivity 调用，透传给原始方法 */
    @ProxyMethod(name = "finishActivity")
    public static class FinishActivity extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }
}
