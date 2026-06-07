package reflection.android.app;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.IAlarmManager} 的反射包装器。
 * <p>
 * IAlarmManager 是 AlarmManager 服务的 Binder 接口。
 * 通过 {@code IAlarmManager$Stub.asInterface()} 获取服务代理。
 */
public class IAlarmManager {
    /**
     * IAlarmManager 的 Stub 内部类反射包装器，提供 {@code asInterface()} 方法。
     */
    public static class Stub {
        public static final MirrorReflection REF = MirrorReflection.on("android.app.IAlarmManager$Stub");
        public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
