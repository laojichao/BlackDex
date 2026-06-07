package top.niunaijun.blackbox.entity;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * 虚拟应用配置信息实体类（Parcelable）。
 * <p>
 * 存储虚拟应用进程的完整配置信息，包括包名、进程名、PID/UID 映射等，
 * 通过 Intent 在宿主进程与虚拟进程之间传递。
 *
 * @author Milk
 */
public class AppConfig implements Parcelable {
    /** Intent Extra 的键名 */
    public static final String KEY = "BlackBox_client_config";

    /** 目标应用包名 */
    public String packageName;
    /** 目标应用进程名 */
    public String processName;
    /** 虚拟进程 PID */
    public int bpid;
    /** 虚拟进程 UID */
    public int buid;
    /** 真实进程 UID */
    public int uid;
    /** 虚拟用户 ID */
    public int userId;
    /** 基础虚拟 UID */
    public int baseBUid;
    /** 进程间通信 Binder Token */
    public IBinder token;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.processName);
        dest.writeInt(this.bpid);
        dest.writeInt(this.buid);
        dest.writeInt(this.uid);
        dest.writeInt(this.userId);
        dest.writeInt(this.baseBUid);
        dest.writeStrongBinder(token);
    }

    public AppConfig() {
    }

    protected AppConfig(Parcel in) {
        this.packageName = in.readString();
        this.processName = in.readString();
        this.bpid = in.readInt();
        this.buid = in.readInt();
        this.uid = in.readInt();
        this.userId = in.readInt();
        this.baseBUid = in.readInt();
        this.token = in.readStrongBinder();
    }

    public static final Parcelable.Creator<AppConfig> CREATOR = new Parcelable.Creator<AppConfig>() {
        @Override
        public AppConfig createFromParcel(Parcel source) {
            return new AppConfig(source);
        }

        @Override
        public AppConfig[] newArray(int size) {
            return new AppConfig[size];
        }
    };
}
