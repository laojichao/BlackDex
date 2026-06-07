package reflection.android.content.pm;

import android.content.pm.PackageParser;
import android.util.DisplayMetrics;

import java.io.File;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.PackageParser} 的反射包装器（Android 4.2 Jelly Bean MR1 版本）。
 * <p>
 * 对应 Jelly Bean MR1 (API 17) 版本的 PackageParser API 签名，
 * 与 {@link PackageParserJellyBean} 结构相同但针对不同 API 级别。
 */
public class PackageParserJellyBean17 {
    /** 反射目标类 {@code android.content.pm.PackageParser} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(android.content.pm.PackageParser.class);
    /** 方法 {@code collectCertificates(Package, int)}，收集包的签名证书 */
    public static MirrorReflection.MethodWrapper<Void> collectCertificates = REF.method("collectCertificates", PackageParser.Package.class, int.class);
    /** 构造器 {@code PackageParser(String)}，带源路径参数 */
    public static MirrorReflection.ConstructorWrapper<PackageParser> constructor = REF.constructor(String.class);
    /** 方法 {@code parsePackage(File, String, DisplayMetrics, int)}，解析 APK 文件 */
    public static MirrorReflection.MethodWrapper<PackageParser.Package> parsePackage = REF.method("parsePackage", File.class, String.class, DisplayMetrics.class, int.class);
}
