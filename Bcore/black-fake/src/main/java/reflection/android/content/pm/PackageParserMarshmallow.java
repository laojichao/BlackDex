package reflection.android.content.pm;

import android.content.pm.PackageParser;

import java.io.File;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.PackageParser} 的反射包装器（Android 6.0 Marshmallow 版本）。
 * <p>
 * 对应 Marshmallow (API 23) 版本的 PackageParser API 签名，
 * 与 Lollipop 版本 API 签名一致。
 */
public class PackageParserMarshmallow {
    /** 反射目标类 {@code android.content.pm.PackageParser} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(android.content.pm.PackageParser.class);

    /** 方法 {@code collectCertificates(Package, int)}，收集包的签名证书 */
    public static MirrorReflection.MethodWrapper<Void> collectCertificates = REF.method("collectCertificates", PackageParser.Package.class, int.class);
    /** 无参构造器 {@code PackageParser()} */
    public static MirrorReflection.ConstructorWrapper<PackageParser> constructor = REF.constructor();
    /** 方法 {@code parsePackage(File, int)}，解析 APK 文件 */
    public static MirrorReflection.MethodWrapper<PackageParser.Package> parsePackage = REF.method("parsePackage", File.class, int.class);
}
