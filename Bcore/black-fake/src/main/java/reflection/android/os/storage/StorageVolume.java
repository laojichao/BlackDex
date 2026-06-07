package reflection.android.os.storage;

import java.io.File;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.os.storage.StorageVolume} 的反射包装器。
 * <p>
 * 通过反射访问 StorageVolume 内部的 {@code mPath} 和 {@code mInternalPath} 字段，
 * 获取存储卷的实际文件路径（公开 API 中不直接暴露）。
 */
public class StorageVolume {
    /** 反射目标类 {@code android.os.storage.StorageVolume} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.os.storage.StorageVolume");

    /** 字段 {@code mPath}，存储卷的挂载路径 */
    public static MirrorReflection.FieldWrapper<File> mPath = REF.field("mPath");
    /** 字段 {@code mInternalPath}，存储卷的内部路径 */
    public static MirrorReflection.FieldWrapper<File> mInternalPath = REF.field("mInternalPath");
}
