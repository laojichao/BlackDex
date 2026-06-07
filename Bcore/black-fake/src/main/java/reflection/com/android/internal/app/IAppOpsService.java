package reflection.com.android.internal.app;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;


/**
 * Android 隐藏 API {@code com.android.internal.app.IAppOpsService} 的反射包装器。
 * <p>
 * IAppOpsService 是应用操作权限管理服务的 Binder 接口，负责检查和记录应用的操作权限。
 * 通过 {@code IAppOpsService$Stub.asInterface()} 获取服务代理。
 */
public class IAppOpsService {
    /**
     * IAppOpsService 的 Stub 内部类反射包装器，提供 {@code asInterface()} 方法。
     */
    public static class Stub {
        public static final String NAME = "com.android.internal.app.IAppOpsService$Stub";
        private static final MirrorReflection REF = MirrorReflection.on(NAME);
        public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}