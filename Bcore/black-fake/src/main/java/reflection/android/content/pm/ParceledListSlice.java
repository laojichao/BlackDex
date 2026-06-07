package reflection.android.content.pm;

import android.os.Parcelable;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.content.pm.ParceledListSlice} 的反射包装器。
 * <p>
 * ParceledListSlice 用于分批传输 Parcelable 列表，避免单次传输数据量过大。
 * 提供 {@code append()}、{@code setLastSlice()} 方法和无参构造器的访问。
 */
public class ParceledListSlice {
    /** 反射目标类 {@code android.content.pm.ParceledListSlice} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.content.pm.ParceledListSlice");
    /** 方法 {@code append()}，追加一个元素到切片列表 */
    public static MirrorReflection.MethodWrapper<Boolean> append = REF.method("append");
    /** 无参构造器 */
    public static MirrorReflection.ConstructorWrapper<Parcelable> constructor = REF.constructor();

    /** 方法 {@code setLastSlice()}，标记这是最后一个切片 */
    public static MirrorReflection.MethodWrapper<Void> setLastSlice = REF.method("setLastSlice");
}
