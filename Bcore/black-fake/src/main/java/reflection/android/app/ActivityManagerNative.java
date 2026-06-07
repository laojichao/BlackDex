package reflection.android.app;


import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.ActivityManagerNative} 的反射包装器。
 * <p>
 * 用于通过反射访问 ActivityManager 的本地实现，适用于 Android 8.0 以下版本。
 * 提供 {@code gDefault} 静态字段和 {@code getDefault()} 静态方法的访问。
 */
public class ActivityManagerNative {
    /** 反射目标类 {@code android.app.ActivityManagerNative} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.app.ActivityManagerNative");

    /** 静态字段 {@code gDefault}，存储默认 ActivityManager 实例的 Singleton 对象 */
    public static MirrorReflection.FieldWrapper<Object> gDefault = REF.field("gDefault");
    /** 静态方法 {@code getDefault()}，获取默认的 IActivityManager 接口 */
    public static MirrorReflection.StaticMethodWrapper<IInterface> getDefault = REF.staticMethod("getDefault");
}
