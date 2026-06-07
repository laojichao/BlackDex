package top.niunaijun.blackbox.core.system.dump;

import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackbox.entity.dump.DumpResult;

/**
 * 虚拟环境的Dump管理服务。
 * <p>
 * 该服务负责管理Dump监控器的注册、注销和通知机制。
 * 外部监控器通过{@link #registerMonitor}注册后，可以接收到
 * {@link DumpResult}类型的Dump结果通知。监控器通过IBinder的死亡回调
 * 自动清理已断开的连接。
 * </p>
 *
 * @see IBDumpMonitor
 * @see DumpResult
 */
public class BDumpManagerService extends IBDumpManagerService.Stub {
    private static final BDumpManagerService sService = new BDumpManagerService();
    private final List<IBinder> mMonitors = new ArrayList<>();

    /**
     * 获取BDumpManagerService单例。
     *
     * @return BDumpManagerService实例
     */
    public static BDumpManagerService get() {
        return sService;
    }

    /**
     * 注册Dump监控器。当监控器Binder死亡时自动注销。
     *
     * @param monitor 监控器的IBinder对象
     */
    @Override
    public void registerMonitor(IBinder monitor) {
        try {
            monitor.linkToDeath(new DeathRecipient() {
                @Override
                public void binderDied() {
                    monitor.unlinkToDeath(this, 0);
                    mMonitors.remove(monitor);
                }
            }, 0);
        } catch (RemoteException ignored) {
        }
        mMonitors.add(monitor);
    }

    /**
     * 注销Dump监控器。
     *
     * @param monitor 监控器的IBinder对象
     */
    @Override
    public void unregisterMonitor(IBinder monitor) {
        mMonitors.remove(monitor);
    }

    /**
     * 向所有已注册的监控器发送Dump结果通知。
     *
     * @param result Dump结果数据
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void noticeMonitor(DumpResult result) throws RemoteException {
        for (IBinder monitor : mMonitors) {
            if (monitor.isBinderAlive()) {
                IBDumpMonitor ibDumpMonitor = IBDumpMonitor.Stub.asInterface(monitor);
                ibDumpMonitor.onDump(result);
            }
        }
    }
}
