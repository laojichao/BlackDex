package top.niunaijun.blackbox.entity.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.system.pm.BPackageSettings;

/**
 * 已安装包信息实体类（Parcelable）。
 * <p>
 * 存储虚拟环境中已安装应用的包名和用户 ID，
 * 支持通过包管理器查询完整的 ApplicationInfo 和 PackageInfo。
 *
 * @author Milk
 * @see BPackageManagerService
 */
public class InstalledPackage implements Parcelable {
    /** 用户 ID */
    public int userId;
    /** 包名 */
    public String packageName;

    /** @return 该包的 ApplicationInfo */
    public ApplicationInfo getApplication() {
        return BlackBoxCore.getBPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA, userId);
    }

    /** @return 该包的 PackageInfo */
    public PackageInfo getPackageInfo() {
        return BlackBoxCore.getBPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA, userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeString(this.packageName);
    }

    public InstalledPackage() {
    }

    public InstalledPackage(String packageName) {
        this.packageName = packageName;
    }

    protected InstalledPackage(Parcel in) {
        this.userId = in.readInt();
        this.packageName = in.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstalledPackage that = (InstalledPackage) o;
        return Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName);
    }

    public static final Parcelable.Creator<InstalledPackage> CREATOR = new Parcelable.Creator<InstalledPackage>() {
        @Override
        public InstalledPackage createFromParcel(Parcel source) {
            return new InstalledPackage(source);
        }

        @Override
        public InstalledPackage[] newArray(int size) {
            return new InstalledPackage[size];
        }
    };
}
