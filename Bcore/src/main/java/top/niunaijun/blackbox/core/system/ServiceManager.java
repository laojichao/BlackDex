package top.niunaijun.blackbox.core.system;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

import top.niunaijun.blackbox.core.system.am.BActivityManagerService;
import top.niunaijun.blackbox.core.system.dump.BDumpManagerService;
import top.niunaijun.blackbox.core.system.os.BStorageManagerService;
import top.niunaijun.blackbox.core.system.pm.BPackageManagerService;
import top.niunaijun.blackbox.core.system.user.BUserManagerService;

/**
 * 虚拟系统服务管理器，负责注册和获取所有 BlackBox 框架的系统服务。
 * <p>
 * 模拟 Android 系统的 {@code ServiceManager}，在构造时将所有虚拟系统服务
 * （Activity管理、包管理、存储管理、用户管理、Dump管理）注册到内部缓存中，
 * 供虚拟进程通过 Binder 调用。
 * </p>
 *
 * @author Milk
 * @see BActivityManagerService
 * @see BPackageManagerService
 * @see BUserManagerService
 */
public class ServiceManager {
    private static ServiceManager sServiceManager = null;
    public static final String ACTIVITY_MANAGER = "activity_manager";
    public static final String PACKAGE_MANAGER = "package_manager";
    public static final String STORAGE_MANAGER = "storage_manager";
    public static final String USER_MANAGER = "user_manager";
    public static final String DUMP_MANAGER = "dump_manager";

    private final Map<String, IBinder> mCaches = new HashMap<>();

    /**
     * 获取服务管理器单例实例（双重检查锁定）。
     *
     * @return 服务管理器实例
     */
    public static ServiceManager get() {
        if (sServiceManager == null) {
            synchronized (ServiceManager.class) {
                if (sServiceManager == null) {
                    sServiceManager = new ServiceManager();
                }
            }
        }
        return sServiceManager;
    }

    /**
     * 根据服务名称获取对应的 Binder 服务。
     *
     * @param name 服务名称，如 {@link #ACTIVITY_MANAGER}、{@link #PACKAGE_MANAGER} 等
     * @return 对应服务的 IBinder，未找到返回 null
     */
    public static IBinder getService(String name) {
        return get().getServiceInternal(name);
    }

    private ServiceManager() {
        mCaches.put(ACTIVITY_MANAGER, BActivityManagerService.get());
        mCaches.put(PACKAGE_MANAGER, BPackageManagerService.get());
        mCaches.put(STORAGE_MANAGER, BStorageManagerService.get());
        mCaches.put(USER_MANAGER, BUserManagerService.get());
        mCaches.put(DUMP_MANAGER, BDumpManagerService.get());
    }

    /**
     * 内部方法，根据名称从缓存中获取服务。
     *
     * @param name 服务名称
     * @return 服务的 IBinder，未找到返回 null
     */
    public IBinder getServiceInternal(String name) {
        return mCaches.get(name);
    }
}
