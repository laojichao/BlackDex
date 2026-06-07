package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Method;

import reflection.android.os.ServiceManager;
import reflection.com.android.internal.telephony.ITelephony;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Md5Utils;

/**
 * ITelephony 系统服务代理，拦截电话和设备标识相关调用。
 * <p>
 * 通过替换 ServiceManager 中 {@link Context#TELEPHONY_SERVICE} 的 Binder 对象实现拦截。
 * 主要拦截设备标识查询方法（getDeviceId、getImeiForSlot、getSubscriberId），
 * 返回宿主包名的 MD5 哈希值作为伪装标识符，防止虚拟环境中的真实设备信息泄露。
 * 同时拦截 isUserDataEnabled 始终返回 true。
 * </p>
 *
 * @author Milk
 * @see BinderInvocationStub
 * @see Md5Utils
 */
public class ITelephonyManagerProxy extends BinderInvocationStub {

    public ITelephonyManagerProxy() {
        super(ServiceManager.getService.call(Context.TELEPHONY_SERVICE));
    }

    @Override
    protected Object getWho() {
        IBinder telephony = ServiceManager.getService.call(Context.TELEPHONY_SERVICE);
        return ITelephony.Stub.asInterface.call(telephony);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    /** 拦截设备 ID 查询，返回宿主包名的 MD5 哈希值 */
    @ProxyMethod(name = "getDeviceId")
    public static class GetDeviceId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
//                MethodParameterUtils.replaceFirstAppPkg(args);
//                return method.invoke(who, args);
            return Md5Utils.md5(BlackBoxCore.getHostPkg());
        }
    }

    /** 拦截 IMEI 查询，返回宿主包名的 MD5 哈希值 */
    @ProxyMethod(name = "getImeiForSlot")
    public static class getImeiForSlot extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
//                MethodParameterUtils.replaceFirstAppPkg(args);
//                return method.invoke(who, args);
            return Md5Utils.md5(BlackBoxCore.getHostPkg());
        }
    }

    /** 拦截数据流量状态查询，始终返回 true（已启用） */
    @ProxyMethod(name = "isUserDataEnabled")
    public static class IsUserDataEnabled extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return true;
        }
    }

    /** 拦截 Subscriber ID 查询，返回宿主包名的 MD5 哈希值 */
    @ProxyMethod(name = "getSubscriberId")
    public static class GetSubscriberId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return Md5Utils.md5(BlackBoxCore.getHostPkg());
        }
    }
}
