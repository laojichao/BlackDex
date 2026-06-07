package reflection.android.content.pm;

import android.os.Parcelable;

import java.util.List;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.ParceledListSlice} 的反射包装器（Android 4.3 Jelly Bean MR2 版本）。
 * <p>
 * 对应 Jelly Bean MR2 (API 18) 版本。与标准版本不同，该版本使用带 {@code List} 参数的构造器。
 */
public class ParceledListSliceJBMR2 {
    /** 反射目标类 {@code android.content.pm.ParceledListSlice} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.content.pm.ParceledListSlice");
    /** 构造器 {@code ParceledListSlice(List)}，接收 List 参数 */
    public static MirrorReflection.ConstructorWrapper<Parcelable> constructor = REF.constructor(List.class);
}
