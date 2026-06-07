package reflection.android.app;

import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.ActivityManager} 的反射包装器（Android 8.0+ 版本）。
 * <p>
 * 在 Android 8.0 (Oreo) 及以上版本中，{@code ActivityManagerNative} 被弃用，
 * 改为通过 {@code ActivityManager.getService()} 获取 IActivityManager。
 * 该类提供 {@code getService()} 实例方法和 {@code IActivityManagerSingleton} 静态字段的访问。
 */
public class ActivityManagerOreo {
    /** 反射目标类 {@code android.app.ActivityManager} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.app.ActivityManager");

    /** 实例方法 {@code getService()}，获取 IActivityManager 服务接口 */
    public static MirrorReflection.MethodWrapper<IInterface> getService = REF.method("getService");
    /** 静态字段 {@code IActivityManagerSingleton}，存储 IActivityManager 的 Singleton */
    public static MirrorReflection.FieldWrapper<Object> IActivityManagerSingleton = REF.field("IActivityManagerSingleton");
}
