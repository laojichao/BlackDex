package top.niunaijun.blackbox.entity.dump;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * DEX Dump 结果实体类（Parcelable）。
 * <p>
 * 封装 DEX 脱壳操作的结果信息，支持三种状态：运行中、成功、失败。
 * 通过 Intent 在进程间传递，用于通知 UI 层 Dump 进度和结果。
 *
 * @author Milk
 */
public class DumpResult implements Parcelable {
    /** 日志标签 */
    public static final String TAG = "DumpResult";
    private static final int STATUS_RUNNING = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAIL = 2;

    /** 目标应用包名 */
    public String packageName;
    /** 结果消息（成功或失败描述） */
    public String msg;
    /** DEX 文件输出目录 */
    public String dir;

    private int status = STATUS_RUNNING;
    /** 总计需要处理的 DEX 数量 */
    public int totalProcess;
    /** 当前已处理的 DEX 数量 */
    public int currProcess;

    /**
     * 标记 Dump 失败。
     *
     * @param msg 错误消息
     * @return 当前实例（支持链式调用）
     */
    public DumpResult dumpError(String msg) {
        this.msg = msg;
        this.status = STATUS_FAIL;
        return this;
    }

    /**
     * 更新 Dump 进度。
     *
     * @param totalProcess 总计 DEX 数量
     * @param currProcess  当前已处理数量
     * @return 当前实例（支持链式调用）
     */
    public DumpResult dumpProcess(int totalProcess, int currProcess) {
        this.totalProcess = totalProcess;
        this.currProcess = currProcess;
        this.status = STATUS_RUNNING;
        return this;
    }

    /** 标记 Dump 成功。@return 当前实例（支持链式调用） */
    public DumpResult dumpSuccess() {
        this.status = STATUS_SUCCESS;
        return this;
    }

    /** @return 是否成功 */
    public boolean isSuccess() {
        return status == STATUS_SUCCESS;
    }

    /** @return 是否失败 */
    public boolean isFail() {
        return status == STATUS_FAIL;
    }

    /** @return 是否正在运行 */
    public boolean isRunning() {
        return status == STATUS_RUNNING;
    }

    @Override
    public String toString() {
        return "DumpResult{" +
                "packageName='" + packageName + '\'' +
                ", msg='" + msg + '\'' +
                ", dir='" + dir + '\'' +
                ", status=" + status +
                ", totalProcess=" + totalProcess +
                ", currProcess=" + currProcess +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.msg);
        dest.writeString(this.dir);
        dest.writeInt(this.status);
        dest.writeInt(this.totalProcess);
        dest.writeInt(this.currProcess);
    }

    public void readFromParcel(Parcel source) {
        this.packageName = source.readString();
        this.msg = source.readString();
        this.dir = source.readString();
        this.status = source.readInt();
        this.totalProcess = source.readInt();
        this.currProcess = source.readInt();
    }

    public DumpResult() {
    }

    protected DumpResult(Parcel in) {
        this.packageName = in.readString();
        this.msg = in.readString();
        this.dir = in.readString();
        this.status = in.readInt();
        this.totalProcess = in.readInt();
        this.currProcess = in.readInt();
    }

    public static final Creator<DumpResult> CREATOR = new Creator<DumpResult>() {
        @Override
        public DumpResult createFromParcel(Parcel source) {
            return new DumpResult(source);
        }

        @Override
        public DumpResult[] newArray(int size) {
            return new DumpResult[size];
        }
    };
}
