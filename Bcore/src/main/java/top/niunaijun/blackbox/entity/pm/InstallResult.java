package top.niunaijun.blackbox.entity.pm;

import android.os.Parcel;
import android.os.Parcelable;

import top.niunaijun.blackbox.utils.Slog;

/**
 * 安装结果实体类（Parcelable）。
 * <p>
 * 封装虚拟环境中 APK 安装操作的结果信息，包含安装状态、包名和错误消息。
 *
 * @author Milk
 */
public class InstallResult implements Parcelable {
    /** 日志标签 */
    public static final String TAG = "InstallResult";

    /** 安装是否成功，默认 {@code true} */
    public boolean success = true;
    /** 安装后的包名 */
    public String packageName;
    /** 结果消息（成功或错误描述） */
    public String msg;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.success ? (byte) 1 : (byte) 0);
        dest.writeString(this.packageName);
        dest.writeString(this.msg);
    }

    public InstallResult() {
    }

    protected InstallResult(Parcel in) {
        this.success = in.readByte() != 0;
        this.packageName = in.readString();
        this.msg = in.readString();
    }

    /**
     * 标记安装失败。
     *
     * @param msg 错误消息
     * @return 当前实例（支持链式调用）
     */
    public InstallResult installError(String msg) {
        this.msg = msg;
        this.success = false;
        Slog.d(TAG, msg);
        return this;
    }

    public static final Parcelable.Creator<InstallResult> CREATOR = new Parcelable.Creator<InstallResult>() {
        @Override
        public InstallResult createFromParcel(Parcel source) {
            return new InstallResult(source);
        }

        @Override
        public InstallResult[] newArray(int size) {
            return new InstallResult[size];
        }
    };

    @Override
    public String toString() {
        return "InstallResult{" +
                "success=" + success +
                ", packageName='" + packageName + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
