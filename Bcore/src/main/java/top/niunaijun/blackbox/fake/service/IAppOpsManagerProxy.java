package top.niunaijun.blackbox.fake.service;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.Method;

import reflection.android.os.ServiceManager;
import reflection.com.android.internal.app.IAppOpsService;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.MethodParameterUtils;

/**
 * IAppOpsManager 系统服务代理，拦截应用操作权限检查相关调用。
 * <p>
 * 通过替换 ServiceManager 中 {@link Context#APP_OPS_SERVICE} 的 Binder 对象以及
 * AppOpsManager 实例内部的 mService 字段实现拦截。
 * 在所有方法调用前自动替换包名参数和用户 ID 参数。
 * </p>
 *
 * @author Milk
 * @see BinderInvocationStub
 * @see MethodParameterUtils
 */
public class IAppOpsManagerProxy extends BinderInvocationStub {
    public IAppOpsManagerProxy() {
        super(ServiceManager.getService.call(Context.APP_OPS_SERVICE));
    }

    @Override
    protected Object getWho() {
        IBinder call = ServiceManager.getService.call(Context.APP_OPS_SERVICE);
        return IAppOpsService.Stub.asInterface.call(call);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        if (reflection.android.app.AppOpsManager.mService != null) {
            AppOpsManager appOpsManager = (AppOpsManager) BlackBoxCore.getContext().getSystemService(Context.APP_OPS_SERVICE);
            try {
                reflection.android.app.AppOpsManager.mService.set(appOpsManager, (IInterface) getProxyInvocation());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        replaceSystemService(Context.APP_OPS_SERVICE);
    }

    /**
     * 全局拦截：替换第一个参数为宿主包名，最后一个参数为虚拟用户 ID。
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstAppPkg(args);
        MethodParameterUtils.replaceLastUserId(args);
        return super.invoke(proxy, method, args);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    /**
     * 拦截 checkPackage 方法，直接返回 MODE_ALLOWED 允许操作。
     */
    @ProxyMethod(name = "checkPackage")
    public static class CheckPackage extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            // todo
            return AppOpsManager.MODE_ALLOWED;
        }
    }

    /** 拦截 checkOperation，透传给原始方法 */
    @ProxyMethod(name = "checkPackage")
    public static class CheckOperation extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    /** 拦截 noteOperation，透传给原始方法 */
    @ProxyMethod(name = "noteOperation")
    public static class NoteOperation extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }
}
