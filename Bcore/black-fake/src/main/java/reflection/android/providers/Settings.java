package reflection.android.providers;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.provider.Settings} 相关内部类的反射包装器集合。
 * <p>
 * 提供对 Settings.Global、Settings.Secure、Settings.System 中 {@code sNameValueCache}
 * 字段以及 {@code NameValueCache} 和 {@code ContentProviderHolder} 内部类的反射访问。
 * 用于直接操作系统设置值的底层 ContentProvider。
 */
public class Settings {

    /**
     * {@code Settings.Global} 的反射包装器（Android 4.2+ 版本）。
     * 提供全局设置的 {@code sNameValueCache} 静态字段访问。
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static class Global {
        public static final MirrorReflection REF = MirrorReflection.on(android.provider.Settings.Global.class);
        public static MirrorReflection.FieldWrapper<Object> sNameValueCache = REF.field("sNameValueCache");
    }

    /**
     * {@code Settings$NameValueCache} 的反射包装器（Android 7.0 以下版本）。
     * 提供 {@code mContentProvider} 字段访问，用于直接操作 Settings 的 ContentProvider。
     */
    public static class NameValueCache {
        public static final MirrorReflection REF = MirrorReflection.on("android.provider.Settings$NameValueCache");
        public static MirrorReflection.FieldWrapper<Object> mContentProvider = REF.field("mContentProvider");
    }

    /**
     * {@code Settings$NameValueCache} 的反射包装器（Android 8.0 Oreo 版本）。
     * 在 Oreo 版本中，ContentProvider 引用改为通过 {@code mProviderHolder} 间接持有。
     */
    public static class NameValueCacheOreo {
        public static final MirrorReflection REF = MirrorReflection.on("android.provider.Settings$NameValueCache");
        public static MirrorReflection.FieldWrapper<Object> mProviderHolder = REF.field("mProviderHolder");
    }

    /**
     * {@code Settings$ContentProviderHolder} 的反射包装器（Android 8.0+ 版本）。
     * 持有 ContentProvider 的 IInterface 代理引用。
     */
    public static class ContentProviderHolder {
        public static final MirrorReflection REF = MirrorReflection.on("android.provider.Settings$ContentProviderHolder");
        public static MirrorReflection.FieldWrapper<IInterface> mContentProvider = REF.field("mContentProvider");
    }

    /**
     * {@code Settings.Secure} 的反射包装器。
     * 提供安全设置的 {@code sNameValueCache} 静态字段访问。
     */
    public static class Secure {
        public static final MirrorReflection REF = MirrorReflection.on(android.provider.Settings.Secure.class);
        public static MirrorReflection.FieldWrapper<Object> sNameValueCache = REF.field("sNameValueCache");
    }

    /**
     * {@code Settings.System} 的反射包装器。
     * 提供系统设置的 {@code sNameValueCache} 静态字段访问。
     */
    public static class System {
        public static final MirrorReflection REF = MirrorReflection.on(android.provider.Settings.System.class);
        public static MirrorReflection.FieldWrapper<Object> sNameValueCache = REF.field("sNameValueCache");
    }
}