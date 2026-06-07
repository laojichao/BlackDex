package top.niunaijun.blackbox.core.system.pm;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 虚拟环境中包的用户状态信息。
 * <p>
 * 该类实现了{@link Parcelable}接口，用于记录一个应用包在特定用户下的状态，
 * 包括是否已安装、是否已停止、是否隐藏。
 * </p>
 *
 * @see BPackageSettings
 */
public class BPackageUserState implements Parcelable {
    public boolean installed;
    public boolean stopped;
    public boolean hidden;

    /**
     * 创建默认的用户状态（未安装、已停止、未隐藏）。
     */
    public BPackageUserState() {
        this.installed = false;
        this.stopped = true;
        this.hidden = false;
    }

    /**
     * 创建已安装的用户状态。
     *
     * @return 已安装状态的BPackageUserState实例
     */
    public static BPackageUserState create() {
        BPackageUserState state = new BPackageUserState();
        state.installed = true;
        return state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.installed ? (byte) 1 : (byte) 0);
        dest.writeByte(this.stopped ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hidden ? (byte) 1 : (byte) 0);
    }

    protected BPackageUserState(Parcel in) {
        this.installed = in.readByte() != 0;
        this.stopped = in.readByte() != 0;
        this.hidden = in.readByte() != 0;
    }

    /**
     * 从另一个BPackageUserState对象拷贝构造。
     *
     * @param state 要拷贝的状态对象
     */
    public BPackageUserState(BPackageUserState state) {
        this.installed = state.installed;
        this.stopped = state.stopped;
        this.hidden = state.hidden;
    }

    public static final Parcelable.Creator<BPackageUserState> CREATOR = new Parcelable.Creator<BPackageUserState>() {
        @Override
        public BPackageUserState createFromParcel(Parcel source) {
            return new BPackageUserState(source);
        }

        @Override
        public BPackageUserState[] newArray(int size) {
            return new BPackageUserState[size];
        }
    };
}
