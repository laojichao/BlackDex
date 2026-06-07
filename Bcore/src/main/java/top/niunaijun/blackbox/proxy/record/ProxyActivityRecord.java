package top.niunaijun.blackbox.proxy.record;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

import top.niunaijun.blackbox.utils.compat.BundleCompat;

/**
 * 代理Activity记录类。
 * <p>
 * 用于在代理Activity启动过程中保存真实的Activity信息，包括：
 * <ul>
 *   <li>用户ID（虚拟用户空间标识）</li>
 *   <li>真实的ActivityInfo</li>
 *   <li>目标Intent（指向真实Activity的Intent）</li>
 *   <li>原始ActivityRecord的IBinder引用</li>
 * </ul>
 * <p>
 * 通过Intent的Extra字段在代理Activity和真实Activity之间传递数据，
 * 使用"_VM_|_xxx_"作为Key前缀以避免与应用自定义Extra冲突。
 *
 * @author Milk
 */
public class ProxyActivityRecord {
    /** 虚拟用户ID */
    public int mUserId;
    /** 真实Activity的ActivityInfo */
    public ActivityInfo mActivityInfo;
    /** 指向真实Activity的目标Intent */
    public Intent mTarget;
    /** 原始ActivityRecord的IBinder引用，用于系统级Activity管理 */
    public IBinder mActivityRecord;

    /**
     * 构造代理Activity记录。
     *
     * @param userId        虚拟用户ID
     * @param activityInfo  真实Activity的ActivityInfo
     * @param target        指向真实Activity的目标Intent
     * @param activityRecord 原始ActivityRecord的IBinder引用
     */
    public ProxyActivityRecord(int userId, ActivityInfo activityInfo, Intent target, IBinder activityRecord) {
        mUserId = userId;
        mActivityInfo = activityInfo;
        mTarget = target;
        mActivityRecord = activityRecord;
    }

    /**
     * 将代理Activity的信息保存到Shadow Intent的Extra中。
     * <p>
     * 保存的信息包括用户ID、ActivityInfo、目标Intent和ActivityRecord的IBinder。
     * ActivityRecord通过 {@link BundleCompat#putBinder} 绕过Bundle不支持
     * 直接存储IBinder的限制。
     *
     * @param shadow         代理Activity的Intent（Shadow）
     * @param target         指向真实Activity的目标Intent
     * @param activityInfo   真实Activity的ActivityInfo
     * @param activityRecord 原始ActivityRecord的IBinder引用
     * @param userId         虚拟用户ID
     */
    public static void saveStub(Intent shadow, Intent target, ActivityInfo activityInfo, IBinder activityRecord, int userId) {
        shadow.putExtra("_VM_|_user_id_", userId);
        shadow.putExtra("_VM_|_activity_info_", activityInfo);
        shadow.putExtra("_VM_|_target_", target);
        BundleCompat.putBinder(shadow, "_VM_|_activity_record_v_", activityRecord);
    }

    /**
     * 从Intent中恢复代理Activity记录。
     * <p>
     * 解析Intent中保存的用户ID、ActivityInfo、目标Intent和ActivityRecord，
     * 构建并返回 {@link ProxyActivityRecord} 实例。
     *
     * @param intent 包含代理Activity数据的Intent
     * @return 恢复的ProxyActivityRecord实例
     */
    public static ProxyActivityRecord create(Intent intent) {
        int userId = intent.getIntExtra("_VM_|_user_id_", 0);
        ActivityInfo activityInfo = intent.getParcelableExtra("_VM_|_activity_info_");
        Intent target = intent.getParcelableExtra("_VM_|_target_");
        IBinder activityRecord = BundleCompat.getBinder(intent, "_VM_|_activity_record_v_");
        return new ProxyActivityRecord(userId, activityInfo, target, activityRecord);
    }
}
