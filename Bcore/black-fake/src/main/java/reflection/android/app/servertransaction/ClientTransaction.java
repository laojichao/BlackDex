package reflection.android.app.servertransaction;

import java.util.List;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.servertransaction.ClientTransaction} 的反射包装器。
 * <p>
 * ClientTransaction 是 Android 9.0 (Pie) 引入的事务机制，用于在 AMS 和客户端之间
 * 传递 Activity 生命周期事务。该类通过反射访问 {@code mActivityCallbacks} 列表字段，
 * 以提取 Activity 启动意图等信息。
 */
public class ClientTransaction {
    /** 反射目标类 {@code android.app.servertransaction.ClientTransaction} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.app.servertransaction.ClientTransaction");

    /** 字段 {@code mActivityCallbacks}，Activity 回调列表 */
    public static MirrorReflection.FieldWrapper<List<Object>> mActivityCallbacks = REF.field("mActivityCallbacks");
}
