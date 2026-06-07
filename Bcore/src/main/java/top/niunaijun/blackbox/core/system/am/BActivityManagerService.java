package top.niunaijun.blackbox.core.system.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.entity.AppConfig;
import top.niunaijun.blackbox.core.system.ISystemService;
import top.niunaijun.blackbox.core.system.pm.BPackageManagerService;
import top.niunaijun.blackbox.core.system.ProcessRecord;
import top.niunaijun.blackbox.core.system.BProcessManager;

import static android.content.pm.PackageManager.GET_META_DATA;

/**
 * 虚拟Activity管理服务，BlackBox框架的核心服务之一。
 * <p>
 * 该服务负责管理虚拟环境中的Activity、Service和Broadcast生命周期，
 * 通过IBinder IPC接口对外提供服务。内部维护每个用户空间（{@link UserSpace}）的独立状态，
 * 包括Activity栈和活跃服务。
 * </p>
 *
 * @see UserSpace
 * @see ActivityStack
 * @see ActiveServices
 */
public class BActivityManagerService extends IBActivityManagerService.Stub implements ISystemService {
    public static final String TAG = "VActivityManagerService";
    private static final BActivityManagerService sService = new BActivityManagerService();
    private final Map<Integer, UserSpace> mUserSpace = new HashMap<>();

    /**
     * 获取BActivityManagerService单例。
     *
     * @return BActivityManagerService实例
     */
    public static BActivityManagerService get() {
        return sService;
    }

    /**
     * 启动虚拟环境中的Service。
     *
     * @param intent        启动Service的Intent
     * @param resolvedType  Intent的MIME类型
     * @param userId        目标用户ID
     * @return 始终返回null
     */
    @Override
    public ComponentName startService(Intent intent, String resolvedType, int userId) {
        UserSpace userSpace = getOrCreateSpaceLocked(userId);
        synchronized (userSpace.mActiveServices) {
            userSpace.mActiveServices.startService(intent, resolvedType, userId);
        }
        return null;
    }

    /**
     * 发送虚拟环境中的广播。
     *
     * @param intent        广播Intent
     * @param resolvedType  Intent的MIME类型
     * @param userId        目标用户ID
     * @return 处理后的Intent（可能已修改包名和组件信息）
     * @throws RemoteException IPC通信异常
     */
    @Override
    public Intent sendBroadcast(Intent intent, String resolvedType, int userId) throws RemoteException {
        List<ResolveInfo> resolves = BPackageManagerService.get().queryBroadcastReceivers(intent, GET_META_DATA, resolvedType, userId);

        for (ResolveInfo resolve : resolves) {
            ProcessRecord processRecord = BProcessManager.get().startProcessLocked(resolve.activityInfo.packageName, resolve.activityInfo.processName, userId, -1, Binder.getCallingUid(), Binder.getCallingPid());
            if (processRecord == null) {
//                throw new RuntimeException("Unable to create process " + resolve.activityInfo.name);
                continue;
            }
            processRecord.bActivityThread.bindApplication();
        }

        if (intent.getPackage() != null) {
            intent.setPackage(BlackBoxCore.getHostPkg());
        }
        if (intent.getComponent() != null) {
            intent.setComponent(null);
//            Intent shadow = new Intent();
//            shadow.setPackage(VirtualCore.getHostPkg());
//            shadow.setAction(StubManifest.getStubReceiver());
//            StubBroadcastRecord.saveStub(shadow, intent, receivers, userId);
        }
        return intent;
    }

    /**
     * Activity创建完成时的回调。
     *
     * @param taskId         任务ID
     * @param token          Activity的IBinder token
     * @param activityRecord ActivityRecord对象
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void onActivityCreated(int taskId, IBinder token, IBinder activityRecord) throws RemoteException {
        int callingPid = Binder.getCallingPid();
        ProcessRecord process = BProcessManager.get().findProcessByPid(callingPid);
        if (process == null) {
            return;
        }
        ActivityRecord record = (ActivityRecord) activityRecord;
        UserSpace userSpace = getOrCreateSpaceLocked(process.userId);
        synchronized (userSpace.mStack) {
            userSpace.mStack.onActivityCreated(process, taskId, token, record);
        }
    }

    /**
     * Activity恢复前台时的回调。
     *
     * @param token Activity的IBinder token
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void onActivityResumed(IBinder token) throws RemoteException {
        int callingPid = Binder.getCallingPid();
        ProcessRecord process = BProcessManager.get().findProcessByPid(callingPid);
        if (process == null) {
            return;
        }
        UserSpace userSpace = getOrCreateSpaceLocked(process.userId);
        synchronized (userSpace.mStack) {
            userSpace.mStack.onActivityResumed(process.userId, token);
        }
    }

    /**
     * Activity销毁时的回调。
     *
     * @param token Activity的IBinder token
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void onActivityDestroyed(IBinder token) throws RemoteException {
        int callingPid = Binder.getCallingPid();
        ProcessRecord process = BProcessManager.get().findProcessByPid(callingPid);
        if (process == null) {
            return;
        }
        UserSpace userSpace = getOrCreateSpaceLocked(process.userId);
        synchronized (userSpace.mStack) {
            userSpace.mStack.onActivityDestroyed(process.userId, token);
        }
    }

    /**
     * 结束Activity时的回调。
     *
     * @param token Activity的IBinder token
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void onFinishActivity(IBinder token) throws RemoteException {
        int callingPid = Binder.getCallingPid();
        ProcessRecord process = BProcessManager.get().findProcessByPid(callingPid);
        if (process == null) {
            return;
        }
        UserSpace userSpace = getOrCreateSpaceLocked(process.userId);
        synchronized (userSpace.mStack) {
            userSpace.mStack.onFinishActivity(process.userId, token);
        }
    }

    /**
     * Service启动命令回调。
     *
     * @param intent  启动Service的Intent
     * @param userId  目标用户ID
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void onStartCommand(Intent intent, int userId) throws RemoteException {
        UserSpace userSpace = getOrCreateSpaceLocked(userId);
        synchronized (userSpace.mActiveServices) {
            userSpace.mActiveServices.onStartCommand(intent, userId);
        }
    }

    /**
     * Service销毁时的回调。
     *
     * @param proxyIntent 代理Service的Intent
     * @param userId      目标用户ID
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void onServiceDestroy(Intent proxyIntent, int userId) throws RemoteException {
        UserSpace userSpace = getOrCreateSpaceLocked(userId);
        synchronized (userSpace.mActiveServices) {
            userSpace.mActiveServices.onServiceDestroy(proxyIntent, userId);
        }
    }

    /**
     * 停止虚拟环境中的Service。
     *
     * @param intent        停止Service的Intent
     * @param resolvedType  Intent的MIME类型
     * @param userId        目标用户ID
     * @return 操作结果
     */
    @Override
    public int stopService(Intent intent, String resolvedType, int userId) {
        UserSpace userSpace = getOrCreateSpaceLocked(userId);
        synchronized (userSpace.mActiveServices) {
            return userSpace.mActiveServices.stopService(intent, resolvedType, userId);
        }
    }

    /**
     * 绑定虚拟环境中的Service。
     *
     * @param service       Service的Intent
     * @param binder        用于回调的IBinder对象
     * @param resolvedType  Intent的MIME类型
     * @param userId        目标用户ID
     * @return 始终返回null
     * @throws RemoteException IPC通信异常
     */
    @Override
    public Intent bindService(Intent service, IBinder binder, String resolvedType, int userId) throws RemoteException {
        UserSpace userSpace = getOrCreateSpaceLocked(userId);
        synchronized (userSpace.mActiveServices) {
            return null;
        }
    }

    /**
     * 解绑虚拟环境中的Service。
     *
     * @param binder 用于回调的IBinder对象
     * @param userId 目标用户ID
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void unbindService(IBinder binder, int userId) throws RemoteException {
        UserSpace userSpace = getOrCreateSpaceLocked(userId);
        synchronized (userSpace.mActiveServices) {
            userSpace.mActiveServices.unbindService(binder, userId);
        }
    }

    /**
     * 初始化虚拟环境中的应用进程。
     *
     * @param packageName 包名
     * @param processName 进程名
     * @param userId      目标用户ID
     * @return 应用配置信息，进程启动失败时返回null
     * @throws RemoteException IPC通信异常
     */
    @Override
    public AppConfig initProcess(String packageName, String processName, int userId) throws RemoteException {
        ProcessRecord processRecord = BProcessManager.get().startProcessLocked(packageName, processName, userId, -1, Binder.getCallingUid(), Binder.getCallingPid());
        if (processRecord == null)
            return null;
        return processRecord.getClientConfig();
    }

    /**
     * 重启虚拟环境中的应用进程。
     *
     * @param packageName 包名
     * @param processName 进程名
     * @param userId      目标用户ID
     * @throws RemoteException IPC通信异常
     */
    @Override
    public void restartProcess(String packageName, String processName, int userId) throws RemoteException {
        BProcessManager.get().restartAppProcess(packageName, processName, userId);
    }

    /**
     * 启动虚拟环境中的Activity。
     *
     * @param intent 启动Activity的Intent
     * @param userId 目标用户ID
     */
    @Override
    public void startActivity(Intent intent, int userId) {
        UserSpace userSpace = getOrCreateSpaceLocked(userId);
        synchronized (userSpace.mStack) {
            userSpace.mStack.startActivityLocked(userId, intent, null, null, null, -1, -1, null);
        }
    }

    /**
     * 通过AMS方式启动虚拟环境中的Activity。
     *
     * @param userId       目标用户ID
     * @param intent       启动Activity的Intent
     * @param resolvedType Intent的MIME类型
     * @param resultTo     发起方Activity的token
     * @param resultWho    发起方标识
     * @param requestCode  请求码
     * @param flags        启动标志位
     * @param options      启动选项
     * @return 操作结果码
     * @throws RemoteException IPC通信异常
     */
    @Override
    public int startActivityAms(int userId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, Bundle options) throws RemoteException {
        UserSpace space = getOrCreateSpaceLocked(userId);
        synchronized (space.mStack) {
            return space.mStack.startActivityLocked(userId, intent, resolvedType, resultTo, resultWho, requestCode, flags, options);
        }
    }

    /**
     * 批量启动虚拟环境中的Activity。
     *
     * @param userId       目标用户ID
     * @param intent       Activity的Intent数组
     * @param resolvedType Intent的MIME类型数组
     * @param resultTo     发起方Activity的token
     * @param options      启动选项
     * @return 操作结果码
     * @throws RemoteException IPC通信异常
     */
    @Override
    public int startActivities(int userId, Intent[] intent, String[] resolvedType, IBinder resultTo, Bundle options) throws RemoteException {
        UserSpace space = getOrCreateSpaceLocked(userId);
        synchronized (space.mStack) {
            return space.mStack.startActivitiesLocked(userId, intent, resolvedType, resultTo, options);
        }
    }

    /**
     * 获取或创建指定用户ID的用户空间。
     *
     * @param userId 目标用户ID
     * @return 用户空间对象
     */
    private UserSpace getOrCreateSpaceLocked(int userId) {
        synchronized (mUserSpace) {
            UserSpace userSpace = mUserSpace.get(userId);
            if (userSpace != null)
                return userSpace;
            userSpace = new UserSpace();
            mUserSpace.put(userId, userSpace);
            return userSpace;
        }
    }

    @Override
    public void systemReady() {

    }
}
