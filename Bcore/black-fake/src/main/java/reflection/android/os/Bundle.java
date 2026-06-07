package reflection.android.os;

import android.os.IBinder;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.os.Bundle} 的反射包装器（Android 4.0+ 版本）。
 * <p>
 * 通过反射访问 Bundle 中隐藏的 {@code putIBinder()} 和 {@code getIBinder()} 方法，
 * 用于在 Bundle 中存取 IBinder 对象（系统级 API）。
 */
public class Bundle {
    /** 反射目标类 {@code android.os.Bundle} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(android.os.Bundle.class);

    /** 方法 {@code putIBinder(String, IBinder)}，向 Bundle 中放入 IBinder 对象 */
    public static MirrorReflection.MethodWrapper<Void> putIBinder = REF.method("putIBinder", String.class, IBinder.class);

    /** 方法 {@code getIBinder(String)}，从 Bundle 中获取 IBinder 对象 */
    public static MirrorReflection.MethodWrapper<IBinder> getIBinder = REF.method("getIBinder", String.class);
}
