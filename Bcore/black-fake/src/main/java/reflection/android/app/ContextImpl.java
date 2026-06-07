package reflection.android.app;


import android.content.pm.PackageManager;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.ContextImpl} 的反射包装器。
 * <p>
 * ContextImpl 是 Android Context 的具体实现类，包含应用的基础包名、
 * 包信息（LoadedApk）、PackageManager 和操作包名等核心字段。
 */
public class ContextImpl {
    /** 反射目标类 {@code android.app.ContextImpl} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.app.ContextImpl");

    /** 字段 {@code mBasePackageName}，基础包名 */
    public static MirrorReflection.FieldWrapper<String> mBasePackageName = REF.field("mBasePackageName");
    /** 字段 {@code mPackageInfo}，LoadedApk 包信息对象 */
    public static MirrorReflection.FieldWrapper<Object> mPackageInfo = REF.field("mPackageInfo");
    /** 字段 {@code mPackageManager}，PackageManager 实例 */
    public static MirrorReflection.FieldWrapper<PackageManager> mPackageManager = REF.field("mPackageManager");
    /** 字段 {@code mOpPackageName}，操作包名（用于权限检查） */
    public static MirrorReflection.FieldWrapper<String> mOpPackageName = REF.field("mOpPackageName");
}
