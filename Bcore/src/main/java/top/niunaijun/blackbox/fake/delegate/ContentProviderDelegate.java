package top.niunaijun.blackbox.fake.delegate;

import android.net.Uri;
import android.os.Build;
import android.os.IInterface;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import reflection.android.app.IActivityManager;
import reflection.android.content.ContentProviderHolderOreo;
import reflection.android.providers.Settings;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.service.provider.ContentProviderStub;
import top.niunaijun.blackbox.fake.service.provider.SettingsProviderStub;
import top.niunaijun.blackbox.utils.compat.BuildCompat;

/**
 * ContentProvider 代理管理器，负责拦截和替换系统 ContentProvider 的调用。
 * <p>
 * 当应用通过 ContentResolver 访问 settings、media、telephony 等系统 Provider 时，
 * 本类将原始的 IInterface 替换为虚拟环境代理（{@link ContentProviderStub} 或
 * {@link SettingsProviderStub}），使包名参数被替换为宿主包名。
 * 同时管理 Settings Provider 的缓存清理，避免虚拟环境间的数据串扰。
 * </p>
 *
 * @author Milk
 * @see ContentProviderStub
 * @see SettingsProviderStub
 */
public class ContentProviderDelegate {
    public static final String TAG = "ContentProviderDelegate";
    private static Set<String> sInjected = new HashSet<>();

    /**
     * 更新 ContentProviderHolder 中的 provider 为虚拟环境代理。
     * <p>
     * 根据 authority 类型选择不同的代理实现：
     * <ul>
     *   <li>"settings" -> {@link SettingsProviderStub}</li>
     *   <li>其他 -> {@link ContentProviderStub}</li>
     * </ul>
     * 兼容 Android O (8.0) 前后 ContentProviderHolder 的不同实现。
     * </p>
     *
     * @param holder ContentProviderHolder 对象
     * @param auth   ContentProvider 的 authority 字符串
     */
    public static void update(Object holder, String auth) {
        IInterface iInterface;
        if (BuildCompat.isOreo()) {
            iInterface = ContentProviderHolderOreo.provider.get(holder);
        } else {
            iInterface = IActivityManager.ContentProviderHolder.provider.get(holder);
        }

        if (iInterface instanceof Proxy)
            return;
        IInterface vContentProvider;
        switch (auth) {
            case "settings":
                vContentProvider = new SettingsProviderStub().wrapper(iInterface, BlackBoxCore.getHostPkg());
                break;
            default:
                vContentProvider = new ContentProviderStub().wrapper(iInterface, BlackBoxCore.getHostPkg());
                break;
        }
        if (BuildCompat.isOreo()) {
            ContentProviderHolderOreo.provider.set(holder, vContentProvider);
        } else {
            IActivityManager.ContentProviderHolder.provider.set(holder, vContentProvider);
        }
    }

    /**
     * 初始化 ContentProvider 环境。
     * <p>
     * 清除 Settings Provider 的缓存，然后触发一次 content://settings 的查询
     * 以强制重新加载 Provider 连接。
     * </p>
     */
    public static void init() {
        clearSettingProvider();

        BlackBoxCore.getContext().getContentResolver().call(Uri.parse("content://settings"), "", null, null);
    }

    /**
     * 清除所有 Settings 命名空间（System、Secure、Global）的 ContentProvider 缓存。
     * <p>
     * 通过反射将 Settings.NameValueCache 中的 mContentProvider 字段置 null，
     * 强制下次访问时重新获取 Provider 连接。
     * </p>
     */
    public static void clearSettingProvider() {
        Object cache;
        cache = Settings.System.sNameValueCache.get();
        if (cache != null) {
            clearContentProvider(cache);
        }
        cache = Settings.Secure.sNameValueCache.get();
        if (cache != null) {
            clearContentProvider(cache);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Settings.Global.REF.getClazz() != null) {
            cache = Settings.Global.sNameValueCache.get();
            if (cache != null) {
                clearContentProvider(cache);
            }
        }
    }

    private static void clearContentProvider(Object cache) {
        if (BuildCompat.isOreo()) {
            Object holder = Settings.NameValueCacheOreo.mProviderHolder.get(cache);
            if (holder != null) {
                Settings.ContentProviderHolder.mContentProvider.set(holder, null);
            }
        } else {
            Settings.NameValueCache.mContentProvider.set(cache, null);
        }
    }
}
