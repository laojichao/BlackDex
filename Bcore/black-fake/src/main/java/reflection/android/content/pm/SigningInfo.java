package reflection.android.content.pm;

import android.content.pm.PackageParser;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.SigningInfo} 的反射包装器。
 * <p>
 * SigningInfo 是 Android 9.0 (Pie) 引入的签名信息类，替代了旧的 {@code PackageInfo.signatures} 字段。
 * 通过反射访问其内部的 {@code mSigningDetails} 字段，获取 {@link PackageParser.SigningDetails} 对象。
 */
public class SigningInfo {
    /** 反射目标类 {@code android.content.pm.SigningInfo} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.content.pm.SigningInfo");

    /** 字段 {@code mSigningDetails}，签名详情对象 */
    public static MirrorReflection.FieldWrapper<PackageParser.SigningDetails> mSigningDetails = REF.field("mSigningDetails");
}
