package reflection.android.util;


import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.util.Singleton} 的反射包装器。
 * <p>
 * Singleton 是 Android 框架内部使用的单例模式工具类。
 * 提供 {@code get()} 方法和 {@code mInstance} 字段的反射访问，
 * 用于获取系统服务的单例实例（如 IActivityManager、IAlarmManager 等）。
 */
public class Singleton {
    /** 反射目标类 {@code android.util.Singleton} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.util.Singleton");
    /** 方法 {@code get()}，获取单例实例 */
    public static MirrorReflection.MethodWrapper<Object> get = REF.method("get");
    /** 字段 {@code mInstance}，单例的实际实例引用 */
    public static MirrorReflection.FieldWrapper<Object> mInstance = REF.field("mInstance");
}
