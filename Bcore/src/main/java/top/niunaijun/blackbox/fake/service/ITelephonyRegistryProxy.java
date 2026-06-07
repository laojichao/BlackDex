package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;

import reflection.android.os.ServiceManager;
import reflection.com.android.internal.telephony.ITelephonyRegistry;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.MethodParameterUtils;

/**
 * ITelephonyRegistry 系统服务代理，拦截电话状态监听相关调用。
 * <p>
 * 通过替换 ServiceManager 中 "telephony.registry" 服务的 Binder 对象实现拦截。
 * 对 listenForSubscriber 和 listen 方法的第一个包名参数进行替换，
 * 使其使用宿主应用包名，避免虚拟环境中的包名泄露给系统电话状态注册服务。
 * </p>
 *
 * @author Milk
 * @see BinderInvocationStub
 */
public class ITelephonyRegistryProxy extends BinderInvocationStub {
    public ITelephonyRegistryProxy() {
        super(ServiceManager.getService.call("telephony.registry"));
    }

    @Override
    protected Object getWho() {
        return ITelephonyRegistry.Stub.asInterface.call(ServiceManager.getService.call("telephony.registry"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("telephony.registry");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    /**
     * 拦截电话状态监听注册（带 Subscriber ID），替换包名参数后透传。
     */
    @ProxyMethod(name = "listenForSubscriber")
    public static class ListenForSubscriber extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    /**
     * 拦截电话状态监听注册，替换包名参数后透传。
     */
    @ProxyMethod(name = "listen")
    public static class Listen extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
