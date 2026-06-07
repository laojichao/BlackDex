package reflection.android.content.pm;

import android.content.pm.PackageParser;

import java.io.File;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.PackageParser} 的反射包装器（Android 5.0 Lollipop 版本）。
 * <p>
 * 对应 Lollipop (API 21) 版本的 PackageParser API 签名。
 * 与 Jelly Bean 版本不同，构造器无参，{@code parsePackage()} 签名简化为 (File, int)。
 */
public class PackageParserLollipop {
    /** 反射目标类 {@code android.content.pm.PackageParser} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(android.content.pm.PackageParser.class);

    /** 方法 {@code collectCertificates(Package, int)}，收集包的签名证书 */
    public static MirrorReflection.MethodWrapper<Void> collectCertificates = REF.method("collectCertificates", PackageParser.Package.class, int.class);
    /** 无参构造器 {@code PackageParser()} */
    public static MirrorReflection.ConstructorWrapper<PackageParser> constructor = REF.constructor();
    /** 方法 {@code parsePackage(File, int)}，解析 APK 文件 */
    public static MirrorReflection.MethodWrapper<PackageParser.Package> parsePackage = REF.method("parsePackage", File.class, int.class);
}
