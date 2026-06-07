package reflection.android.os.storage;

import android.os.storage.StorageVolume;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.os.storage.StorageManager} 的反射包装器。
 * <p>
 * 通过反射访问 {@code getVolumeList()} 静态方法，
 * 获取系统中所有存储卷（包括内部存储和外部 SD 卡）的列表。
 */
public class StorageManager {
    /** 反射目标类 {@code android.os.storage.StorageManager} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.os.storage.StorageManager");

    /** 静态方法 {@code getVolumeList(int, int)}，获取存储卷列表 */
    public static MirrorReflection.StaticMethodWrapper<StorageVolume[]> getVolumeList = REF.staticMethod("getVolumeList", int.class, int.class);
}
