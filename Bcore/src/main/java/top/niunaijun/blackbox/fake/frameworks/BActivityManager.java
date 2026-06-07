package top.niunaijun.blackbox.fake.frameworks;

import android.content.Intent;
import android.os.RemoteException;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.core.system.am.IBActivityManagerService;

/**
 * 虚拟环境 Activity 管理器门面类。
 * <p>
 * 提供 Activity 启动等操作的客户端接口，通过 Binder IPC 调用
 * {@link IBActivityManagerService} 服务端实现。
 * 采用单例模式，服务连接断开时自动重连。
 * </p>
 *
 * @author Milk
 * @see IBActivityManagerService
 * @see ServiceManager
 */
public class BActivityManager {
    private static final BActivityManager sActivityManager = new BActivityManager();
    private IBActivityManagerService mService;

    /**
     * 获取 BActivityManager 单例实例。
     *
     * @return BActivityManager 全局唯一实例
     */
    public static BActivityManager get() {
        return sActivityManager;
    }

    /**
     * 在虚拟环境中启动指定 Activity。
     *
     * @param intent   启动目标的 Intent
     * @param userId   虚拟用户 ID
     */
    public void startActivity(Intent intent, int userId) {
        try {
            getService().startActivity(intent, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IBActivityManagerService getService() {
        if (mService != null && mService.asBinder().isBinderAlive()) {
            return mService;
        }
        mService = IBActivityManagerService.Stub.asInterface(BlackBoxCore.get().getService(ServiceManager.ACTIVITY_MANAGER));
        return getService();
    }
}
