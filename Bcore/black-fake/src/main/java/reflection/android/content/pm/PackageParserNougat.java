package reflection.android.content.pm;

import android.content.pm.PackageParser;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.PackageParser} 的反射包装器（Android 7.0 Nougat 版本）。
 * <p>
 * 对应 Nougat (API 24) 版本。{@code collectCertificates} 从此版本开始变为静态方法，
 * 签名变为 {@code static collectCertificates(Package, int)}。
 */
public class PackageParserNougat {
    /** 反射目标类 {@code android.content.pm.PackageParser} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(android.content.pm.PackageParser.class);
    /** 静态方法 {@code collectCertificates(Package, int)}，收集包的签名证书 */
    public static MirrorReflection.StaticMethodWrapper<Void> collectCertificates = REF.staticMethod("collectCertificates", PackageParser.Package.class, int.class);
}
