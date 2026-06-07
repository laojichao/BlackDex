package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;

import reflection.android.app.ActivityManagerNative;
import reflection.android.app.ActivityManagerOreo;

import reflection.android.util.Singleton;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.delegate.ContentProviderDelegate;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.fake.hook.ScanClass;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.proxy.ProxyManifest;
import top.niunaijun.blackbox.utils.compat.BuildCompat;


/**
 * IActivityManager 系统服务代理，拦截 Activity/Service/Broadcast 等核心系统调用。
 * <p>
 * 通过替换 ActivityManagerNative（5.0-7.1）或 ActivityManagerOreo（8.0+）中的
 * Singleton 实例，代理 IActivityManager 接口的所有方法调用。
 * </p>
 * <p>
 * 主要拦截项：
 * <ul>
 *   <li>getContentProvider - 拦截 ContentProvider 获取并注入虚拟环境代理</li>
 *   <li>startService/stopService/bindService/unbindService - 阻止直接服务操作</li>
 *   <li>broadcastIntent - 阻止直接广播发送</li>
 *   <li>registerReceiver - 阻止直接注册广播接收器</li>
 *   <li>getIntentSender - 阻止 PendingIntent 创建</li>
 * </ul>
 * </p>
 * <p>
 * 同时通过 {@link ScanClass} 引用 {@link ActivityManagerCommonProxy} 的公共方法钩子。
 * </p>
 *
 * @author Milk
 * @see ActivityManagerCommonProxy
 * @see ContentProviderDelegate
 */
@ScanClass(ActivityManagerCommonProxy.class)
public class IActivityManagerProxy extends ClassInvocationStub {
    public static final String TAG = "ActivityManagerStub";

    @Override
    protected Object getWho() {
        Object iActivityManager = null;
        if (BuildCompat.isOreo()) {
            iActivityManager = ActivityManagerOreo.IActivityManagerSingleton.get();
        } else if (BuildCompat.isL()) {
            iActivityManager = ActivityManagerNative.gDefault.get();
        }
        return Singleton.get.call(iActivityManager);
    }

    @Override
    protected void inject(Object base, Object proxy) {
        Object iActivityManager = null;
        if (BuildCompat.isOreo()) {
            iActivityManager = ActivityManagerOreo.IActivityManagerSingleton.get();
        } else if (BuildCompat.isL()) {
            iActivityManager = ActivityManagerNative.gDefault.get();
        }
        Singleton.mInstance.set(iActivityManager, proxy);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    /**
     * 拦截 getContentProvider 方法，对 settings/media/telephony 的 ContentProvider
     * 注入虚拟环境代理，其他 Provider 使用宿主包名查询。
     */
    @ProxyMethod(name = "getContentProvider")
    public static class GetContentProvider extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int authIndex = getAuthIndex();
            Object auth = args[authIndex];

            if (auth instanceof String) {
                if (ProxyManifest.isProxy((String) auth)) {
                    return method.invoke(who, args);
                }

                if (BuildCompat.isQ()) {
                    args[1] = BlackBoxCore.getHostPkg();
                }

                if (auth.equals("settings") || auth.equals("media") || auth.equals("telephony")) {
                    Object content = method.invoke(who, args);
                    ContentProviderDelegate.update(content, (String) auth);
                    return content;
                }
            }
            return method.invoke(who, args);
        }

        private int getAuthIndex() {
            // 10.0
            if (BuildCompat.isQ()) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    /** 拦截 startService，返回 0 阻止在虚拟环境中直接调用系统服务 */
    @ProxyMethod(name = "startService")
    public static class StartService extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    /** 拦截 stopService，返回 0 阻止在虚拟环境中直接调用系统服务 */
    @ProxyMethod(name = "stopService")
    public static class StopService extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    /** 拦截 bindService，返回 0 阻止在虚拟环境中直接绑定系统服务 */
    @ProxyMethod(name = "bindService")
    public static class BindService extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    /**
     * 拦截 bindIsolatedService（Android 10.0+），继承 BindService 的拦截逻辑，
     * 并在前置钩子中将 instanceName 参数置 null。
     */
    // 10.0
    @ProxyMethod(name = "bindIsolatedService")
    public static class BindIsolatedService extends BindService {
        @Override
        protected Object beforeHook(Object who, Method method, Object[] args) throws Throwable {
            // instanceName
            args[6] = null;
            return super.beforeHook(who, method, args);
        }
    }

    /** 拦截 unbindService，返回 0 阻止在虚拟环境中直接解绑系统服务 */
    @ProxyMethod(name = "unbindService")
    public static class UnbindService extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    /** 拦截 getIntentSender，返回 null 阻止创建 PendingIntent */
    @ProxyMethod(name = "getIntentSender")
    public static class GetIntentSender extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod(name = "getIntentSenderWithFeature")
    public static class GetIntentSenderWithFeature extends GetIntentSender {
    }

    @ProxyMethod(name = "broadcastIntentWithFeature")
    public static class BroadcastIntentWithFeature extends BroadcastIntent {
    }

    /** 拦截 broadcastIntent，返回 0 阻止在虚拟环境中直接发送广播 */
    @ProxyMethod(name = "broadcastIntent")
    public static class BroadcastIntent extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @ProxyMethod(name = "sendIntentSender")
    public static class SendIntentSender extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    /** 拦截 registerReceiver，返回 null 阻止在虚拟环境中直接注册广播接收器 */
    @ProxyMethod(name = "registerReceiver")
    public static class RegisterReceiver extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod(name = "grantUriPermission")
    public static class GrantUriPermission extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }
}
