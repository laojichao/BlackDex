package reflection.android.app;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.IApplicationThread$Stub} 的反射包装器（Android 8.0+ 版本）。
 * <p>
 * IApplicationThread 是应用进程与 AMS 之间的回调接口。
 * 在 Android 8.0 及以上版本中，不再使用 ApplicationThreadNative，
 * 而是直接通过 {@code IApplicationThread$Stub.asInterface()} 获取代理。
 */
public class IApplicationThreadOreo {
    /**
     * IApplicationThread 的 Stub 内部类反射包装器，提供 {@code asInterface()} 方法。
     */
    public static final class Stub {
        public static final MirrorReflection REF = MirrorReflection.on("android.app.IApplicationThread$Stub");
        public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
