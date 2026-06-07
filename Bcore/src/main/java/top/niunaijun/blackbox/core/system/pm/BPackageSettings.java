package top.niunaijun.blackbox.core.system.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.AtomicFile;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.core.system.user.BUserHandle;
import top.niunaijun.blackbox.utils.CloseUtils;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * 虚拟环境中包的配置信息。
 * <p>
 * 该类实现了{@link Parcelable}接口，用于管理虚拟环境中一个应用包的配置信息，
 * 包括包对象、AppId、安装选项、以及每个用户的状态（安装/停止/隐藏）。
 * 支持通过{@link #save()}方法持久化到文件系统。
 * </p>
 *
 * @see BPackage
 * @see BPackageUserState
 */
public class BPackageSettings implements Parcelable {
    public BPackage pkg;
    public int appId;
    public InstallOption installOption;
    public Map<Integer, BPackageUserState> userState = new HashMap<>();
    static final BPackageUserState DEFAULT_USER_STATE = new BPackageUserState();

    public BPackageSettings() {
    }

    /**
     * 获取所有用户的状态列表。
     *
     * @return 用户状态列表
     */
    public List<BPackageUserState> getUserState() {
        return new ArrayList<>(userState.values());
    }

    /**
     * 获取所有已注册的用户ID列表。
     *
     * @return 用户ID列表
     */
    public List<Integer> getUserIds() {
        return new ArrayList<>(userState.keySet());
    }

    /**
     * 设置指定用户的安装状态。
     *
     * @param inst   是否已安装
     * @param userId 目标用户ID
     */
    public void setInstalled(boolean inst, int userId) {
        modifyUserState(userId).installed = inst;
    }

    /**
     * 获取指定用户的安装状态。
     *
     * @param userId 目标用户ID
     * @return 如果已安装返回true，否则返回false
     */
    public boolean getInstalled(int userId) {
        return readUserState(userId).installed;
    }

    /**
     * 获取指定用户的停止状态。
     *
     * @param userId 目标用户ID
     * @return 如果已停止返回true，否则返回false
     */
    public boolean getStopped(int userId) {
        return readUserState(userId).stopped;
    }

    /**
     * 设置指定用户的停止状态。
     *
     * @param stop   是否已停止
     * @param userId 目标用户ID
     */
    public void setStopped(boolean stop, int userId) {
        modifyUserState(userId).stopped = stop;
    }

    /**
     * 获取指定用户的隐藏状态。
     *
     * @param userId 目标用户ID
     * @return 如果已隐藏返回true，否则返回false
     */
    public boolean getHidden(int userId) {
        return readUserState(userId).hidden;
    }

    /**
     * 设置指定用户的隐藏状态。
     *
     * @param hidden 是否已隐藏
     * @param userId 目标用户ID
     */
    public void setHidden(boolean hidden, int userId) {
        modifyUserState(userId).hidden = hidden;
    }

    /**
     * 移除指定用户的配置信息。
     *
     * @param userId 目标用户ID
     */
    public void removeUser(int userId) {
        userState.remove(userId);
    }

    /**
     * 读取指定用户的用户状态（只读副本）。
     *
     * @param userId 目标用户ID
     * @return 用户状态对象
     */
    public BPackageUserState readUserState(int userId) {
        BPackageUserState state = userState.get(userId);
        if (state == null) {
            state = new BPackageUserState();
        }
        state = new BPackageUserState(state);

        if (userState.get(BUserHandle.USER_ALL) != null) {
            state.installed = true;
        }
        return state;
    }

    private BPackageUserState modifyUserState(int userId) {
        BPackageUserState state = userState.get(userId);
        if (state == null) {
            state = new BPackageUserState();
            userState.put(userId, state);
        }
        return state;
    }

    /**
     * 将当前配置信息持久化到文件系统。
     *
     * @return 保存成功返回true，失败返回false
     */
    public boolean save() {
        synchronized (this) {
            Parcel parcel = Parcel.obtain();
            AtomicFile atomicFile = new AtomicFile(BEnvironment.getPackageConf(pkg.packageName));
            FileOutputStream fileOutputStream = null;
            try {
                writeToParcel(parcel, 0);
                parcel.setDataPosition(0);
                fileOutputStream = atomicFile.startWrite();
                FileUtils.writeParcelToOutput(parcel, fileOutputStream);
                atomicFile.finishWrite(fileOutputStream);
                return true;
            } catch (Throwable e) {
                e.printStackTrace();
                atomicFile.failWrite(fileOutputStream);
                return false;
            } finally {
                parcel.recycle();
                CloseUtils.close(fileOutputStream);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.pkg, flags);
        dest.writeInt(this.appId);
        dest.writeParcelable(this.installOption, flags);
        dest.writeInt(this.userState.size());
        for (Map.Entry<Integer, BPackageUserState> entry : this.userState.entrySet()) {
            dest.writeValue(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
    }

    protected BPackageSettings(Parcel in) {
        this.pkg = in.readParcelable(BPackage.class.getClassLoader());
        this.appId = in.readInt();
        this.installOption = in.readParcelable(InstallOption.class.getClassLoader());
        int userStateSize = in.readInt();
        this.userState = new HashMap<Integer, BPackageUserState>(userStateSize);
        for (int i = 0; i < userStateSize; i++) {
            Integer key = (Integer) in.readValue(Integer.class.getClassLoader());
            BPackageUserState value = in.readParcelable(BPackageUserState.class.getClassLoader());
            this.userState.put(key, value);
        }
    }

    public static final Creator<BPackageSettings> CREATOR = new Creator<BPackageSettings>() {
        @Override
        public BPackageSettings createFromParcel(Parcel source) {
            return new BPackageSettings(source);
        }

        @Override
        public BPackageSettings[] newArray(int size) {
            return new BPackageSettings[size];
        }
    };
}
