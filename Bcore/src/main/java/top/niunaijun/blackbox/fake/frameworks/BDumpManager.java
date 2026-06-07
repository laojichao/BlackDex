package top.niunaijun.blackbox.fake.frameworks;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.storage.StorageVolume;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.core.system.dump.IBDumpManagerService;
import top.niunaijun.blackbox.core.system.os.IBStorageManagerService;
import top.niunaijun.blackbox.entity.dump.DumpResult;

/**
 * 虚拟环境 Dex Dump 管理器门面类。
 * <p>
 * 提供 Dump 监控器的注册/注销和通知功能，通过 Binder IPC 调用
 * {@link IBDumpManagerService} 服务端实现。
 * 用于在虚拟环境中动态 Dump 目标应用的 Dex 文件。
 * </p>
 *
 * @author Milk
 * @see IBDumpManagerService
 */
public class BDumpManager {
    private static final BDumpManager sDumpManager = new BDumpManager();
    private IBDumpManagerService mService;

    /**
     * 获取 BDumpManager 单例实例。
     *
     * @return BDumpManager 全局唯一实例
     */
    public static BDumpManager get() {
        return sDumpManager;
    }

    /**
     * 注册一个 Dump 监控器。
     *
     * @param monitor 监控器的 IBinder 引用
     */
    public void registerMonitor(IBinder monitor) {
        try {
            getService().registerMonitor(monitor);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注销一个 Dump 监控器。
     *
     * @param monitor 监控器的 IBinder 引用
     */
    public void unregisterMonitor(IBinder monitor) {
        try {
            getService().unregisterMonitor(monitor);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知所有注册的监控器有新的 Dump 结果。
     *
     * @param result Dump 操作的结果数据
     */
    public void noticeMonitor(DumpResult result) {
        try {
            getService().noticeMonitor(result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private IBDumpManagerService getService() {
        if (mService != null && mService.asBinder().isBinderAlive()) {
            return mService;
        }
        mService = IBDumpManagerService.Stub.asInterface(BlackBoxCore.get().getService(ServiceManager.DUMP_MANAGER));
        return getService();
    }
}
