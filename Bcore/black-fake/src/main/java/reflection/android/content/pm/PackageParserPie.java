package reflection.android.content.pm;

import android.content.pm.PackageParser;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.PackageParser} 的反射包装器（Android 9.0 Pie 版本）。
 * <p>
 * 对应 Pie (API 28) 版本。{@code collectCertificates} 的签名在此版本发生变化，
 * 第二个参数从 {@code int} 改为 {@code boolean}（是否跳过验证）。
 */
public class PackageParserPie {
    /** 反射目标类 {@code android.content.pm.PackageParser} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(android.content.pm.PackageParser.class);

    /** 静态方法 {@code collectCertificates(Package, boolean)}，收集包的签名证书 */
    public static MirrorReflection.StaticMethodWrapper<Void> collectCertificates = REF.staticMethod("collectCertificates", PackageParser.Package.class, boolean.class);
}
