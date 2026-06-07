package reflection.android.app.servertransaction;

import android.content.Intent;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.servertransaction.LaunchActivityItem} 的反射包装器。
 * <p>
 * LaunchActivityItem 是 ClientTransaction 的回调项之一，表示启动 Activity 的事务。
 * 通过反射访问 {@code mIntent} 字段以获取启动 Activity 的 Intent。
 */
public class LaunchActivityItem {
    /** 反射目标类 {@code android.app.servertransaction.LaunchActivityItem} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.app.servertransaction.LaunchActivityItem");

    /** 字段 {@code mIntent}，启动 Activity 的 Intent */
    public static MirrorReflection.FieldWrapper<Intent> mIntent = REF.field("mIntent");
}
