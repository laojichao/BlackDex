package reflection.android.content.pm;

import android.content.pm.ApplicationInfo;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.ApplicationInfo} 的反射包装器（Android 7.0 Nougat 版本）。
 * <p>
 * 提供对 Android 7.0 引入的设备保护和凭据保护数据目录字段的访问。
 */
public class ApplicationInfoN {
    /** 反射目标类 {@code android.content.pm.ApplicationInfo} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(ApplicationInfo.class);

    /** 字段 {@code deviceProtectedDataDir}，设备保护的数据目录 */
    public static MirrorReflection.FieldWrapper<String> deviceProtectedDataDir = REF.field("deviceProtectedDataDir");
    /** 字段 {@code deviceEncryptedDataDir}，设备加密的数据目录 */
    public static MirrorReflection.FieldWrapper<String> deviceEncryptedDataDir = REF.field("deviceEncryptedDataDir");
    /** 字段 {@code credentialProtectedDataDir}，凭据保护的数据目录 */
    public static MirrorReflection.FieldWrapper<String> credentialProtectedDataDir = REF.field("credentialProtectedDataDir");
    /** 字段 {@code credentialEncryptedDataDir}，凭据加密的数据目录 */
    public static MirrorReflection.FieldWrapper<String> credentialEncryptedDataDir = REF.field("credentialEncryptedDataDir");
}
