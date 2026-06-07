package top.niunaijun.blackbox.fake.service;

import android.content.Context;

import java.lang.reflect.Method;

import reflection.android.content.pm.ILauncherApps;
import reflection.android.os.ServiceManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.utils.MethodParameterUtils;

/**
 * ILauncherApps 系统服务代理，拦截启动器应用查询相关调用。
 * <p>
 * 通过替换 ServiceManager 中 {@link Context#LAUNCHER_APPS_SERVICE} 的 Binder 对象
 * 实现拦截。在所有方法调用前自动将第一个包名参数替换为宿主包名，
 * 防止虚拟环境中的应用包名泄露给系统启动器。
 * </p>
 *
 * @author Milk
 * @see BinderInvocationStub
 */
public class ILauncherAppsProxy extends BinderInvocationStub {

    public ILauncherAppsProxy() {
        super(ServiceManager.getService.call(Context.LAUNCHER_APPS_SERVICE));
    }

    @Override
    protected Object getWho() {
        return ILauncherApps.Stub.asInterface.call(ServiceManager.getService.call(Context.LAUNCHER_APPS_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstAppPkg(args);
        // todo shouldHideFromSuggestions
        return super.invoke(proxy, method, args);
    }

}
