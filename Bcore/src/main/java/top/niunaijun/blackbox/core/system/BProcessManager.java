package top.niunaijun.blackbox.core.system;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.entity.AppConfig;
import top.niunaijun.blackbox.proxy.ProxyManifest;
import top.niunaijun.blackbox.core.system.pm.BPackageManagerService;
import top.niunaijun.blackbox.core.system.user.BUserHandle;
import top.niunaijun.blackbox.utils.Slog;
import top.niunaijun.blackbox.utils.compat.ApplicationThreadCompat;
import top.niunaijun.blackbox.utils.compat.BundleCompat;
import top.niunaijun.blackbox.utils.provider.ProviderCall;
import top.niunaijun.blackbox.core.IBActivityThread;

/**
 * 虚拟进程管理器，负责管理 BlackBox 框架中所有虚拟应用进程的生命周期。
 * <p>
 * 模拟 Android 系统的进程管理机制，负责虚拟进程的创建、查找、销毁等操作。
 * 每个虚拟进程通过 {@link ProcessRecord} 进行记录，支持多用户环境下
 * 以包名+进程名+用户ID为索引的进程管理。
 * </p>
 *
 * @author Milk
 * @see ProcessRecord
 * @see ProxyManifest
 */
public class BProcessManager {
    public static final String TAG = "BProcessManager";

    public static BProcessManager sVProcessManager = new BProcessManager();
    private final Map<Integer, Map<String, ProcessRecord>> mProcessMap = new HashMap<>();
    private final List<ProcessRecord> mPidsSelfLocked = new ArrayList<>();
    private final Object mProcessLock = new Object();

    /**
     * 获取进程管理器单例实例。
     *
     * @return 进程管理器实例
     */
    public static BProcessManager get() {
        return sVProcessManager;
    }

    /**
     * 启动并锁定一个虚拟进程。如果该进程已存在且已初始化，则直接返回已有的进程记录。
     * <p>
     * 流程：检查进程是否已存在 -> 分配虚拟PID -> 创建进程记录 -> 初始化进程 -> 绑定客户端。
     * 如果初始化失败，会自动清理已创建的进程记录。
     * </p>
     *
     * @param packageName 应用包名
     * @param processName 进程名称
     * @param userId      虚拟用户ID
     * @param bpid        虚拟进程ID，传 -1 表示自动分配
     * @param callingUid  调用方的UID
     * @param callingPid  调用方的PID
     * @return 创建成功返回 {@link ProcessRecord}，失败返回 null
     */
    public ProcessRecord startProcessLocked(String packageName, String processName, int userId, int bpid, int callingUid, int callingPid) {
        ApplicationInfo info = BPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
        if (info == null)
            return null;
        ProcessRecord app;
        int buid = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
        Map<String, ProcessRecord> vProcess = mProcessMap.get(buid);

        if (vProcess == null) {
            vProcess = new HashMap<>();
        }
        synchronized (mProcessLock) {
            if (bpid == -1) {
                app = vProcess.get(processName);
                if (app != null) {
                    if (app.initLock != null) {
                        app.initLock.block();
                    }
                    if (app.bActivityThread != null) {
                        return app;
                    }
                }
                bpid = getUsingBPidL();
                Slog.d(TAG, "init bUid = " + buid + ", bPid = " + bpid);
            }
            if (bpid == -1) {
                throw new RuntimeException("No processes available");
            }
            app = new ProcessRecord(info, processName, 0, bpid, callingUid);
            app.uid = buid;
            app.buid = buid;
            app.userId = userId;
            app.baseBUid = BUserHandle.getAppId(info.uid);

            vProcess.put(processName, app);
            mPidsSelfLocked.add(app);

            mProcessMap.put(app.buid, vProcess);
            if (!initAppProcessL(app)) {
                //init process fail
                vProcess.remove(processName);
                mPidsSelfLocked.remove(app);
                app = null;
            } else {
                app.pid = getPid(BlackBoxCore.getContext(), ProxyManifest.getProcessName(app.bpid));
            }
        }
        return app;
    }

    /**
     * 获取一个未被占用的虚拟进程PID。
     *
     * @return 可用的虚拟PID，如果没有可用PID返回 -1
     */
    private int getUsingBPidL() {
        ActivityManager manager = (ActivityManager) BlackBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
        for (int i = 0; i < ProxyManifest.FREE_COUNT; i++) {
            boolean using = false;
            for (ProcessRecord processRecord : mPidsSelfLocked) {
                if (processRecord.bpid == i) {
                    using = true;
                    break;
                }
            }
            if (using)
                continue;
            return i;
        }
        return -1;
    }

    /**
     * 重启指定应用的虚拟进程。通过当前调用方的PID查找进程记录，
     * 如果找不到则解析出虚拟PID后重新启动进程。
     *
     * @param packageName 应用包名
     * @param processName 进程名称
     * @param userId      虚拟用户ID
     */
    public void restartAppProcess(String packageName, String processName, int userId) {
        synchronized (mProcessLock) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            ProcessRecord app;
            synchronized (mProcessLock) {
                app = findProcessByPid(callingPid);
            }
            if (app == null) {
                String stubProcessName = getProcessName(BlackBoxCore.getContext(), callingPid);
                int bpid = parseBPid(stubProcessName);
                startProcessLocked(packageName, processName, userId, bpid, callingUid, callingPid);
            }
        }
    }

    private int parseBPid(String stubProcessName) {
        String prefix;
        if (stubProcessName == null) {
            return -1;
        } else {
            prefix = BlackBoxCore.getHostPkg() + ":p";
        }
        if (stubProcessName.startsWith(prefix)) {
            try {
                return Integer.parseInt(stubProcessName.substring(prefix.length()));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return -1;
    }

    private boolean initAppProcessL(ProcessRecord record) {
        Log.d(TAG, "initProcess: " + record.processName);
        AppConfig appConfig = record.getClientConfig();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConfig.KEY, appConfig);
        Bundle init = ProviderCall.callSafely(record.getProviderAuthority(), "_Black_|_init_process_", null, bundle);
        IBinder appThread = BundleCompat.getBinder(init, "_Black_|_client_");
        if (appThread == null || !appThread.isBinderAlive()) {
            return false;
        }
        attachClientL(record, appThread);
        return true;
    }

    private void attachClientL(final ProcessRecord app, final IBinder appThread) {
        IBActivityThread activityThread = IBActivityThread.Stub.asInterface(appThread);
        if (activityThread == null) {
            app.kill();
            return;
        }
        try {
            appThread.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    Log.d(TAG, "App Died: " + app.processName);
                    appThread.unlinkToDeath(this, 0);
                    onProcessDie(app);
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        app.bActivityThread = activityThread;
        try {
            app.appThread = ApplicationThreadCompat.asInterface(activityThread.getActivityThread());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        app.initLock.open();
    }

    /**
     * 当虚拟进程死亡时的回调处理，清理进程相关的所有记录。
     *
     * @param record 死亡的进程记录
     */
    public void onProcessDie(ProcessRecord record) {
        synchronized (mProcessLock) {
            record.kill();
            Map<String, ProcessRecord> remove = mProcessMap.remove(record.buid);
            if (remove != null)
                remove.remove(record.processName);
            mPidsSelfLocked.remove(record);
        }
    }

    /**
     * 根据包名、进程名和用户ID查找虚拟进程记录。
     *
     * @param packageName 应用包名
     * @param processName 进程名称
     * @param userId      虚拟用户ID
     * @return 找到的进程记录，未找到返回 null
     */
    public ProcessRecord findProcessRecord(String packageName, String processName, int userId) {
        synchronized (mProcessLock) {
            int appId = BPackageManagerService.get().getAppId(packageName);
            int buid = BUserHandle.getUid(userId, appId);
            Map<String, ProcessRecord> processRecordMap = mProcessMap.get(buid);
            if (processRecordMap == null)
                return null;
            return processRecordMap.get(processName);
        }
    }

    /**
     * 杀死指定包名对应的所有虚拟进程（所有用户空间）。
     *
     * @param packageName 要杀死的应用包名
     */
    public void killAllByPackageName(String packageName) {
        synchronized (mProcessLock) {
            synchronized (mPidsSelfLocked) {
                List<ProcessRecord> tmp = new ArrayList<>(mPidsSelfLocked);
                int appId = BPackageManagerService.get().getAppId(packageName);
                for (ProcessRecord processRecord : mPidsSelfLocked) {
                    int appId1 = BUserHandle.getAppId(processRecord.buid);
                    if (appId == appId1) {
                        mProcessMap.remove(processRecord.buid);
                        tmp.remove(processRecord);
                        processRecord.kill();
                    }
                }
                mPidsSelfLocked.clear();
                mPidsSelfLocked.addAll(tmp);
            }
        }
    }

    /**
     * 杀死指定包名在指定用户空间下的所有虚拟进程。
     *
     * @param packageName 应用包名
     * @param userId      虚拟用户ID
     */
    public void killPackageAsUser(String packageName, int userId) {
        synchronized (mProcessLock) {
            int buid = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
            Map<String, ProcessRecord> process = mProcessMap.get(buid);
            if (process == null)
                return;
            for (ProcessRecord value : process.values()) {
                value.kill();
            }
            mProcessMap.remove(buid);
        }
    }


    /**
     * 根据调用方PID获取对应的虚拟用户ID。
     *
     * @param callingPid 调用方的PID
     * @return 用户ID，如果找不到对应进程则返回 0
     */
    public int getUserIdByCallingPid(int callingPid) {
        synchronized (mProcessLock) {
            ProcessRecord callingProcess = BProcessManager.get().findProcessByPid(callingPid);
            if (callingProcess == null) {
                return 0;
            }
            return callingProcess.userId;
        }
    }

    /**
     * 根据真实PID查找虚拟进程记录。
     *
     * @param pid 真实进程PID
     * @return 找到的进程记录，未找到返回 null
     */
    public ProcessRecord findProcessByPid(int pid) {
        synchronized (mPidsSelfLocked) {
            for (ProcessRecord processRecord : mPidsSelfLocked) {
                if (processRecord.pid == pid)
                    return processRecord;
            }
            return null;
        }
    }

    private static String getProcessName(Context context, int pid) {
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == pid) {
                processName = info.processName;
                break;
            }
        }
        if (processName == null) {
            throw new RuntimeException("processName = null");
        }
        return processName;
    }

    /**
     * 根据进程名获取真实PID。
     *
     * @param context     上下文
     * @param processName 进程名称
     * @return 进程PID，未找到返回 -1
     */
    public static int getPid(Context context, String processName) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                if (runningAppProcess.processName.equals(processName)) {
                    return runningAppProcess.pid;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }
}
