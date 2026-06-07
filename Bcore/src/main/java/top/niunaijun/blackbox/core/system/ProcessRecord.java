package top.niunaijun.blackbox.core.system;

import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.ConditionVariable;

import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.text.TextUtils;

import java.util.Arrays;

import top.niunaijun.blackbox.entity.AppConfig;
import top.niunaijun.blackbox.core.IBActivityThread;
import top.niunaijun.blackbox.proxy.ProxyManifest;

/**
 * 虚拟进程记录，描述一个运行中的虚拟应用进程的完整状态信息。
 * <p>
 * 包含进程的PID、UID、用户ID等核心标识，以及与宿主进程通信所需的
 * {@link IBActivityThread} 和 {@link IInterface} 引用。
 * 同时实现了 {@link Parcelable} 接口以支持跨进程传输。
 * </p>
 *
 * @author Milk
 * @see BProcessManager
 */
public class ProcessRecord extends Binder implements Parcelable {
    /** 应用的 {@link ApplicationInfo} 信息 */
    public final ApplicationInfo info;
    /** 进程名称 */
    final public String processName;
    /** 虚拟应用线程的Binder接口，用于与宿主进程通信 */
    public IBActivityThread bActivityThread;
    /** 应用线程接口，对应真实进程的 ApplicationThread */
    public IInterface appThread;
    /** 真实进程PID */
    public int pid;
    /** 真实进程UID */
    public int uid;
    /** 虚拟UID，由 userId 和 appId 组合计算 */
    public int buid;
    /** 虚拟进程ID，用于标识代理进程 */
    public int bpid;
    /** 调用方的虚拟UID */
    public int callingVUid;
    /** 虚拟用户ID */
    public int userId;
    /** 基础应用虚拟UID */
    public int baseBUid;

    /** 进程初始化锁，用于同步等待进程初始化完成 */
    public ConditionVariable initLock = new ConditionVariable();

    /**
     * 创建一个新的进程记录。
     *
     * @param info        应用信息
     * @param processName 进程名称
     * @param buid        虚拟UID
     * @param bpid        虚拟进程ID
     * @param callingVUid 调用方的虚拟UID
     */
    public ProcessRecord(ApplicationInfo info, String processName, int buid, int bpid, int callingVUid) {
        this.info = info;
        this.buid = buid;
        this.bpid = bpid;
        this.userId = 0;
        this.callingVUid = callingVUid;
        this.processName = processName;
    }

    /**
     * 获取调用方的虚拟UID。
     *
     * @return 调用方虚拟UID
     */
    public int getCallingBUid() {
        return callingVUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessRecord that = (ProcessRecord) o;
        return pid == that.pid &&
                buid == that.buid &&
                bpid == that.bpid &&
                uid == that.uid &&
                userId == that.userId &&
                baseBUid == that.baseBUid &&
                TextUtils.equals(processName, that.processName);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{processName, pid, buid, bpid, uid, pid, userId});
    }

    /**
     * 获取该进程对应的代理 ContentProvider 的 authority。
     *
     * @return 代理Provider的authority字符串
     */
    public String getProviderAuthority() {
        return ProxyManifest.getProxyAuthorities(bpid);
    }

    /**
     * 获取客户端配置，用于传递给虚拟进程的初始化参数。
     *
     * @return 包含进程完整配置信息的 {@link AppConfig} 对象
     */
    public AppConfig getClientConfig() {
        AppConfig config = new AppConfig();
        config.packageName = info.packageName;
        config.processName = processName;
        config.bpid = bpid;
        config.buid = buid;
        config.uid = uid;
        config.userId = userId;
        config.token = this;
        config.baseBUid = baseBUid;
        return config;
    }

    /**
     * 杀死该进程对应的真实进程。
     */
    public void kill() {
        if (pid > 0) {
            try {
                Process.killProcess(pid);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取应用包名。
     *
     * @return 应用包名
     */
    public String getPackageName() {
        return info.packageName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.info, flags);
        dest.writeString(this.processName);
        dest.writeInt(this.pid);
        dest.writeInt(this.buid);
        dest.writeInt(this.bpid);
        dest.writeInt(this.uid);
        dest.writeInt(this.callingVUid);
        dest.writeInt(this.userId);
        dest.writeInt(this.baseBUid);
    }

    protected ProcessRecord(Parcel in) {
        this.info = in.readParcelable(ApplicationInfo.class.getClassLoader());
        this.processName = in.readString();
        this.pid = in.readInt();
        this.buid = in.readInt();
        this.bpid = in.readInt();
        this.uid = in.readInt();
        this.callingVUid = in.readInt();
        this.userId = in.readInt();
        this.baseBUid = in.readInt();
    }

    public static final Creator<ProcessRecord> CREATOR = new Creator<ProcessRecord>() {
        @Override
        public ProcessRecord createFromParcel(Parcel source) {
            return new ProcessRecord(source);
        }

        @Override
        public ProcessRecord[] newArray(int size) {
            return new ProcessRecord[size];
        }
    };
}
