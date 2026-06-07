package reflection.android.os;

import android.os.Parcel;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.os.Bundle} 的反射包装器（Android 4.0 ICS 版本）。
 * <p>
 * 在 Ice Cream Sandwich (API 14-15) 版本中，Bundle 的 Parcel 数据字段名为
 * {@code mParcelledData}，但位于 Bundle 类本身而非 BaseBundle 中。
 */
public class BundleICS {
    /** 反射目标类 {@code android.os.Bundle} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(android.os.Bundle.class);
    /** 字段 {@code mParcelledData}，Bundle 内部的 Parcel 序列化数据 */
    public static MirrorReflection.FieldWrapper<Parcel> mParcelledData = REF.field("mParcelledData");
}