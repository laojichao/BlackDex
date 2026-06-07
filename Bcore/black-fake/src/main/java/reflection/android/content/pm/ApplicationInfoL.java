package reflection.android.content.pm;

import android.content.pm.ApplicationInfo;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.ApplicationInfo} 的反射包装器（Android 5.0 Lollipop 版本）。
 * <p>
 * 提供对 Android 5.0 引入的隐藏字段的访问，包括 CPU ABI 信息、
 * 扫描源目录和拆分公共源目录等。
 */
public class ApplicationInfoL {
    /** 反射目标类 {@code android.content.pm.ApplicationInfo} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(ApplicationInfo.class);
    /** 字段 {@code primaryCpuAbi}，主 CPU ABI */
    public static MirrorReflection.FieldWrapper<String> primaryCpuAbi = REF.field("primaryCpuAbi");
    /** 字段 {@code scanPublicSourceDir}，公共源目录扫描路径 */
    public static MirrorReflection.FieldWrapper<String> scanPublicSourceDir = REF.field("scanPublicSourceDir");
    /** 字段 {@code scanSourceDir}，源目录扫描路径 */
    public static MirrorReflection.FieldWrapper<String> scanSourceDir = REF.field("scanSourceDir");
    /** 字段 {@code secondaryCpuAbi}，次 CPU ABI */
    public static MirrorReflection.FieldWrapper<String> secondaryCpuAbi = REF.field("secondaryCpuAbi");
    /** 字段 {@code splitPublicSourceDirs}，拆分 APK 的公共源目录数组 */
    public static MirrorReflection.FieldWrapper<String[]> splitPublicSourceDirs = REF.field("splitPublicSourceDirs");
}
