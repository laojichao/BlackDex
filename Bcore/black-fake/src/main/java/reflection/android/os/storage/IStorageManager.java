package reflection.android.os.storage;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.os.storage.IStorageManager} 的反射包装器。
 * <p>
 * IStorageManager 是 Android 10+ 引入的存储管理服务 Binder 接口，
 * 替代了旧版的 {@code IMountService}。
 * 通过 {@code IStorageManager$Stub.asInterface()} 获取服务代理。
 */
public class IStorageManager {
    /**
     * IStorageManager 的 Stub 内部类反射包装器，提供 {@code asInterface()} 方法。
     */
    public static class Stub {
        public static final MirrorReflection REF = MirrorReflection.on("android.os.storage.IStorageManager$Stub");

        public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}