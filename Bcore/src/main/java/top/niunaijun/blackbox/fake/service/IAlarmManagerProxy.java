package top.niunaijun.blackbox.fake.service;

import android.content.Context;

import java.lang.reflect.Method;

import reflection.android.app.IAlarmManager;
import reflection.android.os.ServiceManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.MethodParameterUtils;

/**
 * IAlarmManager 系统服务代理，拦截闹钟/定时器相关调用。
 * <p>
 * 通过替换 ServiceManager 中 {@link Context#ALARM_SERVICE} 的 Binder 对象实现拦截。
 * 主要对 set 方法的包名参数进行替换，使其使用宿主应用包名，避免虚拟环境中的包名泄露。
 * </p>
 *
 * @author Milk
 * @see BinderInvocationStub
 */
public class IAlarmManagerProxy extends BinderInvocationStub {

    public IAlarmManagerProxy() {
        super(ServiceManager.getService.call(Context.ALARM_SERVICE));
    }

    @Override
    protected Object getWho() {
        return IAlarmManager.Stub.asInterface.call(ServiceManager.getService.call(Context.ALARM_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.ALARM_SERVICE);
    }

    /**
     * 拦截闹钟设置方法，替换第一个参数中的应用包名为宿主包名。
     */
    @ProxyMethod(name = "set")
    public static class Set extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
