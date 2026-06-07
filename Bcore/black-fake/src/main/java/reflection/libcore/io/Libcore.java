package reflection.libcore.io;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code libcore.io.Libcore} 的反射包装器。
 * <p>
 * Libcore 是 Android 底层 I/O 库的核心类，包含 {@code os} 字段，
 * 持有 {@code libcore.io.Os} 接口的实例，提供低级系统调用封装。
 * BlackDex 使用该反射访问来操作文件系统底层功能。
 */
public class Libcore {
    /** 类的全限定名常量 */
    public static final String NAME = "libcore.io.Libcore";
    /** 反射目标类 {@code libcore.io.Libcore} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on(NAME);

    /** 字段 {@code os}，libcore.io.Os 接口实例，封装底层系统调用 */
    public static MirrorReflection.FieldWrapper<Object> os = REF.field("os");
}
