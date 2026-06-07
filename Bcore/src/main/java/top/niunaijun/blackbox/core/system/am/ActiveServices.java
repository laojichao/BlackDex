package top.niunaijun.blackbox.core.system.am;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import top.niunaijun.blackbox.core.system.pm.BPackageManagerService;

/**
 * 虚拟服务管理器，模拟 Android 系统的 ActiveServices，负责管理虚拟应用中
 * 运行中的 Service 组件的生命周期。
 * <p>
 * 跟踪服务的启动次数（startId）和绑定连接数（bindCount），
 * 并通过 {@link RunningServiceRecord} 和 {@link ConnectedServiceRecord}
 * 维护服务的运行状态。
 * </p>
 *
 * @author Milk
 */
public class ActiveServices {
    public static final String TAG = "ActiveServices";

    private final Map<Intent.FilterComparison, RunningServiceRecord> mRunningServiceRecords = new HashMap<>();
    private final Map<IBinder, ConnectedServiceRecord> mConnectedServices = new HashMap<>();

    /**
     * 启动一个虚拟Service。当前为空实现。
     *
     * @param intent        启动Intent
     * @param resolvedType  Intent的MIME类型
     * @param userId        虚拟用户ID
     */
    public void startService(Intent intent, String resolvedType, int userId) {

    }

    /**
     * 停止一个虚拟Service。如果服务仍有绑定连接则不执行停止。
     *
     * @param intent        停止Intent
     * @param resolvedType  Intent的MIME类型
     * @param userId        虚拟用户ID
     * @return 始终返回 0
     */
    public int stopService(Intent intent, String resolvedType, int userId) {
//        ResolveInfo resolveInfo = resolveService(intent, resolvedType, userId);
        synchronized (mRunningServiceRecords) {
            RunningServiceRecord runningServiceRecord = findRunningServiceRecord(intent);
            if (runningServiceRecord == null) {
                return 0;
            }
            if (runningServiceRecord.mBindCount.get() > 0) {
                Log.d(TAG, "There are also connections");
                return 0;
            }

            runningServiceRecord.mStartId.set(0);
        }
        return 0;
    }

    /**
     * 解绑一个已绑定的虚拟Service，减少其绑定计数。
     *
     * @param binder 绑定时返回的 IBinder 对象
     * @param userId 虚拟用户ID
     */
    public void unbindService(IBinder binder, int userId) {
        ConnectedServiceRecord connectedService = mConnectedServices.get(binder);
        if (connectedService == null) {
            return;
        }
        RunningServiceRecord runningServiceRecord = getOrCreateRunningServiceRecord(connectedService.mIntent);
        runningServiceRecord.mConnectedServiceRecord = null;
        runningServiceRecord.mBindCount.decrementAndGet();
        mConnectedServices.remove(binder);
    }

    /**
     * Service 的 onStartCommand 回调。当前为空实现。
     *
     * @param proxyIntent 代理Intent
     * @param userId      虚拟用户ID
     */
    public void onStartCommand(Intent proxyIntent, int userId) {
    }

    /**
     * Service 销毁回调。当前为空实现。
     *
     * @param proxyIntent 代理Intent
     * @param userId      虚拟用户ID
     */
    public void onServiceDestroy(Intent proxyIntent, int userId) {
    }

    private RunningServiceRecord getOrCreateRunningServiceRecord(Intent intent) {
        RunningServiceRecord runningServiceRecord = findRunningServiceRecord(intent);
        if (runningServiceRecord == null) {
            runningServiceRecord = new RunningServiceRecord();
            mRunningServiceRecords.put(new Intent.FilterComparison(intent), runningServiceRecord);
        }
        return runningServiceRecord;
    }

    private RunningServiceRecord findRunningServiceRecord(Intent intent) {
        return mRunningServiceRecords.get(new Intent.FilterComparison(intent));
    }

    private ResolveInfo resolveService(Intent intent, String resolvedType, int userId) {
        return BPackageManagerService.get().resolveService(intent, 0, resolvedType, userId);
    }

    private ConnectedServiceRecord findConnectedServiceRecord(Intent intent) {
        RunningServiceRecord runningServiceRecord = mRunningServiceRecords.get(intent);
        if (runningServiceRecord == null)
            return null;
        return runningServiceRecord.mConnectedServiceRecord;
    }

    /**
     * 运行中服务的记录，维护服务的启动计数和绑定计数。
     */
    public static class RunningServiceRecord {
        // onStartCommand startId
        private AtomicInteger mStartId = new AtomicInteger(1);
        private AtomicInteger mBindCount = new AtomicInteger(0);
        // 正在连接的服务
        private ConnectedServiceRecord mConnectedServiceRecord;

        /**
         * 获取并自增 startId。
         *
         * @return 当前 startId（自增前的值）
         */
        public int getAndIncrementStartId() {
            return mStartId.getAndIncrement();
        }

        /**
         * 递减绑定计数并返回结果。
         *
         * @return 递减后的绑定计数
         */
        public int decrementBindCountAndGet() {
            return mBindCount.decrementAndGet();
        }

        /**
         * 递增绑定计数并返回结果。
         *
         * @return 递增后的绑定计数
         */
        public int incrementBindCountAndGet() {
            return mBindCount.incrementAndGet();
        }
    }

    /**
     * 已连接服务的记录，维护与 Service 绑定的 IBinder 和对应的 Intent。
     */
    public static class ConnectedServiceRecord {
        private IBinder mIBinder;
        private Intent mIntent;
    }
}
