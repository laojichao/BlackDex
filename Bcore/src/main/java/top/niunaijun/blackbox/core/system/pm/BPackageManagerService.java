package top.niunaijun.blackbox.core.system.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.RemoteException;
import android.text.TextUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.entity.pm.InstallResult;
import top.niunaijun.blackbox.entity.pm.InstalledPackage;
import top.niunaijun.blackbox.core.system.BProcessManager;
import top.niunaijun.blackbox.core.system.ISystemService;
import top.niunaijun.blackbox.core.system.user.BUserManagerService;
import top.niunaijun.blackbox.utils.AbiUtils;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.utils.Slog;
import top.niunaijun.blackbox.utils.compat.PackageParserCompat;

import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_UNAWARE;


/**
 * 虚拟环境中的包管理服务。
 * <p>
 * 该服务是BlackBox框架的核心包管理服务，提供与Android系统PackageManager类似的功能，
 * 包括应用安装/卸载、组件查询（Activity、Service、Provider、BroadcastReceiver）、
 * 包信息查询、Intent解析等。内部通过{@link ComponentResolver}管理所有已注册的组件，
 * 通过{@link Settings}持久化包配置信息。
 * </p>
 *
 * @see BPackage
 * @see BPackageSettings
 * @see ComponentResolver
 * @see Settings
 */
public class BPackageManagerService extends IBPackageManagerService.Stub implements ISystemService {
    public static final String TAG = "BPackageManagerService";
    public static BPackageManagerService sService = new BPackageManagerService();
    private final Settings mSettings = new Settings();
    private final ComponentResolver mComponentResolver;
    private static final BUserManagerService sUserManager = BUserManagerService.get();
    private final List<PackageMonitor> mPackageMonitors = new ArrayList<>();

    final Map<String, BPackageSettings> mPackages = mSettings.mPackages;
    final Object mInstallLock = new Object();

    /**
     * 获取BPackageManagerService单例。
     *
     * @return BPackageManagerService实例
     */
    public static BPackageManagerService get() {
        return sService;
    }

    public BPackageManagerService() {
        mComponentResolver = new ComponentResolver();
    }

    /**
     * 获取指定包的ApplicationInfo信息。
     *
     * @param packageName 包名
     * @param flags       查询标志
     * @param userId      目标用户ID
     * @return ApplicationInfo对象，未找到时返回null
     */
    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        if (Objects.equals(packageName, BlackBoxCore.getHostPkg())) {
            try {
                return BlackBoxCore.getPackageManager().getApplicationInfo(packageName, flags);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
        flags = updateFlags(flags, userId);
        // reader
        synchronized (mPackages) {
            // Normalize package name to handle renamed packages and static libs
            BPackageSettings ps = mPackages.get(packageName);
            if (ps != null) {
                BPackage p = ps.pkg;
                return PackageManagerCompat.generateApplicationInfo(p, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    /**
     * 解析Service对应的ResolveInfo。
     *
     * @param intent       查询Intent
     * @param flags        查询标志
     * @param resolvedType Intent的MIME类型
     * @param userId       目标用户ID
     * @return 匹配的ResolveInfo，未找到时返回null
     */
    @Override
    public ResolveInfo resolveService(Intent intent, int flags, String resolvedType, int userId) {
        if (!sUserManager.exists(userId)) return null;
        List<ResolveInfo> query = queryIntentServicesInternal(
                intent, resolvedType, flags, userId);
        if (query != null) {
            if (query.size() >= 1) {
                // If there is more than one service with the same priority,
                // just arbitrarily pick the first one.
                return query.get(0);
            }
        }
        return null;
    }

    private List<ResolveInfo> queryIntentServicesInternal(Intent intent, String resolvedType, int flags, int userId) {
        ComponentName comp = intent.getComponent();
        if (comp == null) {
            if (intent.getSelector() != null) {
                intent = intent.getSelector();
                comp = intent.getComponent();
            }
        }
        if (comp != null) {
            final List<ResolveInfo> list = new ArrayList<>(1);
            final ServiceInfo si = getServiceInfo(comp, flags, userId);
            if (si != null) {
                // When specifying an explicit component, we prevent the service from being
                // used when either 1) the service is in an instant application and the
                // caller is not the same instant application or 2) the calling package is
                // ephemeral and the activity is not visible to ephemeral applications.
                final ResolveInfo ri = new ResolveInfo();
                ri.serviceInfo = si;
                list.add(ri);
            }
            return list;
        }

        // reader
        synchronized (mPackages) {
            String pkgName = intent.getPackage();
            if (pkgName == null) {
                return null;
            }
            BPackageSettings bPackageSettings = mPackages.get(pkgName);
            if (bPackageSettings != null) {
                final BPackage pkg = bPackageSettings.pkg;
                return mComponentResolver.queryServices(intent, resolvedType, flags, pkg.services,
                        userId);
            }
            return Collections.emptyList();
        }
    }

    /**
     * 解析Activity对应的ResolveInfo。
     *
     * @param intent       查询Intent
     * @param flags        查询标志
     * @param resolvedType Intent的MIME类型
     * @param userId       目标用户ID
     * @return 最佳匹配的ResolveInfo，未找到时返回null
     */
    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags, String resolvedType, int userId) {
        if (!sUserManager.exists(userId)) return null;
        List<ResolveInfo> resolves = queryIntentActivities(intent, resolvedType, flags, userId);
        return chooseBestActivity(intent, resolvedType, flags, resolves);
    }

    /**
     * 解析ContentProvider对应的ProviderInfo。
     *
     * @param authority ContentProvider的authority
     * @param flags     查询标志
     * @param userId    目标用户ID
     * @return ProviderInfo对象，未找到时返回null
     */
    @Override
    public ProviderInfo resolveContentProvider(String authority, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        return mComponentResolver.queryProvider(authority, flags, userId);
    }

    /**
     * 解析Intent对应的ResolveInfo（通用方法）。
     *
     * @param intent       查询Intent
     * @param resolvedType Intent的MIME类型
     * @param flags        查询标志
     * @param userId       目标用户ID
     * @return 最佳匹配的ResolveInfo，未找到时返回null
     */
    @Override
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        List<ResolveInfo> resolves = queryIntentActivities(intent, resolvedType, flags, userId);
        return chooseBestActivity(intent, resolvedType, flags, resolves);
    }

    private ResolveInfo chooseBestActivity(Intent intent, String resolvedType,
                                           int flags, List<ResolveInfo> query) {
        if (query != null) {
            final int N = query.size();
            if (N == 1) {
                return query.get(0);
            } else if (N > 1) {
                // If there is more than one activity with the same priority,
                // then let the user decide between them.
                ResolveInfo r0 = query.get(0);
                ResolveInfo r1 = query.get(1);
                // If the first activity has a higher priority, or a different
                // default, then it is always desirable to pick it.
                if (r0.priority != r1.priority
                        || r0.preferredOrder != r1.preferredOrder
                        || r0.isDefault != r1.isDefault) {
                    return query.get(0);
                }
            }
        }
        return null;
    }

    private List<ResolveInfo> queryIntentActivities(Intent intent,
                                                    String resolvedType, int flags, int userId) {
        final String pkgName = intent.getPackage();
        ComponentName comp = intent.getComponent();
        if (comp == null) {
            if (intent.getSelector() != null) {
                intent = intent.getSelector();
                comp = intent.getComponent();
            }
        }

        if (comp != null) {
            final List<ResolveInfo> list = new ArrayList<>(1);
            final ActivityInfo ai = getActivity(comp, flags, userId);
            if (ai != null) {
                // When specifying an explicit component, we prevent the activity from being
                // used when either 1) the calling package is normal and the activity is within
                // an ephemeral application or 2) the calling package is ephemeral and the
                // activity is not visible to ephemeral applications.
                final ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                list.add(ri);
                return list;
            }
        }

        // reader
        synchronized (mPackages) {
            if (pkgName != null) {
                return mComponentResolver.queryActivities(intent, resolvedType, flags, userId);
            }
        }
        return Collections.emptyList();
    }


    private ActivityInfo getActivity(ComponentName component, int flags,
                                     int userId) {
        flags = updateFlags(flags, userId);
        synchronized (mPackages) {
            BPackage.Activity a = mComponentResolver.getActivity(component);

            if (a != null) {
                BPackageSettings ps = mPackages.get(component.getPackageName());
                if (ps == null) return null;
                return PackageManagerCompat.generateActivityInfo(a, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    /**
     * 获取指定包的PackageInfo信息。
     *
     * @param packageName 包名
     * @param flags       查询标志
     * @param userId      目标用户ID
     * @return PackageInfo对象，未找到时返回null
     */
    @Override
    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        if (Objects.equals(packageName, BlackBoxCore.getHostPkg())) {
            try {
                return BlackBoxCore.getPackageManager().getPackageInfo(packageName, flags);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        flags = updateFlags(flags, userId);
        // reader
        synchronized (mPackages) {
            // Normalize package name to handle renamed packages and static libs
            BPackageSettings ps = mPackages.get(packageName);
            if (ps != null) {
                return PackageManagerCompat.generatePackageInfo(ps, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    /**
     * 获取指定组件的ServiceInfo信息。
     *
     * @param component 组件名
     * @param flags     查询标志
     * @param userId    目标用户ID
     * @return ServiceInfo对象，未找到时返回null
     */
    @Override
    public ServiceInfo getServiceInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        synchronized (mPackages) {
            BPackage.Service s = mComponentResolver.getService(component);
            if (s != null) {
                BPackageSettings ps = mPackages.get(component.getPackageName());
                if (ps == null) return null;
                return PackageManagerCompat.generateServiceInfo(
                        s, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    /**
     * 获取指定组件的ReceiverInfo信息。
     *
     * @param component 组件名
     * @param flags     查询标志
     * @param userId    目标用户ID
     * @return ActivityInfo对象，未找到时返回null
     */
    @Override
    public ActivityInfo getReceiverInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        synchronized (mPackages) {
            BPackage.Activity a = mComponentResolver.getReceiver(component);
            if (a != null) {
                BPackageSettings ps = mPackages.get(component.getPackageName());
                if (ps == null) return null;
                return PackageManagerCompat.generateActivityInfo(
                        a, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    /**
     * 获取指定组件的ActivityInfo信息。
     *
     * @param component 组件名
     * @param flags     查询标志
     * @param userId    目标用户ID
     * @return ActivityInfo对象，未找到时返回null
     */
    @Override
    public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        synchronized (mPackages) {
            BPackage.Activity a = mComponentResolver.getActivity(component);

            if (a != null) {
                BPackageSettings ps = mPackages.get(component.getPackageName());
                if (ps == null) return null;
                return PackageManagerCompat.generateActivityInfo(
                        a, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    /**
     * 获取指定组件的ProviderInfo信息。
     *
     * @param component 组件名
     * @param flags     查询标志
     * @param userId    目标用户ID
     * @return ProviderInfo对象，未找到时返回null
     */
    @Override
    public ProviderInfo getProviderInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        synchronized (mPackages) {
            BPackage.Provider p = mComponentResolver.getProvider(component);
            if (p != null) {
                BPackageSettings ps = mPackages.get(component.getPackageName());
                if (ps == null) return null;
                return PackageManagerCompat.generateProviderInfo(
                        p, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    /**
     * 获取指定用户已安装的应用列表。
     *
     * @param flags  查询标志
     * @param userId 目标用户ID
     * @return ApplicationInfo列表
     */
    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        return getInstalledApplicationsListInternal(flags, userId, Binder.getCallingUid());
    }

    /**
     * 获取指定用户已安装的包列表。
     *
     * @param flags  查询标志
     * @param userId 目标用户ID
     * @return PackageInfo列表
     */
    @Override
    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        final int callingUid = Binder.getCallingUid();
//        if (getInstantAppPackageName(callingUid) != null) {
//            return ParceledListSlice.emptyList();
//        }
        if (!sUserManager.exists(userId)) return Collections.emptyList();

        // writer
        synchronized (mPackages) {
            ArrayList<PackageInfo> list;
            list = new ArrayList<>(mPackages.size());
            for (BPackageSettings ps : mPackages.values()) {
//                if (filterSharedLibPackageLPr(ps, callingUid, userId, flags)) {
//                    continue;
//                }
//                if (filterAppAccessLPr(ps, callingUid, userId)) {
//                    continue;
//                }
                final PackageInfo pi = getPackageInfo(ps.pkg.packageName, flags, userId);
                if (pi != null) {
                    list.add(pi);
                }
            }
            return new ArrayList<>(list);
        }
    }

    private List<ApplicationInfo> getInstalledApplicationsListInternal(int flags, int userId,
                                                                       int callingUid) {
        if (!sUserManager.exists(userId)) return Collections.emptyList();

        // writer
        synchronized (mPackages) {
            ArrayList<ApplicationInfo> list;
            list = new ArrayList<>(mPackages.size());
            Collection<BPackageSettings> packageSettings = mPackages.values();
            for (BPackageSettings ps : packageSettings) {
//                if (filterSharedLibPackageLPr(ps, Binder.getCallingUid(), userId, flags)) {
//                    continue;
//                }
//                if (filterAppAccessLPr(ps, callingUid, userId)) {
//                    continue;
//                }
                ApplicationInfo ai = PackageManagerCompat.generateApplicationInfo(ps.pkg, flags,
                        ps.readUserState(userId), userId);
                if (ai != null) {
                    list.add(ai);
                }
            }
            return list;
        }
    }

    /**
     * 查询匹配指定Intent的Activity列表。
     *
     * @param intent       查询Intent
     * @param flags        查询标志
     * @param resolvedType Intent的MIME类型
     * @param userId       目标用户ID
     * @return 匹配的ResolveInfo列表
     * @throws RemoteException IPC通信异常
     */
    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags, String resolvedType, int userId) throws RemoteException {
        if (!sUserManager.exists(userId)) return Collections.emptyList();
        final String pkgName = intent.getPackage();
        ComponentName comp = intent.getComponent();
        if (comp == null) {
            if (intent.getSelector() != null) {
                intent = intent.getSelector();
                comp = intent.getComponent();
            }
        }

        if (comp != null) {
            final List<ResolveInfo> list = new ArrayList<>(1);
            final ActivityInfo ai = getActivityInfo(comp, flags, userId);
            if (ai != null) {
                // When specifying an explicit component, we prevent the activity from being
                // used when either 1) the calling package is normal and the activity is within
                // an ephemeral application or 2) the calling package is ephemeral and the
                // activity is not visible to ephemeral applications.
                final ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                list.add(ri);
            }
            return list;
        }

        // reader
        List<ResolveInfo> result;
        synchronized (mPackages) {
            if (pkgName != null) {
                BPackageSettings bPackageSettings = mPackages.get(pkgName);
                result = null;
                if (bPackageSettings != null) {
                    final BPackage pkg = bPackageSettings.pkg;

                    result = mComponentResolver.queryActivities(
                            intent, resolvedType, flags, pkg.activities, userId);
                }
                if (result == null || result.size() == 0) {
                    // the caller wants to resolve for a particular package; however, there
                    // were no installed results, so, try to find an ephemeral result
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                }
                return result;
            }
        }
        return Collections.emptyList();
    }

    /**
     * 查询匹配指定Intent的BroadcastReceiver列表。
     *
     * @param intent       查询Intent
     * @param flags        查询标志
     * @param resolvedType Intent的MIME类型
     * @param userId       目标用户ID
     * @return 匹配的ResolveInfo列表
     * @throws RemoteException IPC通信异常
     */
    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, String resolvedType, int userId) throws RemoteException {
        if (!sUserManager.exists(userId)) return Collections.emptyList();

        ComponentName comp = intent.getComponent();
        if (comp == null) {
            if (intent.getSelector() != null) {
                intent = intent.getSelector();
                comp = intent.getComponent();
            }
        }
        if (comp != null) {
            final List<ResolveInfo> list = new ArrayList<>(1);
            final ActivityInfo ai = getReceiverInfo(comp, flags, userId);
            if (ai != null) {
                // When specifying an explicit component, we prevent the activity from being
                // used when either 1) the calling package is normal and the activity is within
                // an instant application or 2) the calling package is ephemeral and the
                // activity is not visible to instant applications.
                ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                list.add(ri);
            }
            return list;
        }

        // reader
        synchronized (mPackages) {
            String pkgName = intent.getPackage();
            BPackageSettings bPackageSettings = mPackages.get(pkgName);
            if (bPackageSettings != null) {
                final BPackage pkg = bPackageSettings.pkg;
                return mComponentResolver.queryReceivers(
                        intent, resolvedType, flags, pkg.receivers, userId);
            }
            return Collections.emptyList();
        }
    }

    /**
     * 查询指定进程名的ContentProvider列表。
     *
     * @param processName 进程名
     * @param uid         用户ID
     * @param flags       查询标志
     * @param userId      目标用户ID
     * @return ProviderInfo列表
     * @throws RemoteException IPC通信异常
     */
    @Override
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags, int userId) throws RemoteException {
        if (!sUserManager.exists(userId)) return Collections.emptyList();

        List<ProviderInfo> providers = new ArrayList<>();
        if (TextUtils.isEmpty(processName))
            return providers;
        providers.addAll(mComponentResolver.queryProviders(processName, null, flags, userId));
        return providers;
    }

    /**
     * 为指定用户安装APK包。
     *
     * @param file   APK文件路径或URI
     * @param option 安装选项
     * @param userId 目标用户ID
     * @return 安装结果
     */
    @Override
    public InstallResult installPackageAsUser(String file, InstallOption option, int userId) {
        synchronized (mInstallLock) {
            return installPackageAsUserLocked(file, option, userId);
        }
    }

    /**
     * 为指定用户卸载包。
     *
     * @param packageName 包名
     * @param userId      目标用户ID
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void uninstallPackageAsUser(String packageName, int userId) throws RemoteException {
        synchronized (mInstallLock) {
            synchronized (mPackages) {
                BPackageSettings ps = mPackages.get(packageName);
                if (ps == null)
                    return;
                if (!isInstalled(packageName, userId)) {
                    return;
                }
                boolean removeApp = ps.getUserState().size() <= 1;
                BProcessManager.get().killPackageAsUser(packageName, userId);
                int i = BPackageInstallerService.get().uninstallPackageAsUser(ps, removeApp, userId);
                if (i < 0) {
                    // todo
                }

                if (removeApp) {
                    mPackages.remove(packageName);
                    mComponentResolver.removeAllComponents(ps.pkg);
                    mSettings.scanPackage();
                } else {
                    ps.removeUser(userId);
                    ps.save();
                }
                onPackageUninstalled(packageName, userId);
            }
        }
    }

    /**
     * 卸载所有用户下的指定包。
     *
     * @param packageName 包名
     */
    @Override
    public void uninstallPackage(String packageName) {
        synchronized (mInstallLock) {
            synchronized (mPackages) {
                BPackageSettings ps = mPackages.get(packageName);
                if (ps == null)
                    return;
                BProcessManager.get().killAllByPackageName(packageName);
                for (Integer userId : ps.getUserIds()) {
                    int i = BPackageInstallerService.get().uninstallPackageAsUser(ps, true, userId);
                    if (i < 0) {
                        continue;
                    }
                    onPackageUninstalled(packageName, userId);
                }
                mPackages.remove(packageName);
                mComponentResolver.removeAllComponents(ps.pkg);
                mSettings.scanPackage();
            }
        }
    }

    /**
     * 删除指定用户及其所有已安装的包。
     *
     * @param userId 目标用户ID
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void deleteUser(int userId) throws RemoteException {
        synchronized (mPackages) {
            for (BPackageSettings ps : mPackages.values()) {
                uninstallPackageAsUser(ps.pkg.packageName, userId);
            }
        }
    }

    /**
     * 检查指定包是否为指定用户安装。
     *
     * @param packageName 包名
     * @param userId      目标用户ID
     * @return 如果已安装返回true，否则返回false
     */
    @Override
    public boolean isInstalled(String packageName, int userId) {
        if (!sUserManager.exists(userId)) return false;
        synchronized (mPackages) {
            BPackageSettings ps = mPackages.get(packageName);
            if (ps == null)
                return false;
            return ps.getInstalled(userId);
        }
    }

    /**
     * 获取指定用户已安装的包列表（简化信息）。
     *
     * @param userId 目标用户ID
     * @return InstalledPackage列表
     */
    @Override
    public List<InstalledPackage> getInstalledPackagesAsUser(int userId) {
        if (!sUserManager.exists(userId)) return Collections.emptyList();
        synchronized (mPackages) {
            List<InstalledPackage> installedPackages = new ArrayList<>();
            for (BPackageSettings ps : mPackages.values()) {
                if (ps.getInstalled(userId)) {
                    InstalledPackage installedPackage = new InstalledPackage();
                    installedPackage.userId = userId;
                    installedPackage.packageName = ps.pkg.packageName;
                    installedPackages.add(installedPackage);
                }
            }
            return installedPackages;
        }
    }

    private InstallResult installPackageAsUserLocked(String file, InstallOption option, int userId) {
        InstallResult result = new InstallResult();
        File apkFile = null;
        try {
            if (!sUserManager.exists(userId)) {
                sUserManager.createUser(userId);
            }
            if (option.isFlag(InstallOption.FLAG_URI_FILE)) {
                apkFile = new File(BEnvironment.getCacheDir(), UUID.randomUUID().toString() + ".apk");
                InputStream inputStream = BlackBoxCore.getContext().getContentResolver().openInputStream(Uri.parse(file));
                FileUtils.copyFile(inputStream, apkFile);
            } else {
                apkFile = new File(file);
            }

            boolean support = AbiUtils.isSupport(apkFile);
            if (!support) {
                return result.installError(BlackBoxCore.is64Bit() ? "not support armeabi-v7a abi" : "not support arm64-v8a abi");
            }

            PackageParser.Package aPackage = parserApk(apkFile.getAbsolutePath());
            if (aPackage == null) {
                return result.installError("parser apk error.");
            }
            result.packageName = aPackage.packageName;

            BPackageSettings bPackageSettings = mSettings.getPackageLPw(aPackage.packageName, aPackage);
            bPackageSettings.installOption = option;

            // stop pkg
            BProcessManager.get().killPackageAsUser(aPackage.packageName, userId);

            int i = BPackageInstallerService.get().installPackageAsUser(bPackageSettings, userId);
            if (i < 0) {
                return result.installError("install apk error.");
            }
            synchronized (mPackages) {
                bPackageSettings.setInstalled(true, userId);
                bPackageSettings.save();
            }
            mComponentResolver.addAllComponents(bPackageSettings.pkg);
            mSettings.scanPackage();
            onPackageInstalled(bPackageSettings.pkg.packageName, userId);
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (apkFile != null && option.isFlag(InstallOption.FLAG_URI_FILE)) {
                FileUtils.deleteDir(apkFile);
            }
        }
        return result;
    }

    private PackageParser.Package parserApk(String file) {
        try {
            PackageParser parser = PackageParserCompat.createParser(new File(file));
            PackageParser.Package aPackage = PackageParserCompat.parsePackage(parser, new File(file), 0);
            PackageParserCompat.collectCertificates(parser, aPackage, 0);
            return aPackage;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    static String fixProcessName(String defProcessName, String processName) {
        if (processName == null) {
            return defProcessName;
        }
        return processName;
    }

    /**
     * Update given flags based on encryption status of current user.
     */
    private int updateFlags(int flags, int userId) {
        if ((flags & (PackageManager.MATCH_DIRECT_BOOT_UNAWARE
                | PackageManager.MATCH_DIRECT_BOOT_AWARE)) != 0) {
            // Caller expressed an explicit opinion about what encryption
            // aware/unaware components they want to see, so fall through and
            // give them what they want
        } else {
            // Caller expressed no opinion, so match based on user state
            flags |= PackageManager.MATCH_DIRECT_BOOT_AWARE | MATCH_DIRECT_BOOT_UNAWARE;
        }
        return flags;
    }

    /**
     * 获取指定包的AppId。
     *
     * @param packageName 包名
     * @return AppId，未找到时返回-1
     */
    public int getAppId(String packageName) {
        BPackageSettings bPackageSettings = mPackages.get(packageName);
        if (bPackageSettings != null)
            return bPackageSettings.appId;
        return -1;
    }

    Settings getSettings() {
        return mSettings;
    }

    /**
     * 添加包监听器，用于监听包的安装和卸载事件。
     *
     * @param monitor 包监听器
     */
    public void addPackageMonitor(PackageMonitor monitor) {
        mPackageMonitors.add(monitor);
    }

    /**
     * 移除包监听器。
     *
     * @param monitor 包监听器
     */
    public void removePackageMonitor(PackageMonitor monitor) {
        mPackageMonitors.add(monitor);
    }

    void onPackageUninstalled(String packageName, int userId) {
        for (PackageMonitor packageMonitor : mPackageMonitors) {
            packageMonitor.onPackageUninstalled(packageName, userId);
        }
        Slog.d(TAG, "onPackageUninstalled: " + packageName + ", userId: " + userId);
    }

    void onPackageInstalled(String packageName, int userId) {
        for (PackageMonitor packageMonitor : mPackageMonitors) {
            packageMonitor.onPackageInstalled(packageName, userId);
        }
        Slog.d(TAG, "onPackageInstalled: " + packageName + ", userId: " + userId);
    }

    @Override
    public void systemReady() {
        mSettings.scanPackage();
        for (BPackageSettings value : mPackages.values()) {
            mComponentResolver.addAllComponents(value.pkg);
        }
    }
}