package top.niunaijun.blackbox.fake.service;

import android.os.IInterface;
import android.os.Process;
import android.os.storage.StorageVolume;

import java.lang.reflect.Method;

import reflection.android.os.ServiceManager;
import reflection.android.os.mount.IMountService;
import reflection.android.os.storage.IStorageManager;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.compat.BuildCompat;

/**
 * IStorageManager/MountService 系统服务代理，拦截存储卷列表查询。
 * <p>
 * 通过替换 ServiceManager 中 "mount" 服务的 Binder 对象实现拦截。
 * 兼容 Android O (8.0) 前后的接口差异（IStorageManager vs IMountService）。
 * 拦截 getVolumeList 方法以返回虚拟环境隔离的存储卷列表。
 * </p>
 *
 * @author Milk
 * @see BinderInvocationStub
 * @see top.niunaijun.blackbox.fake.frameworks.BStorageManager
 */
public class IStorageManagerProxy extends BinderInvocationStub {

    public IStorageManagerProxy() {
        super(ServiceManager.getService.call("mount"));
    }

    @Override
    protected Object getWho() {
        IInterface mount;
        if (BuildCompat.isOreo()) {
            mount = IStorageManager.Stub.asInterface.call(ServiceManager.getService.call("mount"));
        } else {
            mount = IMountService.Stub.asInterface.call(ServiceManager.getService.call("mount"));
        }
        return mount;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("mount");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    /**
     * 拦截存储卷列表查询，优先从虚拟环境存储管理器获取。
     * 兼容 args 为 null 的情况。
     */
    @ProxyMethod(name = "getVolumeList")
    public static class GetVolumeList extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args == null) {
                StorageVolume[] volumeList = BlackBoxCore.getBStorageManager().getVolumeList(Process.myUid(), null, 0, BActivityThread.getUserId());
                if (volumeList == null) {
                    return method.invoke(who, args);
                }
                return volumeList;
            }
            try {
                int uid = (int) args[0];
                String packageName = (String) args[1];
                int flags = (int) args[2];
                StorageVolume[] volumeList = BlackBoxCore.getBStorageManager().getVolumeList(uid, packageName, flags, BActivityThread.getUserId());
                if (volumeList == null) {
                    return method.invoke(who, args);
                }
            } catch (Throwable t) {
                return method.invoke(who, args);
            }
            return method.invoke(who, args);
        }
    }
}
