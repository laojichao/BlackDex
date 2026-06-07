package top.niunaijun.blackbox.entity.pm;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 安装选项实体类（Parcelable）。
 * <p>
 * 使用位标志定义 APK 安装方式：
 * <ul>
 *     <li>{@link #FLAG_SYSTEM} - 从系统已安装应用安装</li>
 *     <li>{@link #FLAG_STORAGE} - 从存储文件安装</li>
 *     <li>{@link #FLAG_URI_FILE} - 通过 URI 安装</li>
 * </ul>
 *
 * @author Milk
 */
public class InstallOption implements Parcelable {
    /** 从系统已安装应用安装 */
    public static final int FLAG_SYSTEM = 1;
    /** 从存储文件安装 */
    public static final int FLAG_STORAGE = 1 << 1;
    /** 通过 URI 安装 */
    public static final int FLAG_URI_FILE = 1 << 3;

    /** 安装标志位 */
    public int flags = 0;

    /**
     * 创建从系统已安装应用安装的选项。
     *
     * @return 安装选项实例
     */
    public static InstallOption installBySystem() {
        InstallOption installOption = new InstallOption();
        installOption.flags = installOption.flags | FLAG_SYSTEM;
        return installOption;
    }

    /**
     * 创建从存储文件安装的选项。
     *
     * @return 安装选项实例
     */
    public static InstallOption installByStorage() {
        InstallOption installOption = new InstallOption();
        installOption.flags = installOption.flags | FLAG_STORAGE;
        return installOption;
    }

    /**
     * 标记为 URI 文件安装方式。
     *
     * @return 当前实例（支持链式调用）
     */
    public InstallOption makeUriFile() {
        this.flags |= FLAG_URI_FILE;
        return this;
    }

    /**
     * 判断是否包含指定标志。
     *
     * @param flag 要检查的标志位
     * @return 包含该标志返回 {@code true}
     */
    public boolean isFlag(int flag) {
        return (flags & flag) != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.flags);
    }

    public InstallOption() {
    }

    protected InstallOption(Parcel in) {
        this.flags = in.readInt();
    }

    public static final Parcelable.Creator<InstallOption> CREATOR = new Parcelable.Creator<InstallOption>() {
        @Override
        public InstallOption createFromParcel(Parcel source) {
            return new InstallOption(source);
        }

        @Override
        public InstallOption[] newArray(int size) {
            return new InstallOption[size];
        }
    };
}
