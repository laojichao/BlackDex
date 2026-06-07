package top.niunaijun.blackbox.core.system.os;

import android.os.Process;
import android.os.RemoteException;
import android.os.storage.StorageVolume;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.core.system.ISystemService;
import top.niunaijun.blackbox.core.system.user.BUserHandle;
import top.niunaijun.blackbox.utils.compat.BuildCompat;

/**
 * 虚拟环境中的存储管理服务。
 * <p>
 * 该服务为虚拟环境提供独立的存储卷管理，将存储路径重定向到虚拟环境的用户目录，
 * 使应用在虚拟环境中运行时访问的是隔离的存储空间。
 * </p>
 *
 * @see BEnvironment
 */
public class BStorageManagerService extends IBStorageManagerService.Stub implements ISystemService {
    private static final BStorageManagerService sService = new BStorageManagerService();

    /**
     * 获取BStorageManagerService单例。
     *
     * @return BStorageManagerService实例
     */
    public static BStorageManagerService get() {
        return sService;
    }

    public BStorageManagerService() {
    }

    /**
     * 获取虚拟环境的存储卷列表，将存储路径重定向到虚拟环境用户目录。
     *
     * @param uid         调用者的UID
     * @param packageName 包名
     * @param flags       查询标志
     * @param userId      目标用户ID
     * @return StorageVolume数组，如果无法获取则返回null
     * @throws RemoteException IPC通信异常
     */
    @Override
    public StorageVolume[] getVolumeList(int uid, String packageName, int flags, int userId) throws RemoteException {
        if (reflection.android.os.storage.StorageManager.getVolumeList == null) {
            return null;
        }
        try {
            StorageVolume[] storageVolumes = reflection.android.os.storage.StorageManager.getVolumeList.call(BUserHandle.getUserId(Process.myUid()), 0);
            if (storageVolumes == null)
                return null;
            for (StorageVolume storageVolume : storageVolumes) {
                reflection.android.os.storage.StorageVolume.mPath.set(storageVolume, BEnvironment.getExternalUserDir(userId));
                if (BuildCompat.isPie()) {
                    reflection.android.os.storage.StorageVolume.mInternalPath.set(storageVolume, BEnvironment.getExternalUserDir(userId));
                }
            }
            return storageVolumes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void systemReady() {

    }
}
