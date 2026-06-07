package top.niunaijun.blackbox.fake.frameworks;

import android.os.RemoteException;
import android.os.storage.StorageVolume;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.core.system.os.IBStorageManagerService;

/**
 * 虚拟环境存储管理器门面类。
 * <p>
 * 提供存储卷列表查询的客户端接口，通过 Binder IPC 调用
 * {@link IBStorageManagerService} 服务端实现。
 * 用于在虚拟环境中隔离和管理应用的存储挂载点。
 * </p>
 *
 * @author Milk
 * @see IBStorageManagerService
 */
public class BStorageManager {
    private static final BStorageManager sStorageManager = new BStorageManager();
    private IBStorageManagerService mService;

    /**
     * 获取 BStorageManager 单例实例。
     *
     * @return BStorageManager 全局唯一实例
     */
    public static BStorageManager get() {
        return sStorageManager;
    }

    /**
     * 获取虚拟用户的存储卷列表。
     *
     * @param uid         应用 UID
     * @param packageName 包名
     * @param flags       标志位
     * @param userId      虚拟用户 ID
     * @return 存储卷数组，失败时返回空数组
     */
    public StorageVolume[] getVolumeList(int uid, String packageName, int flags, int userId) {
        try {
            return getService().getVolumeList(uid, packageName, flags, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new StorageVolume[]{};
    }

    private IBStorageManagerService getService() {
        if (mService != null && mService.asBinder().isBinderAlive()) {
            return mService;
        }
        mService = IBStorageManagerService.Stub.asInterface(BlackBoxCore.get().getService(ServiceManager.STORAGE_MANAGER));
        return getService();
    }
}
