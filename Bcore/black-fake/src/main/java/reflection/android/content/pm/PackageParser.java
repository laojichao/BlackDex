package reflection.android.content.pm;

import android.util.DisplayMetrics;
import java.io.File;
import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.PackageParser} 的反射包装器。
 * <p>
 * 通过反射访问 PackageParser 的构造器、{@code parsePackage()} 和 {@code collectCertificates()} 方法，
 * 用于在运行时解析 APK 文件。该类对应 Android 5.0+ 版本的 API 签名。
 */
public class PackageParser {
    /** 反射目标类 {@code android.content.pm.PackageParser} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(android.content.pm.PackageParser.class);

    /** 方法 {@code collectCertificates(Package, int)}，收集包的签名证书 */
    public static MirrorReflection.MethodWrapper<Void> collectCertificates = REF.method("collectCertificates", android.content.pm.PackageParser.Package.class, int.class);
    /** 构造器 {@code PackageParser(String)}，带源路径参数的构造器 */
    public static MirrorReflection.ConstructorWrapper<android.content.pm.PackageParser> constructor = REF.constructor(String.class);
    /** 方法 {@code parsePackage(File, String, DisplayMetrics, int)}，解析 APK 文件（Android 5.0+ 签名） */
    public static MirrorReflection.MethodWrapper<android.content.pm.PackageParser.Package> parsePackage = REF.method("parsePackage", File.class, String.class, DisplayMetrics.class, int.class);
}
