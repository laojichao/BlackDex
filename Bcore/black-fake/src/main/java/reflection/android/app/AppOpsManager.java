package reflection.android.app;

import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.AppOpsManager} 的反射包装器。
 * <p>
 * AppOpsManager 负责应用操作权限管理。该类通过反射访问其内部的
 * {@code mService} 字段，获取底层 IAppOpsService 的 Binder 代理。
 */
public class AppOpsManager {
    /** 反射目标类 {@code android.app.AppOpsManager} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(android.app.AppOpsManager.class);
    /** 字段 {@code mService}，IAppOpsService 的 Binder 代理 */
    public static MirrorReflection.FieldWrapper<IInterface> mService = REF.field("mService");
}
