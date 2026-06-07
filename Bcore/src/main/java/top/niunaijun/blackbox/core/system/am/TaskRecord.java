package top.niunaijun.blackbox.core.system.am;

import android.content.Intent;

import java.util.LinkedList;
import java.util.List;

/**
 * 虚拟环境中的任务记录。
 * <p>
 * 该类代表一个Activity任务栈，维护任务ID、用户ID、任务亲和性（taskAffinity）以及
 * 该任务下的所有Activity记录列表。用于Activity栈管理和启动模式的判断。
 * </p>
 *
 * @see ActivityRecord
 * @see ActivityStack
 */
public class TaskRecord {
    public int id;
    public int userId;
    public String taskAffinity;
    public Intent rootIntent;
    public final List<ActivityRecord> activities = new LinkedList<>();

    /**
     * 创建任务记录。
     *
     * @param id          任务ID
     * @param userId      用户ID
     * @param taskAffinity 任务亲和性字符串
     */
    public TaskRecord(int id, int userId, String taskAffinity) {
        this.id = id;
        this.userId = userId;
        this.taskAffinity = taskAffinity;
    }

    /**
     * 判断是否需要新建任务。当所有Activity都已结束时返回true。
     *
     * @return 如果所有Activity都已结束返回true，否则返回false
     */
    public boolean needNewTask() {
        for (ActivityRecord activity : activities) {
            if (!activity.finished) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将Activity添加到栈顶。
     *
     * @param record 要添加的ActivityRecord
     */
    public void addTopActivity(ActivityRecord record) {
        activities.add(record);
    }

    /**
     * 从任务栈中移除指定的Activity。
     *
     * @param record 要移除的ActivityRecord
     */
    public void removeActivity(ActivityRecord record) {
        activities.remove(record);
    }

    /**
     * 获取栈顶未结束的Activity记录。
     *
     * @return 栈顶未结束的ActivityRecord，如果所有Activity都已结束则返回null
     */
    public ActivityRecord getTopActivityRecord() {
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityRecord activityRecord = activities.get(i);
            if (!activityRecord.finished) {
                return activityRecord;
            }
        }
        return null;
    }
}
