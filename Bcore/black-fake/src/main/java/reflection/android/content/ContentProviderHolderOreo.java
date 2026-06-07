package reflection.android.content;

import android.content.pm.ProviderInfo;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.ContentProviderHolder} 的反射包装器（Android 8.0+ 版本）。
 * <p>
 * ContentProviderHolder 持有 ContentProvider 的引用及其 ProviderInfo。
 * 在 Android 8.0 (Oreo) 及以上版本中，该类的位置从 IActivityManager 内部移到了独立类。
 */
public class ContentProviderHolderOreo {
    /** 反射目标类 {@code android.app.ContentProviderHolder} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.app.ContentProviderHolder");

    /** 字段 {@code info}，ContentProvider 的 ProviderInfo */
    public static MirrorReflection.FieldWrapper<ProviderInfo> info = REF.field("info");
    /** 字段 {@code provider}，ContentProvider 的 IInterface 代理 */
    public static MirrorReflection.FieldWrapper<IInterface> provider = REF.field("provider");
}
