package top.niunaijun.blackbox.fake.frameworks;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;

import java.util.Collections;
import java.util.List;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.entity.pm.InstallResult;
import top.niunaijun.blackbox.entity.pm.InstalledPackage;
import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.core.system.pm.IBPackageManagerService;

/**
 * 虚拟环境包管理器门面类。
 * <p>
 * 提供包信息查询、安装/卸载、组件解析等客户端接口，通过 Binder IPC 调用
 * {@link IBPackageManagerService} 服务端实现。是虚拟环境中替代系统 PackageManager
 * 的核心组件，所有操作都基于虚拟用户 ID 隔离。
 * </p>
 *
 * @author Milk
 * @see IBPackageManagerService
 */
public class BPackageManager {
    private static final BPackageManager sPackageManager = new BPackageManager();
    private IBPackageManagerService mService;

    /**
     * 获取 BPackageManager 单例实例。
     *
     * @return BPackageManager 全局唯一实例
     */
    public static BPackageManager get() {
        return sPackageManager;
    }

    /**
     * 获取指定包名的启动 Intent。
     * <p>
     * 先尝试查找 CATEGORY_INFO 类型的 Activity，未找到再尝试 CATEGORY_LAUNCHER。
     * </p>
     *
     * @param packageName 包名
     * @param userId      虚拟用户 ID
     * @return 启动 Intent，未找到时返回 null
     */
    public Intent getLaunchIntentForPackage(String packageName, int userId) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = queryIntentActivities(intentToResolve,
                0,
                intentToResolve.resolveTypeIfNeeded(BlackBoxCore.getContext().getContentResolver()),
                userId);

        // Otherwise, try to find a main launcher activity.
        if (ris == null || ris.size() <= 0) {
            // reuse the intent instance
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = queryIntentActivities(intentToResolve,
                    0,
                    intentToResolve.resolveTypeIfNeeded(BlackBoxCore.getContext().getContentResolver()),
                    userId);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(ris.get(0).activityInfo.packageName,
                ris.get(0).activityInfo.name);
        return intent;
    }

    /**
     * 解析 Service 的 Intent。
     *
     * @param intent        Intent 对象
     * @param flags         标志位
     * @param resolvedType  解析后的 MIME 类型
     * @param userId        虚拟用户 ID
     * @return 解析结果，失败时返回 null
     */
    public ResolveInfo resolveService(Intent intent, int flags, String resolvedType, int userId) {
        try {
            return getService().resolveService(intent, flags, resolvedType, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 解析 Activity 的 Intent。
     *
     * @param intent        Intent 对象
     * @param flags         标志位
     * @param resolvedType  解析后的 MIME 类型
     * @param userId        虚拟用户 ID
     * @return 解析结果，失败时返回 null
     */
    public ResolveInfo resolveActivity(Intent intent, int flags, String resolvedType, int userId) {
        try {
            return getService().resolveActivity(intent, flags, resolvedType, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 解析 ContentProvider。
     *
     * @param authority   Provider 的 authority 字符串
     * @param flags       标志位
     * @param userId      虚拟用户 ID
     * @return ProviderInfo，失败时返回 null
     */
    public ProviderInfo resolveContentProvider(String authority, int flags, int userId) {
        try {
            return getService().resolveContentProvider(authority, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 解析 Intent。
     *
     * @param intent        Intent 对象
     * @param resolvedType  解析后的 MIME 类型
     * @param flags         标志位
     * @param userId        虚拟用户 ID
     * @return 解析结果，失败时返回 null
     */
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
        try {
            return getService().resolveIntent(intent, resolvedType, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 获取应用信息。
     *
     * @param packageName 包名
     * @param flags       标志位
     * @param userId      虚拟用户 ID
     * @return ApplicationInfo，失败时返回 null
     */
    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        try {
            return getService().getApplicationInfo(packageName, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 获取包信息。
     *
     * @param packageName 包名
     * @param flags       标志位
     * @param userId      虚拟用户 ID
     * @return PackageInfo，失败时返回 null
     */
    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        try {
            return getService().getPackageInfo(packageName, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 获取 Service 信息。
     *
     * @param component 组件名
     * @param flags     标志位
     * @param userId    虚拟用户 ID
     * @return ServiceInfo，失败时返回 null
     */
    public ServiceInfo getServiceInfo(ComponentName component, int flags, int userId) {
        try {
            return getService().getServiceInfo(component, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 获取 BroadcastReceiver 信息。
     *
     * @param componentName 组件名
     * @param flags         标志位
     * @param userId        虚拟用户 ID
     * @return ActivityInfo，失败时返回 null
     */
    public ActivityInfo getReceiverInfo(ComponentName componentName, int flags, int userId) {
        try {
            return getService().getReceiverInfo(componentName, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 获取 Activity 信息。
     *
     * @param component 组件名
     * @param flags     标志位
     * @param userId    虚拟用户 ID
     * @return ActivityInfo，失败时返回 null
     */
    public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        try {
            return getService().getActivityInfo(component, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 获取 ContentProvider 信息。
     *
     * @param component 组件名
     * @param flags     标志位
     * @param userId    虚拟用户 ID
     * @return ProviderInfo，失败时返回 null
     */
    public ProviderInfo getProviderInfo(ComponentName component, int flags, int userId) {
        try {
            return getService().getProviderInfo(component, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 查询匹配 Intent 的 Activity 列表。
     */
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags, String resolvedType, int userId) {
        try {
            return getService().queryIntentActivities(intent, flags, resolvedType, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 查询匹配 Intent 的广播接收器列表。
     */
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, String resolvedType, int userId) {
        try {
            return getService().queryBroadcastReceivers(intent, flags, resolvedType, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 查询指定进程中的 ContentProvider 列表。
     */
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags, int userId) {
        try {
            return getService().queryContentProviders(processName, uid, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 安装指定 APK 到虚拟用户空间。
     *
     * @param file   APK 文件路径
     * @param option 安装选项配置
     * @param userId 虚拟用户 ID
     * @return 安装结果
     */
    public InstallResult installPackageAsUser(String file, InstallOption option, int userId) {
        try {
            return getService().installPackageAsUser(file, option, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * 获取虚拟用户已安装的应用信息列表。
     */
    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        try {
            return getService().getInstalledApplications(flags, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 获取虚拟用户已安装的包信息列表。
     */
    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        try {
            return getService().getInstalledPackages(flags, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 为指定虚拟用户卸载包。
     *
     * @param packageName 包名
     * @param userId      虚拟用户 ID
     */
    public void uninstallPackageAsUser(String packageName, int userId) {
        try {
            getService().uninstallPackageAsUser(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 卸载所有虚拟用户中的指定包。
     *
     * @param packageName 包名
     */
    public void uninstallPackage(String packageName) {
        try {
            getService().uninstallPackage(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查指定包是否在虚拟用户中已安装。
     *
     * @param packageName 包名
     * @param userId      虚拟用户 ID
     * @return 已安装返回 true
     */
    public boolean isInstalled(String packageName, int userId) {
        try {
            return getService().isInstalled(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取虚拟用户已安装包的详细列表（包含安装选项信息）。
     */
    public List<InstalledPackage> getInstalledPackagesAsUser(int userId) {
        try {
            return getService().getInstalledPackagesAsUser(userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void crash(Throwable e) {
        e.printStackTrace();
    }

    private IBPackageManagerService getService() {
        if (mService != null && mService.asBinder().isBinderAlive()) {
            return mService;
        }
        mService = IBPackageManagerService.Stub.asInterface(BlackBoxCore.get().getService(ServiceManager.PACKAGE_MANAGER));
        return getService();
    }
}
