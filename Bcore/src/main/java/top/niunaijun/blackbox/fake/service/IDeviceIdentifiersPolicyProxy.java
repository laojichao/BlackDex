package top.niunaijun.blackbox.fake.service;


import java.lang.reflect.Method;

import reflection.android.os.IDeviceIdentifiersPolicyService;
import reflection.android.os.ServiceManager;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Md5Utils;

/**
 * IDeviceIdentifiersPolicy 系统服务代理（Android 8.0+），拦截设备标识查询。
 * <p>
 * 通过替换 ServiceManager 中 "device_identifiers" 服务的 Binder 对象实现拦截。
 * 将 getSerialForPackage 的返回值替换为宿主包名的 MD5 哈希，防止虚拟环境中
 * 序列号泄露真实设备信息。
 * </p>
 *
 * @author Milk
 * @see BinderInvocationStub
 * @see Md5Utils
 */
public class IDeviceIdentifiersPolicyProxy extends BinderInvocationStub {

    public IDeviceIdentifiersPolicyProxy() {
        super(ServiceManager.getService.call("device_identifiers"));
    }

    @Override
    protected Object getWho() {
        return IDeviceIdentifiersPolicyService.Stub.asInterface.call(ServiceManager.getService.call("device_identifiers"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("device_identifiers");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    /**
     * 拦截设备序列号查询，返回宿主包名的 MD5 哈希值作为伪装序列号。
     */
    @ProxyMethod(name = "getSerialForPackage")
    public static class GetSerialForPackage extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
//                args[0] = BlackBoxCore.getHostPkg();
//                return method.invoke(who, args);
            return Md5Utils.md5(BlackBoxCore.getHostPkg());
        }
    }
}
