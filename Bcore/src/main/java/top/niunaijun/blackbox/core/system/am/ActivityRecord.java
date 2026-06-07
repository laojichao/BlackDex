package top.niunaijun.blackbox.core.system.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Binder;
import android.os.IBinder;

import top.niunaijun.blackbox.core.system.ProcessRecord;


/**
 * 虚拟环境中的Activity记录。
 * <p>
 * 该类继承自{@link Binder}，用于标识和管理虚拟环境中每个Activity实例的状态，
 * 包括所属任务、Intent信息、ActivityInfo、组件名、进程信息等。
 * 通过{@link #create}工厂方法创建实例。
 * </p>
 *
 * @see ActivityStack
 * @see TaskRecord
 */
public class ActivityRecord extends Binder {
    public TaskRecord task;
    public IBinder token;
    public IBinder resultTo;
    public ActivityInfo info;
    public ComponentName component;
    public Intent intent;
    public int userId;
    public boolean finished;
    public ProcessRecord processRecord;

    /**
     * 创建一个新的ActivityRecord实例。
     *
     * @param intent   启动Activity的Intent
     * @param info     Activity的ActivityInfo信息
     * @param resultTo 发起方Activity的IBinder token
     * @param userId   目标用户ID
     * @return 新创建的ActivityRecord对象
     */
    public static ActivityRecord create(Intent intent, ActivityInfo info, IBinder resultTo, int userId) {
        ActivityRecord record = new ActivityRecord();
        record.intent = intent;
        record.info = info;
        record.component = new ComponentName(info.packageName, info.name);
        record.resultTo = resultTo;
        record.userId = userId;
        return record;
    }


}
