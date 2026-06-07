package reflection.android.os;

import android.os.Parcel;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.os.BaseBundle} 的反射包装器。
 * <p>
 * BaseBundle 是 Bundle 的基类，该类通过反射访问 {@code mParcelledData} 字段，
 * 获取 Bundle 内部未解析的 Parcel 数据。在 Android 4.4 (KitKat) 及以上版本中使用。
 */
public class BaseBundle {
    /** 反射目标类 {@code android.os.BaseBundle} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.os.BaseBundle");
    /** 字段 {@code mParcelledData}，Bundle 内部的 Parcel 序列化数据 */
    public static MirrorReflection.FieldWrapper<Parcel> mParcelledData = REF.field("mParcelledData");
}