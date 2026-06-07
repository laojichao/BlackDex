package reflection.android.app;

import android.app.Application;
import android.app.Instrumentation;
import android.content.pm.ApplicationInfo;
import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.LoadedApk} 的反射包装器。
 * <p>
 * LoadedApk 表示一个已加载的 APK 包信息，包含 ApplicationInfo、
 * Application 创建方法和 ClassLoader 获取方法等。
 */
public class LoadedApk {
    /** 反射目标类 {@code android.app.LoadedApk} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.app.LoadedApk");
    /** 字段 {@code mApplicationInfo}，应用的 ApplicationInfo */
    public static MirrorReflection.FieldWrapper<ApplicationInfo> mApplicationInfo = REF.field("mApplicationInfo");
    /** 方法 {@code makeApplication(boolean, Instrumentation)}，创建 Application 实例 */
    public static MirrorReflection.MethodWrapper<Application> makeApplication = REF.method("makeApplication", boolean.class, Instrumentation.class);
    /** 方法 {@code getClassloader()}，获取应用的 ClassLoader */
    public static MirrorReflection.MethodWrapper<ClassLoader> getClassloader = REF.method("getClassloader");

    /** 字段 {@code mSecurityViolation}，是否存在安全违规标志 */
    public static MirrorReflection.FieldWrapper<Boolean> mSecurityViolation = REF.field("mSecurityViolation");
}