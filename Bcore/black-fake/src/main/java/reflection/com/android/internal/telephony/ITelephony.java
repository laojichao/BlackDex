package reflection.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code com.android.internal.telephony.ITelephony} 的反射包装器。
 * <p>
 * ITelephony 是电话服务的 Binder 接口，提供电话状态查询、呼叫控制等隐藏功能。
 * 通过 {@code ITelephony$Stub.asInterface()} 获取服务代理。
 */
public class ITelephony {
    /**
     * ITelephony 的 Stub 内部类反射包装器，提供 {@code asInterface()} 方法。
     */
    public static class Stub {
        public static final MirrorReflection REF = MirrorReflection.on("com.android.internal.telephony.ITelephony$Stub");

        public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
