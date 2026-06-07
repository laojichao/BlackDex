package reflection.android.os;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.os.IDeviceIdentifiersPolicyService} 的反射包装器。
 * <p>
 * IDeviceIdentifiersPolicyService 是 Android 8.0 (Oreo) 引入的设备标识符策略服务接口，
 * 用于控制设备序列号、IMEI 等标识符的访问权限。
 */
public class IDeviceIdentifiersPolicyService {
    /**
     * IDeviceIdentifiersPolicyService 的 Stub 内部类反射包装器，
     * 提供 {@code asInterface()} 方法。
     */
    public static class Stub {
        public static final MirrorReflection REF = MirrorReflection.on("android.os.IDeviceIdentifiersPolicyService$Stub");
        public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
