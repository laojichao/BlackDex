package reflection.android.content.pm;

import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.ILauncherApps} 的反射包装器。
 * <p>
 * ILauncherApps 是 LauncherApps 服务的 Binder 接口，用于与启动器交互。
 * 通过 {@code ILauncherApps$Stub.asInterface()} 获取服务代理。
 */
public class ILauncherApps {

    /**
     * ILauncherApps 的 Stub 内部类反射包装器，提供 {@code asInterface()} 方法。
     */
    public static final class Stub {
        public static final MirrorReflection REF = MirrorReflection.on("android.content.pm.ILauncherApps$Stub");
        public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface");
    }
}
