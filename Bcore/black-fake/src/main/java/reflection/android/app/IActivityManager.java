package reflection.android.app;

import android.content.pm.ProviderInfo;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.IActivityManager} 的反射包装器。
 * <p>
 * IActivityManager 是 ActivityManager 服务的 Binder 接口，提供启动 Activity、
 * 管理 ContentProvider 等核心系统服务方法。
 * 该类还包含 {@code ContentProviderHolder} 内部类的反射包装。
 */
public class IActivityManager {
    /** 反射目标类 {@code android.app.IActivityManager} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.app.IActivityManager");

    /** 实例方法 {@code startActivity()}，启动 Activity */
    public static MirrorReflection.MethodWrapper<Integer> startActivity = REF.method("startActivity");

    /**
     * Android 隐藏内部类 {@code IActivityManager$ContentProviderHolder} 的反射包装器。
     * <p>
     * ContentProvider 的持有者对象，包含 ProviderInfo 和 IInterface 代理。
     */
    public static class ContentProviderHolder {
        public static final MirrorReflection REF = MirrorReflection.on("android.app.IActivityManager$ContentProviderHolder");
        public static MirrorReflection.FieldWrapper<ProviderInfo> info = REF.field("info");
        public static MirrorReflection.FieldWrapper<IInterface> provider = REF.field("provider");
    }
}
