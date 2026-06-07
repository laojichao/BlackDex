package reflection.android.app;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.ApplicationThreadNative} 的反射包装器。
 * <p>
 * ApplicationThreadNative 是 IApplicationThread 的 Binder 实现基类。
 * 提供 {@code asInterface(IBinder)} 方法，将 IBinder 转换为 IApplicationThread 代理。
 * 适用于 Android 8.0 以下版本。
 */
public class ApplicationThreadNative {
    /** 反射目标类 {@code android.app.ApplicationThreadNative} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.app.ApplicationThreadNative");

    /** 静态方法 {@code asInterface(IBinder)}，将 IBinder 转换为 IApplicationThread 接口 */
    public static MirrorReflection.MethodWrapper<IInterface> asInterface = REF.method("asInterface", IBinder.class);
}
