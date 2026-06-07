package top.niunaijun.blackbox.entity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 虚拟 Service 记录实体类。
 * <p>
 * 管理虚拟环境中 Android Service 的绑定状态，包括：
 * <ul>
 *     <li>Service 实例持有</li>
 *     <li>基于 Intent 的 BoundInfo 绑定信息管理</li>
 *     <li>绑定计数器（支持多次 bind/unbind）</li>
 *     <li>Binder 死亡监听与自动清理</li>
 * </ul>
 *
 * @author Milk
 * @see ActiveServices
 */
public class ServiceRecord {
    private Service mService;
    private Map<Intent.FilterComparison, BoundInfo> mBounds = new HashMap<>();
    private boolean rebind;
    private int mStartId;

    /** 绑定信息内部类，持有 IBinder 引用和绑定计数 */
    public class BoundInfo {
        private IBinder mIBinder;
        private AtomicInteger mBindCount = new AtomicInteger(0);

        public int incrementAndGetBindCount() {
            return mBindCount.incrementAndGet();
        }

        public int decrementAndGetBindCount() {
            return mBindCount.decrementAndGet();
        }

        public IBinder getIBinder() {
            return mIBinder;
        }

        public void setIBinder(IBinder IBinder) {
            mIBinder = IBinder;
        }
    }

    public int getStartId() {
        return mStartId;
    }

    public void setStartId(int startId) {
        mStartId = startId;
    }

    public Service getService() {
        return mService;
    }

    public void setService(Service service) {
        mService = service;
    }

    public IBinder getBinder(Intent intent) {
        BoundInfo boundInfo = getOrCreateBoundInfo(intent);
        return boundInfo.getIBinder();
    }

    public boolean hasBinder(Intent intent) {
        BoundInfo boundInfo = getOrCreateBoundInfo(intent);
        return boundInfo.getIBinder() != null;
    }

    /**
     * 向指定 Intent 添加 Binder 并注册死亡监听。
     * <p>Binder 死亡时自动从绑定映射中移除，防止内存泄漏。</p>
     */
    public void addBinder(Intent intent, final IBinder iBinder) {
        final Intent.FilterComparison filterComparison = new Intent.FilterComparison(intent);
        BoundInfo boundInfo = getOrCreateBoundInfo(intent);
        if (boundInfo == null) {
            boundInfo = new BoundInfo();
            mBounds.put(filterComparison, boundInfo);
        }
        boundInfo.setIBinder(iBinder);
        try {
            iBinder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    iBinder.unlinkToDeath(this, 0);
                    mBounds.remove(filterComparison);
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int incrementAndGetBindCount(Intent intent) {
        BoundInfo boundInfo = getOrCreateBoundInfo(intent);
        return boundInfo.incrementAndGetBindCount();
    }

    public boolean decreaseConnectionCount(Intent intent) {
        Intent.FilterComparison filterComparison = new Intent.FilterComparison(intent);
        BoundInfo boundInfo = mBounds.get(filterComparison);
        if (boundInfo == null)
            return true;
        int i = boundInfo.decrementAndGetBindCount();
        if (i <= 0) {
//            mBounds.remove(filterComparison);
            return true;
        }
        return false;
    }

    public BoundInfo getOrCreateBoundInfo(Intent intent) {
        Intent.FilterComparison filterComparison = new Intent.FilterComparison(intent);
        BoundInfo boundInfo = mBounds.get(filterComparison);
        if (boundInfo == null) {
            boundInfo = new BoundInfo();
            mBounds.put(filterComparison, boundInfo);
        }
        return boundInfo;
    }

    public boolean isRebind() {
        return rebind;
    }

    public void setRebind(boolean rebind) {
        this.rebind = rebind;
    }
}
