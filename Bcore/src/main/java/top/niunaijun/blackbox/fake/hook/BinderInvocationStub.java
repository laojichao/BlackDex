package top.niunaijun.blackbox.fake.hook;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.util.Map;

import reflection.android.os.ServiceManager;

/**
 * 基于 Binder 的服务代理桩基类。
 * <p>
 * 继承 {@link ClassInvocationStub} 并实现 {@link IBinder} 接口，用于代理系统服务的
 * Binder 对象。通过替换 ServiceManager 中缓存的 IBinder 引用，使所有对该服务的
 * IPC 调用都经过本代理。
 * </p>
 * <p>
 * IBinder 接口的默认实现委托给原始 Binder 对象（{@link #mBaseBinder}），
 * 子类通过 {@link #replaceSystemService(String)} 方法完成服务替换。
 * </p>
 *
 * @author Milk
 * @see ClassInvocationStub
 * @see ServiceManager#sCache
 */
public abstract class BinderInvocationStub extends ClassInvocationStub implements IBinder {
    /** 被代理的原始 IBinder 对象 */
    private IBinder mBaseBinder;

    /**
     * 构造方法，传入原始 Binder 对象。
     *
     * @param baseBinder 被代理的原始 IBinder
     */
    public BinderInvocationStub(IBinder baseBinder) {
        mBaseBinder = baseBinder;
    }

    @Override
    protected void onBindMethod() {
    }

    @Nullable
    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return mBaseBinder.getInterfaceDescriptor();
    }

    @Override
    public boolean pingBinder() {
        return mBaseBinder.pingBinder();
    }

    @Override
    public boolean isBinderAlive() {
        return mBaseBinder.isBinderAlive();
    }

    @Nullable
    @Override
    public IInterface queryLocalInterface(@NonNull String descriptor) {
        return (IInterface) getProxyInvocation();
    }

    @Override
    public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        mBaseBinder.dump(fd, args);
    }

    @Override
    public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        mBaseBinder.dumpAsync(fd, args);
    }

    @Override
    public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        return mBaseBinder.transact(code, data, reply, flags);
    }

    @Override
    public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
        mBaseBinder.linkToDeath(recipient, flags);
    }

    @Override
    public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
        return mBaseBinder.unlinkToDeath(recipient, flags);
    }


    /**
     * 替换 ServiceManager 缓存中的系统服务。
     * <p>
     * 将本代理实例放入 ServiceManager.sCache 中对应 name 的位置，
     * 使后续对该服务名称的 getService 调用返回本代理。
     * </p>
     *
     * @param name 系统服务名称（如 "package"、"alarm"、"mount" 等）
     */
    protected void replaceSystemService(String name) {
        Map<String, IBinder> services = ServiceManager.sCache.get();
        services.put(name, this);
    }
}
