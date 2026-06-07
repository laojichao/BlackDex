package reflection.android.os;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.os.Handler} 的反射包装器。
 * <p>
 * 通过反射访问 Handler 内部的 {@code mCallback} 字段，
 * 获取或替换 Handler 的回调处理器，用于消息拦截和 Hook。
 */
public class Handler {
    /** 反射目标类 {@code android.os.Handler} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(android.os.Handler.class);

    /** 字段 {@code mCallback}，Handler 的回调接口 */
    public static MirrorReflection.FieldWrapper<android.os.Handler.Callback> mCallback = REF.field("mCallback");
}
