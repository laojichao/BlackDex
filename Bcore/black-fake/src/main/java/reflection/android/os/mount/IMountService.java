package reflection.android.os.mount;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.os.storage.IMountService} 的反射包装器。
 * <p>
 * IMountService 是 Android 旧版本（Android 9.0 以下）的存储挂载服务 Binder 接口。
 * 在 Android 10+ 中已被 {@code IStorageManager} 替代。
 * 通过 {@code IMountService$Stub.asInterface()} 获取服务代理。
 */
public class IMountService {
    /**
     * IMountService 的 Stub 内部类反射包装器，提供 {@code asInterface()} 方法。
     */
    public static class Stub {
        public static final MirrorReflection REF = MirrorReflection.on("android.os.storage.IMountService$Stub");
        public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
