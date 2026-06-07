package reflection.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code com.android.internal.telephony.ITelephonyRegistry} 的反射包装器。
 * <p>
 * ITelephonyRegistry 是电话状态注册服务的 Binder 接口，用于监听电话状态变化事件。
 * 通过 {@code ITelephonyRegistry$Stub.asInterface()} 获取服务代理。
 */
public class ITelephonyRegistry {

	/**
	 * ITelephonyRegistry 的 Stub 内部类反射包装器，提供 {@code asInterface()} 方法。
	 */
	public static class Stub {
		public static final MirrorReflection REF = MirrorReflection.on("com.android.internal.telephony.ITelephonyRegistry$Stub");
		public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
	}
}
